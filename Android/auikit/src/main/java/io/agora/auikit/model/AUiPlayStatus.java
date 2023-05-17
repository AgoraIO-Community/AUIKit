package io.agora.auikit.model;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IntDef({
        AUiPlayStatus.idle,
        AUiPlayStatus.playing,
})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD})
public @interface AUiPlayStatus {
    int idle = 0; // 未播放
    int playing = 1; // 播放中
}
