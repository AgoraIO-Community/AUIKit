package io.agora.auikit.ui.jukebox.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

import io.agora.auikit.ui.R;
import io.agora.auikit.ui.basic.AUIFrameLayout;
import io.agora.auikit.ui.basic.AUITabLayout;
import io.agora.auikit.ui.jukebox.AUIMusicInfo;
import io.agora.auikit.ui.jukebox.IAUIJukeboxView;

public class AUIJukeboxView extends AUIFrameLayout implements IAUIJukeboxView {

    private ViewPager2 viewPager;
    private AUITabLayout tlCategory;
    private ActionDelegate actionDelegate;
    private AUIJukeboxChooseView mChooseView;
    private AUIJukeboxChosenView mChosenView;
    private AUIJukeboxChooseView.AbsDataListAdapter<AUIMusicInfo> mChooseListAdapter;
    private AUIJukeboxChooseView.AbsDataListAdapter<AUIMusicInfo> mSearchListAdapter;
    private AUIJukeboxChosenView.AbsDataListAdapter<AUIMusicInfo> mChosenListAdapter;

    public AUIJukeboxView(@NonNull Context context) {
        this(context, null);
    }

    public AUIJukeboxView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AUIJukeboxView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.aui_jukebox_view, this);

        tlCategory = findViewById(R.id.jbtabLayout);
        viewPager = findViewById(R.id.viewPager);

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View contentView;
                if (viewType == 1) {
                    contentView = new AUIJukeboxChosenView(getContext());
                } else {
                    contentView = new AUIJukeboxChooseView(getContext());
                }
                contentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                return new RecyclerView.ViewHolder(contentView) {
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (position == 1) {
                    AUIJukeboxChosenView chosenView = (AUIJukeboxChosenView) holder.itemView;
                    refreshChosenViewLayout(chosenView);
                } else {
                    AUIJukeboxChooseView chooseView = (AUIJukeboxChooseView) holder.itemView;
                    refreshChooseViewLayout(chooseView);
                }
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        tlCategory.setOnTabSelectChangeListener((index, menuId, selected) -> {
            if(selected){
                viewPager.setCurrentItem(index);
            }
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tlCategory.selectTab(position);
            }
        });
    }

    @Override
    public void setActionDelegate(ActionDelegate delegate) {
        this.actionDelegate = delegate;
    }

    @Override
    public void setChooseSongCategories(List<String> categories) {
        if (mChosenView != null) {
            mChooseView.resetCategories(categories);
        }
    }

    @Override
    public void refreshChooseSongList(String category, List<AUIMusicInfo> songList) {
        if (mChooseView == null || mChooseListAdapter == null) {
            return;
        }
        if (mChooseView.getCategorySelected().equals(category)) {
            mChooseListAdapter.submitList(songList, () -> {
                mChooseView.setDataListRefreshComplete();
            });
        }
    }

    @Override
    public void loadMoreChooseSongList(String category, List<AUIMusicInfo> songList) {
        if (mChooseView == null || mChooseListAdapter == null) {
            return;
        }
        if (mChooseView.getCategorySelected().equals(category)) {
            ArrayList<AUIMusicInfo> list = new ArrayList<>(mChooseListAdapter.getCurrentList());
            list.addAll(songList);
            mChooseListAdapter.submitList(list, () -> {
                mChooseListAdapter.setLoadMoreComplete();
            });
        }
    }

    @Override
    public void setChosenSongList(List<AUIMusicInfo> songList) {
        if (mChosenView == null || mChosenListAdapter == null) {
            return;
        }
        if (songList == null || songList.isEmpty()) {
            mChosenListAdapter.submitList(new ArrayList<>());
            tlCategory.setTabDotNum(1, 0);
        } else {
            mChosenListAdapter.submitList(new ArrayList<>(songList));
            tlCategory.setTabDotNum(1, songList.size());
        }
    }

    @Override
    public void refreshSearchSongList(List<AUIMusicInfo> songList) {
        if (mChooseView == null || mSearchListAdapter == null) {
            return;
        }
        mSearchListAdapter.submitList(songList, () -> {
            mChooseView.setSearchListRefreshComplete();
        });
    }

    @Override
    public void loadMoreSearchSongList(List<AUIMusicInfo> songList) {
        if (mChooseView == null || mSearchListAdapter == null) {
            return;
        }
        ArrayList<AUIMusicInfo> list = new ArrayList<>(mSearchListAdapter.getCurrentList());
        list.addAll(songList);
        mSearchListAdapter.submitList(list, () -> {
            mSearchListAdapter.setLoadMoreComplete();
        });
    }

    // 点歌
    private void refreshChooseViewLayout(@NonNull AUIJukeboxChooseView chooseView) {
        mChooseView = chooseView;
        DiffUtil.ItemCallback<AUIMusicInfo> diffItemCallback = new DiffUtil.ItemCallback<AUIMusicInfo>() {
            @Override
            public boolean areItemsTheSame(@NonNull AUIMusicInfo oldItem, @NonNull AUIMusicInfo newItem) {
                return newItem.getSongCode().equals(oldItem.getSongCode());
            }

            @Override
            public boolean areContentsTheSame(@NonNull AUIMusicInfo oldItem, @NonNull AUIMusicInfo newItem) {
                return false;
            }
        };

        mChooseListAdapter = new AUIJukeboxChooseView.AbsDataListAdapter<AUIMusicInfo>(diffItemCallback) {
            @Override
            void onBindItemView(AUIJukeboxChooseItemView itemView, int position) {
                AUIMusicInfo musicModel = getItem(position);
                itemView.setSongName(musicModel.getName());
                itemView.setSingerName(musicModel.getSinger());
                itemView.setSongCover(musicModel.getPost());
                itemView.setOnChooseChangeListener(null);
                itemView.setChooseCheck(isSongChosen(musicModel));
                itemView.setOnChooseChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        buttonView.setClickable(false);
                        if (actionDelegate != null) {
                            actionDelegate.onSongChosen(musicModel);
                        }
                    }
                });
            }

            @Override
            void onLoadMore() {
                if (actionDelegate != null) {
                    actionDelegate.onChooseSongLoadMore(chooseView.getCategorySelected(), getItemCount());
                }
            }
        };
        chooseView.setDataListAdapter(mChooseListAdapter);
        chooseView.setDataListOnRefreshListener(() -> {
            if (actionDelegate != null) {
                actionDelegate.onChooseSongRefreshing(chooseView.getCategorySelected());
            }
        });
        mSearchListAdapter = new AUIJukeboxChooseView.AbsDataListAdapter<AUIMusicInfo>(diffItemCallback) {
            @Override
            void onBindItemView(AUIJukeboxChooseItemView itemView, int position) {
                AUIMusicInfo musicModel = getItem(position);
                itemView.setSongName(musicModel.getName());
                itemView.setSingerName(musicModel.getSinger());
                itemView.setSongCover(musicModel.getPost());
                itemView.setChooseCheck(isSongChosen(musicModel));
                itemView.setOnChooseChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        buttonView.setClickable(false);
                        if (actionDelegate != null) {
                            actionDelegate.onSongChosen(musicModel);
                        }
                    }
                });
            }

            @Override
            void onLoadMore() {
                if (actionDelegate != null) {
                    actionDelegate.onSearchSongLoadMore(chooseView.getSearchContent(), getItemCount());
                }
            }
        };
        chooseView.setSearchListAdapter(mSearchListAdapter);
        chooseView.setOnSearchListener(content -> {
            if (actionDelegate != null) {
                actionDelegate.onSearchSongRefreshing(chooseView.getSearchContent());
            }
        });
        chooseView.setSearchListOnRefreshListener(() -> {
            if (actionDelegate != null) {
                actionDelegate.onSearchSongRefreshing(chooseView.getSearchContent());
            }
        });

        chooseView.setCategoryVisible(true);
        chooseView.setOnCategoryTabChangeListener(index -> {
            mChooseListAdapter.submitList(new ArrayList<>());
            if (actionDelegate != null) {
                actionDelegate.onChooseSongRefreshing(chooseView.getCategorySelected());
            }
        });
    }

    private boolean isSongChosen(AUIMusicInfo musicModel) {
        if (mChosenListAdapter == null) {
            return false;
        }
        List<AUIMusicInfo> chosenList = mChosenListAdapter.getCurrentList();
        for (AUIMusicInfo item : chosenList) {
            if (item.getSongCode().equals(musicModel.getSongCode())) {
                return true;
            }
        }
        return false;
    }

    // 已点
    private void refreshChosenViewLayout(@NonNull AUIJukeboxChosenView chosenView) {
        mChosenView = chosenView;

        DiffUtil.ItemCallback<AUIMusicInfo> diffItemCallback = new DiffUtil.ItemCallback<AUIMusicInfo>() {
            @Override
            public boolean areItemsTheSame(@NonNull AUIMusicInfo oldItem, @NonNull AUIMusicInfo newItem) {
                return newItem.getSinger().equals(oldItem.getSongCode());
            }

            @Override
            public boolean areContentsTheSame(@NonNull AUIMusicInfo oldItem, @NonNull AUIMusicInfo newItem) {
                return false;
            }
        };
        mChosenListAdapter = new AUIJukeboxChosenView.AbsDataListAdapter<AUIMusicInfo>(diffItemCallback) {

            @Override
            void onBindItemView(@NonNull AUIJukeboxChosenItemView itemView, int position) {
                AUIMusicInfo item = getItem(position);
                if (actionDelegate != null) {
                    actionDelegate.onChosenSongItemUpdating(itemView, position, item);
                }
                itemView.setSongName(item.getName());
                itemView.setSingerName(item.getSinger());
                itemView.setSongCover(item.getPost());
                itemView.setOrder((position + 1) + "");

                itemView.setOnDeleteClickListener(v -> {
                    if (actionDelegate != null) {
                        actionDelegate.onSongDeleted(item);
                    }
                });
                itemView.setOnTopClickListener(v -> {
                    if (actionDelegate != null) {
                        actionDelegate.onSongPinged(item);
                    }
                });
                itemView.setOnPlayingClickListener(v -> {
                    if (actionDelegate != null) {
                        actionDelegate.onSongSwitched(item);
                    }
                });
            }
        };
        chosenView.setDataListAdapter(mChosenListAdapter);
    }

}
