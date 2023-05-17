package io.agora.auikit.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DelegateHelper<Delegate> {

    private final List<Delegate> delegateList = Collections.synchronizedList(new ArrayList<>());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public void bindDelegate(@Nullable Delegate delegate) {
        if (delegate == null) {
            return;
        }
        delegateList.add(delegate);
    }

    public void unBindDelegate(@Nullable Delegate delegate) {
        if (delegate == null) {
            return;
        }
        delegateList.remove(delegate);
    }

    public void unBindAll() {
        delegateList.clear();
        mainHandler.removeCallbacksAndMessages(null);
    }

    public void notifyDelegate(@NonNull DelegateRunnable<Delegate> runnable) {
        for (Delegate delegate : delegateList) {
            if (mainHandler.getLooper().getThread() != Thread.currentThread()) {
                mainHandler.post(() -> runnable.run(delegate));
            } else {
                runnable.run(delegate);
            }
        }
    }

    public interface DelegateRunnable<Delegate> {
        void run(Delegate delegate);
    }

}
