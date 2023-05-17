package io.agora.auikit.service.callback;


import androidx.annotation.Nullable;

public interface AUiCallback {

    /**
     * @param error null: success, notNull: fail
     */
    void onResult(@Nullable AUiException error);

}
