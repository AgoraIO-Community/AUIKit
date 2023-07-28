package io.agora.auikit.ui.jukebox.impl;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

import io.agora.auikit.ui.R;
import io.agora.auikit.ui.basic.AUIEditText;
import io.agora.auikit.ui.basic.AUITabLayout;

public class AUIJukeboxChooseView extends FrameLayout {

    private AUITabLayout tlCategory;
    private RecyclerView rvDataList;
    private RecyclerView rvSearchList;
    private AUIEditText etSearch;
    private OnSearchListener onSearchListener;
    private SwipeRefreshLayout srlDataList;
    private SwipeRefreshLayout srlSearchList;
    private boolean categoryVisible;
    private OnCategoryTabChangeListener onCategoryTabChangeListener;

    public AUIJukeboxChooseView(@NonNull Context context) {
        this(context, null);
    }

    public AUIJukeboxChooseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIJukeboxChooseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.aui_jukebox_choose_view, this);

        rvDataList = findViewById(R.id.recyclerView);
        rvSearchList = findViewById(R.id.recyclerView_search);
        tlCategory = findViewById(R.id.tl_category);
        etSearch = findViewById(R.id.et_search);
        srlDataList = findViewById(R.id.srl_list);
        srlSearchList = findViewById(R.id.srl_search_list);

        etSearch.setRightIconVisibility(View.GONE);
        etSearch.setRightIconClickListener(v -> {
            etSearch.setText("");
            if (onSearchListener != null) {
                srlSearchList.setVisibility(View.GONE);
                srlDataList.setVisibility(View.VISIBLE);
                if (categoryVisible) {
                    tlCategory.setVisibility(View.VISIBLE);
                }
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etSearch.setRightIconVisibility(TextUtils.isEmpty(etSearch.getText().toString()) ? View.GONE : View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String content = etSearch.getText().toString();
                if (onSearchListener != null && !TextUtils.isEmpty(content)) {
                    srlSearchList.setVisibility(View.VISIBLE);
                    srlDataList.setVisibility(View.GONE);
                    if (categoryVisible) {
                        tlCategory.setVisibility(View.GONE);
                    }
                    onSearchListener.onSearchingStart(content);
                }
            }
            return false;
        });
        tlCategory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                rvDataList.scrollToPosition(0);
                if(onCategoryTabChangeListener != null){
                    onCategoryTabChangeListener.onCategoryTabChanged(tab.getPosition());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void setOnSearchListener(OnSearchListener listener) {
        this.onSearchListener = listener;
    }

    public void resetCategories(@Nullable List<String> categories) {
        tlCategory.removeAllTabs();
        if (categories != null) {
            for (String category : categories) {
                tlCategory.addTab(tlCategory.newTab().setText(category));
            }
        }
    }

    public void setCategoryVisible(boolean visible) {
        categoryVisible = visible;
        tlCategory.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public int getCategorySelectedPosition() {
        return tlCategory.getSelectedTabPosition();
    }

    public String getCategorySelected() {
        return tlCategory.getTabAt(tlCategory.getSelectedTabPosition()).getText().toString();
    }

    public <Data> void setDataListAdapter(@NonNull AbsDataListAdapter<Data> adapter) {
        rvDataList.setAdapter(adapter);
    }

    public void setDataListOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        srlDataList.setOnRefreshListener(listener);
    }

    public void setDataListRefreshComplete() {
        srlDataList.setRefreshing(false);
    }

    public <Data> void setSearchListAdapter(@NonNull AbsDataListAdapter<Data> adapter) {
        rvSearchList.setAdapter(adapter);
    }

    public void setSearchListOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        srlSearchList.setOnRefreshListener(listener);
    }

    public void setSearchListRefreshComplete() {
        srlSearchList.setRefreshing(false);
    }

    public String getSearchContent() {
        return etSearch.getText().toString();
    }

    public void setOnCategoryTabChangeListener(OnCategoryTabChangeListener onCategoryTabChangeListener) {
        this.onCategoryTabChangeListener = onCategoryTabChangeListener;
    }

    public interface OnSearchListener {
        void onSearchingStart(String content);
    }

    public interface OnCategoryTabChangeListener{
        void onCategoryTabChanged(int index);
    }

    public abstract static class AbsDataListAdapter<Data> extends ListAdapter<Data, RecyclerView.ViewHolder> {
        private volatile boolean isLoadingMore = false;

        protected AbsDataListAdapter(@NonNull DiffUtil.ItemCallback<Data> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public final RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RecyclerView.ViewHolder(new AUIJukeboxChooseItemView(parent.getContext())) {
            };
        }

        @Override
        public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            onBindItemView((AUIJukeboxChooseItemView) holder.itemView, position);
            if (getItemCount() > 0 && position == getItemCount() - 1 && !isLoadingMore) {
                isLoadingMore = true;
                onLoadMore();
            }
        }


        abstract void onBindItemView(AUIJukeboxChooseItemView itemView, int position);

        abstract void onLoadMore();

        public void setLoadMoreComplete(){
            isLoadingMore = false;
        }

    }
}
