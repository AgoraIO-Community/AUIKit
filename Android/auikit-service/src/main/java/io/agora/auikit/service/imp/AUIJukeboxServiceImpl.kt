package io.agora.auikit.service.imp

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import io.agora.auikit.model.AUIChooseMusicModel
import io.agora.auikit.model.AUIMusicModel
import io.agora.auikit.model.AUIPlayStatus
import io.agora.auikit.service.IAUIJukeboxService
import io.agora.auikit.service.IAUIJukeboxService.AUIJukeboxRespObserver
import io.agora.auikit.service.callback.AUICallback
import io.agora.auikit.service.callback.AUIChooseSongListCallback
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUIMusicListCallback
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.rtm.AUIRtmAttributeRespObserver
import io.agora.auikit.service.rtm.AUIRtmException
import io.agora.auikit.service.rtm.AUIRtmManager
import io.agora.auikit.service.rtm.AUIRtmMessageRespObserver
import io.agora.auikit.service.rtm.AUIRtmPublishModel
import io.agora.auikit.service.rtm.AUIRtmReceiptModel
import io.agora.auikit.service.rtm.AUIRtmSongInfo
import io.agora.auikit.service.rtm.kAUISongAddNetworkInterface
import io.agora.auikit.service.rtm.kAUISongPinNetworkInterface
import io.agora.auikit.service.rtm.kAUISongPlayNetworkInterface
import io.agora.auikit.service.rtm.kAUISongRemoveNetworkInterface
import io.agora.auikit.service.rtm.kAUISongStopNetworkInterface
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ObservableHelper
import io.agora.rtc2.Constants
import retrofit2.Response

val kChooseSongKey = "song"
class AUIJukeboxServiceImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmManager,
    private val ktvApi: KTVApi
) : IAUIJukeboxService, AUIRtmAttributeRespObserver, AUIRtmMessageRespObserver {

    private val TAG: String = "Jukebox_LOG"

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()

    private val observableHelper =
        ObservableHelper<AUIJukeboxRespObserver>()

    // 选歌列表
    private val chooseMusicList = mutableListOf<AUIChooseMusicModel>()

    init {
        rtmManager.subscribeAttribute(channelName, kChooseSongKey, this)
        rtmManager.subscribeMessage(this)
    }

    override fun deInitService(completion: AUICallback?) {
        super.deInitService(completion)
        if (roomContext.getArbiter(channelName)?.isArbiter() != true) {
            return
        }

        rtmManager.cleanBatchMetadata(
            channelName,
            remoteKeys = listOf(kChooseSongKey)
        ) { error ->
            if (error != null) {
                completion?.onResult(AUIException(AUIException.ERROR_CODE_RTM, ""))
            } else {
                completion?.onResult(null)
            }
        }
    }

    override fun registerRespObserver(observer: AUIJukeboxRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: AUIJukeboxRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    // 获取歌曲列表
    override fun getMusicList(chartId: Int, page: Int, pageSize: Int, completion: AUIMusicListCallback?) {
        Log.d(TAG, "getMusicList call chartId:$chartId,page:$page,pageSize:$pageSize")
        val jsonOption = "{\"pitchType\":1,\"needLyric\":true}"
        ktvApi.searchMusicByMusicChartId(chartId,
            page,
            pageSize,
            jsonOption,
            onMusicCollectionResultListener = { requestId, status, p, size, total, list ->
                Log.d(TAG, "getMusicList call return chartId:$chartId,page:$page,pageSize:$pageSize,outListSize=${list?.size}",)
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
        rtmManager.getMetadata(channelName, completion = { rtmException, map ->
            if (rtmException != null) {
                completion?.onResult(
                    AUIException(
                        rtmException.code,
                        rtmException.reason
                    ), null)
                return@getMetadata
            }
            val chooseSongStr = map?.get(kChooseSongKey)
            if (chooseSongStr.isNullOrEmpty()) {
                completion?.onResult(null, null)
                return@getMetadata
            }
            val chooseMusics: List<AUIChooseMusicModel> =
                gson.fromJson(chooseSongStr, object : TypeToken<List<AUIChooseMusicModel>>() {}.type) ?: mutableListOf()
            chooseMusicList.clear()
            chooseMusicList.addAll(chooseMusics)
            completion?.onResult(null, chooseMusics)
        })
    }

    // 点一首歌
    override fun chooseSong(song: AUIMusicModel, completion: AUICallback?) {
        if (roomContext.getArbiter(channelName)?.isArbiter() == true) {
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
            rtmChooseSong(chooseSong, completion)
            return
        }

        val rtmSongInfo = AUIRtmSongInfo(
            channelName,
            roomContext.currentUserInfo.userId,
            song.songCode,
            song.singer,
            song.name,
            song.poster,
            song.duration,
            song.musicUrl,
            song.lrcUrl,
            roomContext.currentUserInfo
        )

        rtmManager.publishAndWaitReceipt(
            channelName,
            lockOwnerId,
            AUIRtmPublishModel(
                interfaceName = kAUISongAddNetworkInterface,
                data = rtmSongInfo,
                channelName = channelName
            )
        ) { error ->
            if (error != null) {
                completion?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error=$error"))
            } else {
                completion?.onResult(null)
            }
        }
    }

    // 移除一首自己点的歌
    override fun removeSong(songCode: String, completion: AUICallback?) {
        if (roomContext.getArbiter(channelName)?.isArbiter() == true) {
            rtmRemoveSong(songCode, roomContext.currentUserInfo.userId, completion)
            return
        }

        val rtmSongInfo = AUIRtmSongInfo(
            channelName,
            roomContext.currentUserInfo.userId,
            songCode = songCode
        )

        rtmManager.publishAndWaitReceipt(
            channelName,
            lockOwnerId,
            AUIRtmPublishModel(
                interfaceName = kAUISongRemoveNetworkInterface,
                data = rtmSongInfo,
                channelName = channelName
            )
        ) { error ->
            if (error != null) {
                completion?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error=$error"))
            } else {
                completion?.onResult(null)
            }
        }
    }

    // 置顶歌曲
    override fun pingSong(songCode: String, completion: AUICallback?) {
        if (roomContext.getArbiter(channelName)?.isArbiter() == true) {
            rtmPinSong(songCode, roomContext.currentUserInfo.userId, completion)
            return
        }

        val rtmSongInfo = AUIRtmSongInfo(
            channelName,
            roomContext.currentUserInfo.userId
        )

        rtmManager.publishAndWaitReceipt(
            channelName,
            lockOwnerId,
            AUIRtmPublishModel(
                interfaceName = kAUISongPinNetworkInterface,
                data = rtmSongInfo,
                channelName = channelName
            )
        ) { error ->
            if (error != null) {
                completion?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error=$error"))
            } else {
                completion?.onResult(null)
            }
        }
    }

    // 更新播放状态
    override fun updatePlayStatus(songCode: String, @AUIPlayStatus playStatus: Int, completion: AUICallback?) {
        Log.d(TAG, "updatePlayStatus: $songCode, playStatus: $playStatus")
        if (roomContext.getArbiter(channelName)?.isArbiter() == true) {
            rtmUpdatePlayStatus(songCode, playStatus, roomContext.currentUserInfo.userId, completion)
            return
        }
        val rtmSongInfo = AUIRtmSongInfo(
            channelName,
            roomContext.currentUserInfo.userId
        )
        val model = if (playStatus == AUIPlayStatus.playing) {
            AUIRtmPublishModel(
                interfaceName = kAUISongPlayNetworkInterface,
                data = rtmSongInfo,
                channelName = channelName
            )
        } else{
            AUIRtmPublishModel(
                interfaceName = kAUISongStopNetworkInterface,
                data = rtmSongInfo,
                channelName = channelName
            )
        }
        rtmManager.publishAndWaitReceipt(
            channelName,
            lockOwnerId,
            model
        ) { error ->
            if (error != null) {
                completion?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error=$error"))
            } else {
                completion?.onResult(null)
            }
        }
    }

    override fun cleanUserInfo(userId: String, completion: AUICallback?) {
        super.cleanUserInfo(userId, completion)
        val musicList = chooseMusicList.filter { it.owner?.userId != userId }
        if (musicList.size != chooseMusicList.size) {
            val metadata = mutableMapOf<String, String>()
            metadata[kChooseSongKey] = GsonTools.beanToString(musicList) ?: ""
            rtmManager.setBatchMetadata(
                channelName,
                metadata = metadata
            ) { error ->
                if (error != null) {
                    completion?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error=$error"))
                } else {
                    completion?.onResult(null)
                }
            }
        }
    }


    override fun getChannelName() = channelName

    private fun isSuccess(resp: CommonResp<Any>): Boolean {
        return resp.code == 0
    }

    private fun isNetSuccess(response: Response<*>): Boolean {
        return response.code() == 200
    }

    override fun onAttributeChanged(channelName: String, key: String, value: Any) {
        if (key != kChooseSongKey) {
            return
        }
        Log.d(TAG, "channelName:$channelName,key:$key,value:$value")
        val changedSongs: List<AUIChooseMusicModel> =
            gson.fromJson(value as String, object : TypeToken<List<AUIChooseMusicModel>>() {}.type) ?: mutableListOf()
        this.chooseMusicList.clear()
        this.chooseMusicList.addAll(changedSongs)
        observableHelper.notifyEventHandlers { delegate: AUIJukeboxRespObserver ->
            delegate.onUpdateAllChooseSongs(this.chooseMusicList)
        }
    }

    override fun onMessageReceive(channelName: String, publisherId: String, message: String) {
        if (publisherId.isEmpty() && channelName != this.channelName) {
            return
        }

        val publishModel: AUIRtmPublishModel<JsonObject>? =
            GsonTools.toBean(message, object : TypeToken<AUIRtmPublishModel<JsonObject>>() {}.type)

        if (publishModel?.uniqueId == null) {
            return
        }

        if (publishModel.interfaceName == null) {
            // receipt message from arbiter
            val receiptModel = GsonTools.toBean(message, AUIRtmReceiptModel::class.java) ?: return
            if (receiptModel.code == 0) {
                // success
                rtmManager.markReceiptFinished(receiptModel.uniqueId, null)
            } else {
                // failure
                rtmManager.markReceiptFinished(
                    receiptModel.uniqueId, AUIRtmException(
                        receiptModel.code,
                        receiptModel.reason, "receipt message from arbiter"
                    )
                )
            }
        } else {
            val song =
                GsonTools.toBean(publishModel.data, AUIRtmSongInfo::class.java)
            if (song == null) {
                rtmManager.sendReceipt(
                    channelName,
                    publisherId,
                    AUIRtmReceiptModel(publishModel.uniqueId, -1, channelName, "Gson parse failed!")
                )
                return
            }
            when (publishModel.interfaceName) {
                kAUISongAddNetworkInterface -> {
                    val chooseSong = AUIChooseMusicModel()
                    chooseSong.createAt = System.currentTimeMillis()
                    chooseSong.owner = song.owner
                    chooseSong.songCode = song.songCode
                    chooseSong.name = song.name
                    chooseSong.singer = song.singer
                    chooseSong.poster = song.poster
                    chooseSong.duration = song.duration
                    chooseSong.musicUrl = song.musicUrl
                    chooseSong.lrcUrl = song.lrcUrl
                    rtmChooseSong(chooseSong){ error ->
                        rtmManager.sendReceipt(
                            channelName,
                            publisherId,
                            AUIRtmReceiptModel(
                                publishModel.uniqueId,
                                error?.code ?: 0,
                                channelName,
                                error?.message ?: ""
                            )
                        )
                    }
                }
                kAUISongRemoveNetworkInterface -> {
                    rtmRemoveSong(song.songCode, song.userId) { error ->
                        rtmManager.sendReceipt(
                            channelName,
                            publisherId,
                            AUIRtmReceiptModel(
                                publishModel.uniqueId,
                                error?.code ?: 0,
                                channelName,
                                error?.message ?: ""
                            )
                        )
                    }
                }
                kAUISongPinNetworkInterface -> {
                    rtmPinSong(song.songCode, song.userId) { error ->
                        rtmManager.sendReceipt(
                            channelName,
                            publisherId,
                            AUIRtmReceiptModel(
                                publishModel.uniqueId,
                                error?.code ?: 0,
                                channelName,
                                error?.message ?: ""
                            )
                        )
                    }
                }
                kAUISongPlayNetworkInterface -> {
                    rtmUpdatePlayStatus(song.songCode, AUIPlayStatus.playing, song.userId){error ->
                        rtmManager.sendReceipt(
                            channelName,
                            publisherId,
                            AUIRtmReceiptModel(
                                publishModel.uniqueId,
                                error?.code ?: 0,
                                channelName,
                                error?.message ?: ""
                            )
                        )
                    }
                }
                kAUISongStopNetworkInterface -> {
                    rtmUpdatePlayStatus(song.songCode, AUIPlayStatus.idle, song.userId){error ->
                        rtmManager.sendReceipt(
                            channelName,
                            publisherId,
                            AUIRtmReceiptModel(
                                publishModel.uniqueId,
                                error?.code ?: 0,
                                channelName,
                                error?.message ?: ""
                            )
                        )
                    }
                }
            }
        }
    }


    private fun rtmChooseSong(song: AUIChooseMusicModel, callback: AUICallback?) {
        if(chooseMusicList.find { it.songCode == song.songCode } != null){
            callback?.onResult(AUIException(AUIException.ERROR_CODE_SONG_ALREADY_EXIST, ""))
            return
        }

        val metaData = mutableMapOf<String, String>()
        var willError: AUIException? = null
        observableHelper.notifyEventHandlers {
            willError = it.onSongWillAdd(song.owner?.userId, metaData)
            if (willError != null) {
                return@notifyEventHandlers
            }
        }
        if(willError != null){
            callback?.onResult(willError)
            return
        }

        val songList = ArrayList(chooseMusicList)
        songList.add(song)
        metaData[kChooseSongKey] = GsonTools.beanToString(songList) ?: ""
        rtmManager.setBatchMetadata(channelName, metadata = metaData){ error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    private fun rtmRemoveSong(songCode: String, removeUserId: String, callback: AUICallback?){
        val index = chooseMusicList.indexOfFirst { it.songCode == songCode }
        if(index < 0){
            callback?.onResult(AUIException(AUIException.ERROR_CODE_SONG_NOT_EXIST, ""))
            return
        }
        val song = chooseMusicList[index]
        if(song.owner?.userId != removeUserId && !roomContext.isRoomOwner(channelName)){
            callback?.onResult(AUIException(AUIException.ERROR_CODE_PERMISSION_LEAK, ""))
            return
        }

        val metaData = mutableMapOf<String, String>()
        var willError: AUIException? = null
        observableHelper.notifyEventHandlers {
            willError = it.onSongWillRemove(song.owner?.userId, metaData)
            if (willError != null) {
                return@notifyEventHandlers
            }
        }
        if(willError != null){
            callback?.onResult(willError)
            return
        }

        val songList = ArrayList(chooseMusicList.filter { it.songCode != songCode })
        metaData[kChooseSongKey] = GsonTools.beanToString(songList) ?: ""
        rtmManager.setBatchMetadata(channelName, metadata = metaData){ error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    private fun rtmPinSong(songCode: String, updateUserId: String, callback: AUICallback?) {
        val index = chooseMusicList.indexOfFirst { it.songCode == songCode }
        if(index < 0){
            callback?.onResult(AUIException(AUIException.ERROR_CODE_SONG_NOT_EXIST, ""))
            return
        }

        if(chooseMusicList[index].owner?.userId != updateUserId){
            callback?.onResult(AUIException(AUIException.ERROR_CODE_PERMISSION_LEAK, ""))
            return
        }

        val songList = ArrayList(chooseMusicList)
        val song = songList[index]
        val origPinAt = song.pinAt
        song.pinAt = System.currentTimeMillis()
        songList.sortWith { o1, o2 ->
            //歌曲播放中优先（只会有一个，多个目前没有，如果有需要修改排序策略）
            if (o1.status == AUIPlayStatus.playing) {
                return@sortWith -1
            }
            if (o2.status == AUIPlayStatus.playing) {
                return@sortWith 1
            }

            //都没有置顶时间，比较创建时间，创建时间小的在前（即创建早的在前）
            if(o1.pinAt < 1 && o2.pinAt < 1){
                return@sortWith if (o2.createAt - o1.createAt > 0) -1 else 1
            }

            //有一个有置顶时间，置顶时间大的在前（即后置顶的在前）
            return@sortWith if (o2.pinAt - o1.pinAt > 0) 1 else -1
        }
        val metaData = mutableMapOf<String, String>()
        metaData[kChooseSongKey] = GsonTools.beanToString(songList) ?: ""
        song.pinAt = origPinAt
        rtmManager.setBatchMetadata(channelName, metadata = metaData) { error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    private fun rtmUpdatePlayStatus(songCode: String, playStatus: Int, updateUserId: String, callback: AUICallback?){
        val index = chooseMusicList.indexOfFirst { it.songCode == songCode }
        if(index < 0){
            callback?.onResult(AUIException(AUIException.ERROR_CODE_SONG_NOT_EXIST, ""))
            return
        }

        val song = chooseMusicList[index]
        if(song.owner?.userId != updateUserId && !roomContext.isRoomOwner(channelName)){
            callback?.onResult(AUIException(AUIException.ERROR_CODE_PERMISSION_LEAK, ""))
            return
        }

        val metaData = mutableMapOf<String, String>()

        val origStatus = song.status
        song.status = playStatus
        val songList = ArrayList(chooseMusicList)
        metaData[kChooseSongKey] = GsonTools.beanToString(songList) ?: ""
        song.status = origStatus
        rtmManager.setBatchMetadata(channelName, metadata = metaData){ error ->
            if (error == null) {
                callback?.onResult(null)
            } else {
                callback?.onResult(AUIException(AUIException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

}