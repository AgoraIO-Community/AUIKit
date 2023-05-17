package io.agora.auikit.model;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IntDef({
        AUiMicSeatStatus.idle,
        AUiMicSeatStatus.used,
        AUiMicSeatStatus.locked,
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AUiMicSeatStatus {
    int idle = 0; // 空闲
    int used = 1; // 使用中
    int locked = 2; // 锁定
}
