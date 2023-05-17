package io.agora.auikit.service.callback;

public interface AUiMusicLoadStateCallback {
    /**
     * 音乐加载成功
     * @param songCode 歌曲编码， 和你loadMusic传入的songCode一致
     * @param lyricUrl 歌词地址
     */
    void onMusicLoadSuccess(Long songCode, String lyricUrl);

    /**
     * 音乐加载失败
     * @param reason 歌曲加载失败的原因
     */
    void onMusicLoadFail(Long songCode, int reason);

    /**
     * 音乐加载进度
     * @param songCode 歌曲编码
     * @param percent 歌曲加载进度
     * @param status 歌曲加载的状态
     * @param msg
     * @param lyricUrl
     */
    void onMusicLoadProgress(Long songCode, int percent, int status, String msg, String lyricUrl);
}