package io.agora.auikit.ui.jukebox;

import androidx.annotation.NonNull;

import java.util.List;

import io.agora.auikit.model.AUIChooseMusicModel;
import io.agora.auikit.model.AUIMusicModel;


public interface IAUIJukeboxView {

    /**
     * 设置事件回调
     *
     * @param delegate 事件回调
     */
    void setActionDelegate(ActionDelegate delegate);

    /**
     * 设置选歌列表分类
     *
     * @param categories 选歌列表分类
     */
    void setChooseSongCategories(List<String> categories);

    /**
     * 刷新选歌列表，会删除原先的所有数据，在{@link ActionDelegate#onChooseSongRefreshing(String)}里获取数据成功后调用
     *
     * @param category 选歌列表分类
     * @param songList 选歌列表数据
     */
    void refreshChooseSongList(String category, List<AUIMusicModel> songList);

    /**
     * 添加选歌列表数据，在{@link ActionDelegate#onChooseSongLoadMore(String, int)}里获取数据成功后调用
     *
     * @param category 选歌列表分类
     * @param songList 选歌列表数据
     */
    void loadMoreChooseSongList(String category, List<AUIMusicModel> songList);

    /**
     * 设置选中歌词列表
     *
     * @param songList 选中歌词列表
     */
    void setChosenSongList(List<AUIChooseMusicModel> songList);

    /**
     * 刷新搜索歌词列表，会删除原先的所有数据，在{@link ActionDelegate#onSearchSongRefreshing(String)} 里获取数据成功后调用
     *
     * @param songList 搜索歌词列表数据
     */
    void refreshSearchSongList(List<AUIMusicModel> songList);

    /**
     * 添加搜索歌词列表数据，在{@link ActionDelegate#onSearchSongLoadMore(String, int)} 里获取数据成功后调用
     *
     * @param songList 搜索歌词列表数据
     */
    void loadMoreSearchSongList(List<AUIMusicModel> songList);

    interface ActionDelegate {

        /**
         * 当选歌列表分类tab切换或者下拉刷新时调用
         *
         * @param category 分类名称
         */
        void onChooseSongRefreshing(String category);

        /**
         * 当选歌列表上滑到底部加载更多时调用
         *
         * @param category 分类名称
         * @param startIndex 下一次请求数据的开始位置
         */
        void onChooseSongLoadMore(String category, int startIndex);

        /**
         * 当已选歌词列表item视图更新时调用
         *
         * @param itemView 歌词item视图，可用于根据业务逻辑显示相应的按钮
         * @param position item位置
         * @param model 已选歌词信息
         */
        void onChosenSongItemUpdating(IAUIJukeboxChosenItemView itemView, int position, @NonNull AUIChooseMusicModel model);

        /**
         * 当搜索歌词列表开始搜索或者下拉刷新时调用
         *
         * @param condition 搜索条件
         */
        void onSearchSongRefreshing(String condition);

        /**
         * 当搜索歌词列表上滑到底部加载更多时调用
         *
         * @param condition 搜索条件
         * @param startIndex 下一次请求数据的开始位置
         */
        void onSearchSongLoadMore(String condition, int startIndex);

        /**
         * 选歌时调用
         *
         * @param song 歌曲信息
         */
        void onSongChosen(AUIMusicModel song);

        /**
         * 歌曲置顶时调用
         *
         * @param song 已选歌曲信息
         */
        void onSongPinged(AUIChooseMusicModel song);

        /**
         * 歌曲删除时调用
         *
         * @param song 已选歌曲信息
         */
        void onSongDeleted(AUIChooseMusicModel song);

        /**
         * 歌曲切歌时调用
         *
         * @param song 已选歌曲信息
         */
        void onSongSwitched(AUIChooseMusicModel song);

    }
}
