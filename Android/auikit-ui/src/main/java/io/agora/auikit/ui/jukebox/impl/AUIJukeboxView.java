package io.agora.auikit.ui.jukebox.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.agora.auikit.model.AUIChooseMusicModel;
import io.agora.auikit.model.AUIMusicModel;
import io.agora.auikit.model.AUIPlayStatus;
import io.agora.auikit.ui.R;
import io.agora.auikit.ui.basic.AUIFrameLayout;
import io.agora.auikit.ui.jukebox.IAUIJukeboxView;

public class AUIJukeboxView extends AUIFrameLayout implements IAUIJukeboxView {

    private ViewPager2 viewPager;
    private TextView tvNumTag;
    private TabLayout tlCategory;
    private ActionDelegate actionDelegate;
    private AUIJukeboxChooseView mChooseView;
    private AUIJukeboxChosenView mChosenView;
    private AUIJukeboxChooseView.AbsDataListAdapter<AUIMusicModel> mChooseListAdapter;
    private AUIJukeboxChooseView.AbsDataListAdapter<AUIMusicModel> mSearchListAdapter;
    private AUIJukeboxChosenView.AbsDataListAdapter<AUIChooseMusicModel> mChosenListAdapter;

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

        tvNumTag = findViewById(R.id.tvNumTag);
        tvNumTag.setVisibility(View.GONE);

        tlCategory = findViewById(R.id.tabLayout);
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

        tlCategory.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tlCategory.selectTab(tlCategory.getTabAt(position));
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
    public void refreshChooseSongList(String category, List<AUIMusicModel> songList) {
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
    public void loadMoreChooseSongList(String category, List<AUIMusicModel> songList) {
        if (mChooseView == null || mChooseListAdapter == null) {
            return;
        }
        if (mChooseView.getCategorySelected().equals(category)) {
            ArrayList<AUIMusicModel> list = new ArrayList<>(mChooseListAdapter.getCurrentList());
            list.addAll(songList);
            mChooseListAdapter.submitList(list, () -> {
                mChooseListAdapter.setLoadMoreComplete();
            });
        }
    }

    @Override
    public void setChosenSongList(List<AUIChooseMusicModel> songList) {
        if (mChosenView == null || mChosenListAdapter == null) {
            return;
        }
        if (songList == null || songList.isEmpty()) {
            tvNumTag.setVisibility(GONE);
            mChosenListAdapter.submitList(new ArrayList<>());
        } else {
            tvNumTag.setVisibility(View.VISIBLE);
            tvNumTag.setText(Math.min(99, songList.size()) + "");
            mChosenListAdapter.submitList(new ArrayList<>(songList));
        }
    }

    @Override
    public void refreshSearchSongList(List<AUIMusicModel> songList) {
        if (mChooseView == null || mSearchListAdapter == null) {
            return;
        }
        mSearchListAdapter.submitList(songList, () -> {
            mChooseView.setSearchListRefreshComplete();
        });
    }

    @Override
    public void loadMoreSearchSongList(List<AUIMusicModel> songList) {
        if (mChooseView == null || mSearchListAdapter == null) {
            return;
        }
        ArrayList<AUIMusicModel> list = new ArrayList<>(mSearchListAdapter.getCurrentList());
        list.addAll(songList);
        mSearchListAdapter.submitList(list, () -> {
            mSearchListAdapter.setLoadMoreComplete();
        });
    }

    // 点歌
    private void refreshChooseViewLayout(@NonNull AUIJukeboxChooseView chooseView) {
        mChooseView = chooseView;
        DiffUtil.ItemCallback<AUIMusicModel> diffItemCallback = new DiffUtil.ItemCallback<AUIMusicModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull AUIMusicModel oldItem, @NonNull AUIMusicModel newItem) {
                return newItem.songCode.equals(oldItem.songCode);
            }

            @Override
            public boolean areContentsTheSame(@NonNull AUIMusicModel oldItem, @NonNull AUIMusicModel newItem) {
                return false;
            }
        };

        mChooseListAdapter = new AUIJukeboxChooseView.AbsDataListAdapter<AUIMusicModel>(diffItemCallback) {
            @Override
            void onBindItemView(AUIJukeboxChooseItemView itemView, int position) {
                AUIMusicModel musicModel = getItem(position);
                itemView.setSongName(musicModel.name);
                itemView.setSingerName(musicModel.singer);
                itemView.setSongCover(musicModel.poster);
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
        mSearchListAdapter = new AUIJukeboxChooseView.AbsDataListAdapter<AUIMusicModel>(diffItemCallback) {
            @Override
            void onBindItemView(AUIJukeboxChooseItemView itemView, int position) {
                AUIMusicModel musicModel = getItem(position);
                itemView.setSongName(musicModel.name);
                itemView.setSingerName(musicModel.singer);
                itemView.setSongCover(musicModel.poster);
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
            if (actionDelegate != null) {
                actionDelegate.onChooseSongRefreshing(chooseView.getCategorySelected());
            }
        });
    }

    private boolean isSongChosen(AUIMusicModel musicModel) {
        if (mChosenListAdapter == null) {
            return false;
        }
        List<AUIChooseMusicModel> chosenList = mChosenListAdapter.getCurrentList();
        for (AUIChooseMusicModel item : chosenList) {
            if (item.songCode.equals(musicModel.songCode)) {
                return true;
            }
        }
        return false;
    }

    // 已点
    private void refreshChosenViewLayout(@NonNull AUIJukeboxChosenView chosenView) {
        mChosenView = chosenView;

        DiffUtil.ItemCallback<AUIChooseMusicModel> diffItemCallback = new DiffUtil.ItemCallback<AUIChooseMusicModel>() {
            @Override
            public boolean areItemsTheSame(@NonNull AUIChooseMusicModel oldItem, @NonNull AUIChooseMusicModel newItem) {
                return newItem.songCode.equals(oldItem.songCode);
            }

            @Override
            public boolean areContentsTheSame(@NonNull AUIChooseMusicModel oldItem, @NonNull AUIChooseMusicModel newItem) {
                return false;
            }
        };
        mChosenListAdapter = new AUIJukeboxChosenView.AbsDataListAdapter<AUIChooseMusicModel>(diffItemCallback) {

            @Override
            void onBindItemView(@NonNull AUIJukeboxChosenItemView itemView, int position) {
                AUIChooseMusicModel item = getItem(position);
                if (actionDelegate != null) {
                    actionDelegate.onChosenSongItemUpdating(itemView, position, item);
                }
                itemView.setSongName(item.name);
                itemView.setSingerName(item.singer);
                itemView.setSongCover(item.poster);
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

    private void sortChooseSongList(List<AUIChooseMusicModel> songList) {
        Collections.sort(songList, (model1, model2) -> {
            if (model1.status == AUIPlayStatus.playing) {
                return -1;
            } else if (model2.status == AUIPlayStatus.playing) {
                return 1;
            }
            if (model1.pinAt < 1 && model2.pinAt < 1) {
                return model1.createAt - model2.createAt < 0 ? -1 : 1;
            }
            return model1.pinAt - model2.pinAt > 0 ? 1 : -1;
        });
    }
}
