package io.agora.auikit.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class AUIEffectVoiceInfo {

    private int id;

    private int effectId;

    private @DrawableRes int resId;
    private @StringRes int name;

    public AUIEffectVoiceInfo(int id, int resId, int name) {
        this.id = id;
        this.resId = resId;
        this.name = name;
    }

    public AUIEffectVoiceInfo(int id, int effectId, int resId, int name) {
        this.id = id;
        this.effectId = effectId;
        this.resId = resId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public AUIEffectVoiceInfo setId(int id) {
        this.id = id;
        return this;
    }

    public int getResId() {
        return resId;
    }

    public AUIEffectVoiceInfo setEffectId(int effectId) {
        this.effectId = effectId;
        return this;
    }

    public int getEffectId() {
        return effectId;
    }

    public AUIEffectVoiceInfo setResId(int resId) {
        this.resId = resId;
        return this;
    }

    public int getTitle() {
        return name;
    }

    public AUIEffectVoiceInfo setTitle(int name) {
        this.name = name;
        return this;
    }
}
