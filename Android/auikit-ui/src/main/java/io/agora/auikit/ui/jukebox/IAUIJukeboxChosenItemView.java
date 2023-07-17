package io.agora.auikit.ui.jukebox;

public interface IAUIJukeboxChosenItemView {

    /** 设置视图状态
     *
     * @param isCurrent 该条目是顺序首尾
     * @param ctrlAble 是否可以进行控制（切歌/置顶）
     * @param deleteAble 是否可以进行删除
     */
    void setViewStatus(boolean isCurrent, boolean ctrlAble, boolean deleteAble);
}
