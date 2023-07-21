package io.agora.auikit.service.callback;


import androidx.annotation.Nullable;

public interface AUICallback {

    /**
     * @param error null: success, notNull: fail
     */
    void onResult(@Nullable AUIException error);

}
