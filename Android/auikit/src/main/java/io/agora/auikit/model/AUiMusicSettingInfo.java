package io.agora.auikit.model;

/**
 * @author create by zhangwei03
 */
public class AUiMusicSettingInfo {
    // 耳返
    private boolean isEar;
    // 人声音量
    private int signalVolume;
    // 音乐音量
    private int musicVolume;
    // 升降调
    private int pitch;
    // 音效
    private int effectId;

    public AUiMusicSettingInfo() {
    }

    public AUiMusicSettingInfo(boolean isEar, int signalVolume, int musicVolume, int pitch, int effectId) {
        this.isEar = isEar;
        this.signalVolume = signalVolume;
        this.musicVolume = musicVolume;
        this.pitch = pitch;
        this.effectId = effectId;
    }

    public boolean isEar() {
        return isEar;
    }

    public AUiMusicSettingInfo setEar(boolean ear) {
        isEar = ear;
        return this;
    }

    public int getSignalVolume() {
        return signalVolume;
    }

    public AUiMusicSettingInfo setSignalVolume(int signalVolume) {
        this.signalVolume = signalVolume;
        return this;
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public AUiMusicSettingInfo setMusicVolume(int musicVolume) {
        this.musicVolume = musicVolume;
        return this;
    }

    public int getPitch() {
        return pitch;
    }

    public AUiMusicSettingInfo setPitch(int pitch) {
        this.pitch = pitch;
        return this;
    }

    public int getEffectId() {
        return effectId;
    }

    public AUiMusicSettingInfo setEffectId(int effectId) {
        this.effectId = effectId;
        return this;
    }
}
