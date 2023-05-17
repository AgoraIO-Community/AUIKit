package io.agora.auikit.ui.basic;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.auikit.R;

public class AUIRecyclerView extends RecyclerView {
    private AUISpaceItemDecoration spaceItemDecoration;

    public AUIRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public AUIRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AUIRecyclerView, defStyleAttr, 0);
        int spaceHorizontal = typedArray.getDimensionPixelOffset(R.styleable.AUIRecyclerView_aui_spaceHorizontal, 0);
        int spaceVertical = typedArray.getDimensionPixelOffset(R.styleable.AUIRecyclerView_aui_spaceVertical, 0);
        int orientation = typedArray.getInt(R.styleable.AUIRecyclerView_android_orientation, 0); // 0: h; 1: v
        Drawable listDivider = typedArray.getDrawable(R.styleable.AUIRecyclerView_aui_listDivider);
        typedArray.recycle();

        spaceItemDecoration = new AUISpaceItemDecoration(spaceHorizontal, spaceVertical);
        addItemDecoration(spaceItemDecoration);

        if (listDivider != null) {
            DividerItemDecoration divider = new DividerItemDecoration(context, orientation);
            divider.setDrawable(listDivider);
            addItemDecoration(divider);
        }
    }

    public void setSpaceHorizontal(int space) {
        if (spaceItemDecoration != null && spaceItemDecoration.getSpaceHorizontal() == space) {
            return;
        }
        if (spaceItemDecoration != null) {
            removeItemDecoration(spaceItemDecoration);
            spaceItemDecoration = new AUISpaceItemDecoration(space, spaceItemDecoration.getSpaceVertical());
        } else {
            spaceItemDecoration = new AUISpaceItemDecoration(space, 0);
        }
        addItemDecoration(spaceItemDecoration);
    }

    public void setSpaceVertical(int space) {
        if (spaceItemDecoration != null && spaceItemDecoration.getSpaceVertical() == space) {
            return;
        }
        if (spaceItemDecoration != null) {
            removeItemDecoration(spaceItemDecoration);
            spaceItemDecoration = new AUISpaceItemDecoration(spaceItemDecoration.getSpaceHorizontal(), space);
        } else {
            spaceItemDecoration = new AUISpaceItemDecoration(0, space);
        }
        addItemDecoration(spaceItemDecoration);
    }

    public int getSpaceHorizontal() {
        if (spaceItemDecoration != null) {
            return spaceItemDecoration.getSpaceHorizontal();
        }
        return 0;
    }

    public int getSpaceVertical() {
        if (spaceItemDecoration != null) {
            return spaceItemDecoration.getSpaceVertical();
        }
        return 0;
    }


}
