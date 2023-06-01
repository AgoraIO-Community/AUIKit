package io.agora.auikit.ui.jukebox.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import io.agora.auikit.R;

public class AUIJukeboxChosenView extends FrameLayout {

    private RecyclerView rvDataList;

    public AUIJukeboxChosenView(@NonNull Context context) {
        this(context, null);
    }

    public AUIJukeboxChosenView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIJukeboxChosenView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.aui_jukebox_chosen_view, this);

        rvDataList = findViewById(R.id.recyclerView);
    }

    public <Data> void setDataListAdapter(AbsDataListAdapter<Data> adapter) {
        rvDataList.setAdapter(adapter);
    }


    public static abstract class AbsDataListAdapter<Data> extends ListAdapter<Data, RecyclerView.ViewHolder> {

        protected AbsDataListAdapter(@NonNull DiffUtil.ItemCallback<Data> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public final RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(new AUIJukeboxChosenItemView(parent.getContext())) {
            };
        }

        @Override
        public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            AUIJukeboxChosenItemView itemView = (AUIJukeboxChosenItemView) holder.itemView;
            onBindItemView(itemView, position);
        }

        abstract void onBindItemView(@NonNull AUIJukeboxChosenItemView itemView, int position);

    }
}
