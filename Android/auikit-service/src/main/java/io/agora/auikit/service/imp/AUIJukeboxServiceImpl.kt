package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUIChooseMusicModel
import io.agora.auikit.model.AUIMusicModel
import io.agora.auikit.model.AUIPlayStatus
import io.agora.auikit.service.IAUIJukeboxService
import io.agora.auikit.service.IAUIJukeboxService.AUIJukeboxRespObserver
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIChooseSongListCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUIMusicListCallback
import io.agora.auikit.service.collection.AUIAttributesModel
import io.agora.auikit.service.collection.AUIListCollection
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import io.agora.rtc2.Constants

val kChooseSongKey = "song"

enum class AUIJukeboxCmd {
    chooseSongCmd,
    removeSongCmd,
    pingSongCmd,
    updatePlayStatusCmd
}

class AUIJukeboxServiceImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val ktvApi: KTVApi
) : IAUIJukeboxService {

    private val TAG: String = "Jukebox_LOG"

    private val observableHelper =
        ObservableHelper<AUIJukeboxRespObserver>()

    private val listCollection = AUIListCollection(channelName, kChooseSongKey, rtmManager)

    // 选歌列表
    private val chooseMusicList = mutableListOf<AUIChooseMusicModel>()

    init {
        listCollection.subscribeWillAdd(this::metadataWillAdd)
        listCollection.subscribeWillRemove(this::metadataWillRemove)
        listCollection.subscribeWillMerge(this::metadataWillMerge)
        listCollection.subscribeAttributesDidChanged(this::onAttributesChanged)
        listCollection.subscribeAttributesWillSet(this::onAttributesWillSet)
    }

    override fun deInitService(completion: AUICallback?) {
        super.deInitService(completion)
        listCollection.cleanMetaData(completion)
        listCollection.release()
    }

    override fun registerRespObserver(observer: AUIJukeboxRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: AUIJukeboxRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    // 获取歌曲列表
    override fun getMusicList(
        chartId: Int,
        page: Int,
        pageSize: Int,
        completion: AUIMusicListCallback?
    ) {
        Log.d(TAG, "getMusicList call chartId:$chartId,page:$page,pageSize:$pageSize")
        val jsonOption = "{\"pitchType\":1,\"needLyric\":true}"
        ktvApi.searchMusicByMusicChartId(chartId,
            page,
            pageSize,
            jsonOption,
            onMusicCollectionResultListener = { requestId, status, p, size, total, list ->
                Log.d(
                    TAG,
                    "getMusicList call return chartId:$chartId,page:$page,pageSize:$pageSize,outListSize=${list?.size}",
                )
                if (status != Constants.ERR_OK) {
                    completion?.onResult(null, null)
                    return@searchMusicByMusicChartId
                }
                val musicList = mutableListOf<AUIMusicModel>()
                list?.forEach {
                    val musicModel = AUIMusicModel().apply {
                        songCode = it.songCode.toString()
                        name = it.name
                        singer = it.singer
                        poster = it.poster
                        releaseTime = it.releaseTime
                        duration = it.durationS
                    }
                    musicList.add(musicModel)
                }
                completion?.onResult(null, musicList)
            })
    }

    // 搜索歌曲
    override fun searchMusic(
        keyword: String?, page: Int, pageSize: Int, completion: AUIMusicListCallback?
    ) {
        Log.d(TAG, "searchMusic call keyword:$keyword,page:$page,pageSize:$pageSize")
        val jsonOption = "{\"pitchType\":1,\"needLyric\":true}"
        ktvApi.searchMusicByKeyword(keyword ?: "",
            page,
            pageSize,
            jsonOption,
            onMusicCollectionResultListener = { requestId, status, p, size, total, list ->
                Log.d(TAG, "searchMusic call return keyword:$keyword,page:$page,pageSize:$pageSize")
                if (status != Constants.ERR_OK) {
                    completion?.onResult(null, null)
                    return@searchMusicByKeyword
                }
                val musicList = mutableListOf<AUIMusicModel>()
                list?.forEach {
                    val musicModel = AUIMusicModel().apply {
                        songCode = it.songCode.toString()
                        name = it.name
                        singer = it.singer
                        poster = it.poster
                        releaseTime = it.releaseTime
                        duration = it.durationS
                    }
                    musicList.add(musicModel)
                }
                completion?.onResult(null, musicList)
            })
    }

    // 获取当前点歌列表
    override fun getAllChooseSongList(completion: AUIChooseSongListCallback?) {
        Log.d(TAG, "getAllChooseSongList call")
        listCollection.getMetaData { error, value ->
            if (error != null) {
                completion?.onResult(error, null)
                return@getMetaData
            }
            val chooseMusics: List<AUIChooseMusicModel> =
                GsonTools.toList(GsonTools.beanToString(value), AUIChooseMusicModel::class.java)
                    ?: mutableListOf()
            chooseMusicList.clear()
            chooseMusicList.addAll(chooseMusics)
            completion?.onResult(null, chooseMusics)
        }
    }

    // 点一首歌
    override fun chooseSong(song: AUIMusicModel, completion: AUICallback?) {
        val chooseSong = AUIChooseMusicModel()
        chooseSong.createAt = System.currentTimeMillis()
        chooseSong.owner = roomContext.currentUserInfo
        chooseSong.songCode = song.songCode
        chooseSong.name = song.name
        chooseSong.singer = song.singer
        chooseSong.poster = song.poster
        chooseSong.releaseTime = song.releaseTime
        chooseSong.duration = song.duration
        chooseSong.musicUrl = song.musicUrl
        chooseSong.lrcUrl = song.lrcUrl

        val metadata = HashMap(GsonTools.beanToMap(chooseSong))
        listCollection.addMetaData(
            AUIJukeboxCmd.chooseSongCmd.name,
            metadata,
            listOf(mapOf("songCode" to song.songCode)),
            completion
        )
    }

    // 移除一首自己点的歌
    override fun removeSong(songCode: String, completion: AUICallback?) {
        listCollection.removeMetaData(
            AUIJukeboxCmd.removeSongCmd.name,
            listOf(mapOf("songCode" to songCode)),
            completion
        )
    }

    // 置顶歌曲
    override fun pingSong(songCode: String, completion: AUICallback?) {
        listCollection.mergeMetaData(
            AUIJukeboxCmd.pingSongCmd.name,
            mapOf("pinAt" to System.currentTimeMillis()),
            listOf(mapOf("songCode" to songCode, "userId" to roomContext.currentUserInfo.userId)),
            completion
        )
    }

    // 更新播放状态
    override fun updatePlayStatus(
        songCode: String,
        @AUIPlayStatus playStatus: Int,
        completion: AUICallback?
    ) {
        Log.d(TAG, "updatePlayStatus: $songCode, playStatus: $playStatus")
        listCollection.mergeMetaData(
            AUIJukeboxCmd.updatePlayStatusCmd.name,
            mapOf("status" to playStatus),
            listOf(mapOf("songCode" to songCode, "userId" to roomContext.currentUserInfo.userId)),
            completion
        )
    }

    override fun cleanUserInfo(userId: String, completion: AUICallback?) {
        super.cleanUserInfo(userId, completion)
        listCollection.removeMetaData(
            AUIJukeboxCmd.removeSongCmd.name,
            listOf(mapOf("owner" to mapOf("userId" to userId))),
            completion
        )
    }


    override fun getChannelName() = channelName

    private fun sortChooseSongList(songList: List<Map<String, Any>>): List<Map<String, Any>> {
        val sortSongList = songList.sortedWith(Comparator<Map<String, Any>> { o1, o2 ->
            if (o1["status"] == AUIPlayStatus.playing.toLong()) {
                return@Comparator -1
            }
            if (o2["status"] == AUIPlayStatus.playing.toLong()) {
                return@Comparator 1
            }

            val pinAt1 = o1["pinAt"] as? Long ?: 0
            val pinAt2 = o2["pinAt"] as? Long ?: 0
            val createAt1 = o1["createAt"] as? Long ?: 0
            val createAt2 = o2["createAt"] as? Long ?: 0
            if (pinAt1 < 1 && pinAt2 < 1) {
                return@Comparator if((createAt1 - createAt2) > 0) 1 else -1
            }

            return@Comparator if((pinAt2 - pinAt1) > 0) 1 else -1
        })

        return sortSongList
    }


    private fun onAttributesChanged(channelName: String, observeKey: String, value: AUIAttributesModel) {
        if (observeKey != kChooseSongKey) {
            return
        }
        Log.d(TAG, "channelName:$channelName,key:$observeKey,value:$value")
        val changedSongs: List<AUIChooseMusicModel> =
            GsonTools.toList(GsonTools.beanToString(value.getList()), AUIChooseMusicModel::class.java)
                ?: mutableListOf()
        this.chooseMusicList.clear()
        this.chooseMusicList.addAll(changedSongs)
        observableHelper.notifyEventHandlers { delegate: AUIJukeboxRespObserver ->
            delegate.onUpdateAllChooseSongs(this.chooseMusicList)
        }
    }

    private fun onAttributesWillSet(
        channelName: String,
        observeKey: String,
        valueCmd: String?,
        value: AUIAttributesModel
    ): AUIAttributesModel {
        if (valueCmd == AUIJukeboxCmd.pingSongCmd.name) {
            val songList = value.getList() ?: return value
            return AUIAttributesModel(sortChooseSongList(songList))
        }
        return value
    }

    private fun metadataWillAdd(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>
    ): AUIException? {

        val owner = value["owner"] as? Map<*, *>
        val userId = owner?.get("userId")
        if (valueCmd == AUIJukeboxCmd.chooseSongCmd.name) {
            val metaData = mutableMapOf<String, String>()
            value.entries.forEach {
                metaData[it.key] = GsonTools.beanToString(it.value) ?: return@forEach
            }
            var error: AUIException? = null
            observableHelper.notifyEventHandlers {
                error = it.onSongWillAdd(publisherId, metaData)?.let { return@notifyEventHandlers }
            }
            return error
        }
        return null
    }

    private fun metadataWillRemove(
        publisherId: String,
        valueCmd: String?,
        value: Map<String, Any>
    ): AUIException? {

        val owner = value["owner"] as? Map<*, *>
        val userId = owner?.get("userId") as? String ?: ""
        val songCode = value["songCode"] as? String
        if (valueCmd == AUIJukeboxCmd.removeSongCmd.name) {
            if (publisherId != userId && !roomContext.isRoomOwner(channelName, publisherId)) {
                return AUIException(AUIException.ERROR_CODE_PERMISSION_LEAK, "")
            }
            val metaData = mutableMapOf<String, String>()
            value.entries.forEach {
                metaData[it.key] = GsonTools.beanToString(it.value) ?: return@forEach
            }
            var error: AUIException? = null
            observableHelper.notifyEventHandlers {
                error = it.onSongWillRemove(userId, metaData)?.let { return@notifyEventHandlers }
            }
            return error
        }
        return null
    }

    private fun metadataWillMerge(
        publisherId: String,
        valueCmd: String?,
        newValue: Map<String, Any>,
        oldValue: Map<String, Any>
    ): AUIException? {
        return null
    }
}