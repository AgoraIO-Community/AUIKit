package io.agora.auikit.service.imp

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import io.agora.auikit.model.AUiChooseMusicModel
import io.agora.auikit.model.AUiMusicModel
import io.agora.auikit.model.AUiPlayStatus
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.service.IAUiJukeboxService
import io.agora.auikit.service.IAUiJukeboxService.AUiJukeboxRespDelegate
import io.agora.auikit.service.callback.AUiCallback
import io.agora.auikit.service.callback.AUiChooseSongListCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.callback.AUiMusicListCallback
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.song.SongAddReq
import io.agora.auikit.service.http.song.SongInterface
import io.agora.auikit.service.http.song.SongOwner
import io.agora.auikit.service.http.song.SongPinReq
import io.agora.auikit.service.http.song.SongPlayReq
import io.agora.auikit.service.http.song.SongRemoveReq
import io.agora.auikit.service.http.song.SongStopReq
import io.agora.auikit.service.ktv.KTVApi
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.service.rtm.AUiRtmMsgProxyDelegate
import io.agora.auikit.utils.DelegateHelper
import io.agora.rtc2.Constants
import retrofit2.Call
import retrofit2.Response

class AUiJukeboxServiceImpl constructor(
    private val channelName: String,
    private val rtmManager: AUiRtmManager,
    private val ktvApi: KTVApi
) : IAUiJukeboxService, AUiRtmMsgProxyDelegate {

    private val TAG: String = "Jukebox_LOG"
    private val kChooseSongKey = "song"

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
        .create()

    private val songInterface by lazy { HttpManager.getService(SongInterface::class.java) }

    private val delegateHelper = DelegateHelper<AUiJukeboxRespDelegate>()

    // 选歌列表
    private val chooseMusicList = mutableListOf<AUiChooseMusicModel>()

    init {
        rtmManager.subscribeMsg(channelName, kChooseSongKey, this)
    }

    override fun bindRespDelegate(delegate: AUiJukeboxRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: AUiJukeboxRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    // 获取歌曲列表
    override fun getMusicList(chartId: Int, page: Int, pageSize: Int, completion: AUiMusicListCallback?) {
        Log.d(TAG, "getMusicList call chartId:$chartId,page:$page,pageSize:$pageSize")
        val jsonOption = "{\"pitchType\":1,\"needLyric\":true}"
        ktvApi.searchMusicByMusicChartId(chartId,
            page,
            pageSize,
            jsonOption,
            onMusicCollectionResultListener = { requestId, status, p, size, total, list ->
                Log.d(TAG, "getMusicList call return chartId:$chartId,page:$page,pageSize:$pageSize")
                if (status != Constants.ERR_OK) {
                    completion?.onResult(null, null)
                    return@searchMusicByMusicChartId
                }
                val musicList = mutableListOf<AUiMusicModel>()
                list?.forEach {
                    val musicModel = AUiMusicModel().apply {
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
        keyword: String?, page: Int, pageSize: Int, completion: AUiMusicListCallback?
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
                val musicList = mutableListOf<AUiMusicModel>()
                list?.forEach {
                    val musicModel = AUiMusicModel().apply {
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
    override fun getAllChooseSongList(completion: AUiChooseSongListCallback?) {
        Log.d(TAG, "getAllChooseSongList call")
        rtmManager.getMetadata(channelName, completion = { rtmException, map ->
            if (rtmException != null) {
                completion?.onResult(AUiException(rtmException.code, rtmException.reason), null)
                return@getMetadata
            }
            val chooseSongStr = map?.get(kChooseSongKey)
            if (chooseSongStr.isNullOrEmpty()) {
                completion?.onResult(null, null)
                return@getMetadata
            }
            val chooseMusics: List<AUiChooseMusicModel> =
                gson.fromJson(chooseSongStr, object : TypeToken<List<AUiChooseMusicModel>>() {}.type) ?: mutableListOf()
            chooseMusicList.clear()
            chooseMusicList.addAll(chooseMusics)
            completion?.onResult(null, chooseMusics)
        })
    }

    // 点一首歌
    override fun chooseSong(song: AUiMusicModel, completion: AUiCallback?) {
        val chooseMusicModel = gson.fromJson(gson.toJson(song), AUiChooseMusicModel::class.java)
        chooseMusicModel.apply {
            owner = roomContext.currentUserInfo
            createAt = System.currentTimeMillis()
            pinAt = 0L
            status = AUiPlayStatus.idle
        }
        val songAddReq = SongAddReq(
            roomId = channelName,
            userId = roomContext.currentUserInfo.userId,
            songCode = chooseMusicModel.songCode,
            name = chooseMusicModel.name,
            singer = chooseMusicModel.singer,
            poster = chooseMusicModel.poster,
            releaseTime = chooseMusicModel.releaseTime,
            duration = chooseMusicModel.duration,
            musicUrl = chooseMusicModel.musicUrl ?: "",
            lrcUrl = chooseMusicModel.lrcUrl ?: "",
            owner = SongOwner(
                userId = roomContext.currentUserInfo.userId,
                userName = roomContext.currentUserInfo.userName,
                userAvatar = roomContext.currentUserInfo.userAvatar,
            )
        )
        songInterface.songAdd(songAddReq)
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (isNetSuccess(response)) {
                        val respBody = response.body() ?: return
                        if (isSuccess(respBody)) {
//                            delegateHelper.notifyDelegate { delegate: AUiJukeboxRespDelegate ->
//                                delegate.onAddChooseSong(chooseMusicModel)
//                            }
//                            chooseMusicList.add(chooseMusicModel)
                            completion?.onResult(null)
                        } else {
                            completion?.onResult(AUiException(respBody.code, respBody.message))
                        }
                    } else {
                        completion?.onResult(AUiException(response.code(), response.message()))
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    completion?.onResult(AUiException(-1, t.message))
                }
            })
    }

    // 移除一首自己点的歌
    override fun removeSong(songCode: String, completion: AUiCallback?) {
        val songRemoveReq = SongRemoveReq(channelName, songCode, roomContext.currentUserInfo.userId)
        songInterface.songRemove(songRemoveReq)
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (isNetSuccess(response)) {
                        val respBody = response.body() ?: return
                        if (isSuccess(respBody)) {
                            completion?.onResult(null)
                        } else {
                            completion?.onResult(AUiException(respBody.code, respBody.message))
                        }
                    } else {
                        completion?.onResult(AUiException(response.code(), response.message()))
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    completion?.onResult(AUiException(-1, t.message))
                }
            })
    }

    // 置顶歌曲
    override fun pingSong(songCode: String, completion: AUiCallback?) {
        val songPinReq = SongPinReq(channelName, songCode, roomContext.currentUserInfo.userId)
        songInterface.songPin(songPinReq)
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (isNetSuccess(response)) {
                        val respBody = response.body() ?: return
                        if (isSuccess(respBody)) {
                            completion?.onResult(null)
                        } else {
                            completion?.onResult(AUiException(respBody.code, respBody.message))
                        }
                    } else {
                        completion?.onResult(AUiException(response.code(), response.message()))
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    completion?.onResult(AUiException(-1, t.message))
                }
            })
    }

    // 更新播放状态
    override fun updatePlayStatus(songCode: String, @AUiPlayStatus playStatus: Int, completion: AUiCallback?) {
        if (playStatus == AUiPlayStatus.playing) {
            playSong(songCode, completion)
        } else if (playStatus == AUiPlayStatus.idle) {
            stopSong(songCode, completion)
        }
    }

    private fun playSong(songCode: String, completion: AUiCallback?) {
        val songPlayReq = SongPlayReq(channelName, songCode, roomContext.currentUserInfo.userId)
        songInterface.songPlay(songPlayReq)
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (isNetSuccess(response)) {
                        val respBody = response.body() ?: return
                        if (isSuccess(respBody)) {
                            completion?.onResult(null)
                        } else {
                            completion?.onResult(AUiException(respBody.code, respBody.message))
                        }
                    } else {
                        completion?.onResult(AUiException(response.code(), response.message()))
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    completion?.onResult(AUiException(-1, t.message))
                }
            })
    }

    private fun stopSong(songCode: String, completion: AUiCallback?) {
        val songStopReq = SongStopReq(channelName, songCode, roomContext.currentUserInfo.userId)
        songInterface.songStop(songStopReq)
            .enqueue(object : retrofit2.Callback<CommonResp<Any>> {
                override fun onResponse(call: Call<CommonResp<Any>>, response: Response<CommonResp<Any>>) {
                    if (isNetSuccess(response)) {
                        val respBody = response.body() ?: return
                        if (isSuccess(respBody)) {
                            completion?.onResult(null)
                        } else {
                            completion?.onResult(AUiException(respBody.code, respBody.message))
                        }
                    } else {
                        completion?.onResult(AUiException(response.code(), response.message()))
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    completion?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun getChannelName() = channelName

    private fun isSuccess(resp: CommonResp<Any>): Boolean {
        return resp.code == 0
    }

    private fun isNetSuccess(response: Response<*>): Boolean {
        return response.code() == 200
    }

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        if (key != kChooseSongKey) {
            return
        }
        Log.d(TAG, "channelName:$channelName,key:$key,value:$value")
        val changedSongs: List<AUiChooseMusicModel> =
            gson.fromJson(value as String, object : TypeToken<List<AUiChooseMusicModel>>() {}.type) ?: mutableListOf()
        this.chooseMusicList.clear()
        this.chooseMusicList.addAll(changedSongs)
        delegateHelper.notifyDelegate { delegate: AUiJukeboxRespDelegate ->
            delegate.onUpdateAllChooseSongs(this.chooseMusicList)
        }
    }
}