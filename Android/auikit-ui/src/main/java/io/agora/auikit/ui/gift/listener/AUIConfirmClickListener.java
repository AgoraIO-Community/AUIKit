package io.agora.auikit.ui.gift.listener;

import android.view.View;

import io.agora.auikit.model.AUIGiftEntity;

public interface AUIConfirmClickListener {
    void sendGift(View view, AUIGiftEntity bean);
}
