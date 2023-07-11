package io.agora.auikit.ui.micseats;

public interface IMicSeatsView {

    /**
     * 设置麦位数，默认8个
     * @param count 麦位数
     */
    void setMicSeatCount(int count);

    /**
     * 获取麦位View列表，可用于操作麦位ui
     *
     * @return 麦位View列表
     */
    IMicSeatItemView[] getMicSeatItemViewList();

    /**
     * 设置点击事件回调
     *
     * @param actionDelegate 事件回调
     */
    void setMicSeatActionDelegate(ActionDelegate actionDelegate);

    /**
     * 开启水波纹动画
     */
    default void startRippleAnimation(int index){}

    /**
     * 结束水波纹动画
     */
    default void stopRippleAnimation(int index){}

    interface ActionDelegate {

        /**
         * 点击麦位时触发
         *
         * @param index 麦位位置，从0开始
         * @param dialogView 麦位弹窗view，可以用来根据业务逻辑修改弹窗内容
         * @return 是否弹窗
         */
        boolean onClickSeat(int index, IMicSeatDialogView dialogView);

        /**
         * 当点击上麦时触发
         *
         * @param index 麦位位置，从0开始
         */
        void onClickEnterSeat(int index);

        /**
         * 当点击下麦时触发
         *
         * @param index 麦位位置，从0开始
         */
        void onClickLeaveSeat(int index);

        /**
         * 当点击踢人时触发
         *
         * @param index 麦位位置，从0开始
         */
        void onClickKickSeat(int index);

        /**
         * 当点击封麦/取消封麦时触发
         *
         * @param index 麦位位置，从0开始
         * @param isClose 是否封麦
         */
        void onClickCloseSeat(int index, boolean isClose);

        /**
         * 当点击静音/取消静音时触发
         *
         * @param index 麦位位置，从0开始
         * @param mute 是否静音
         */
        void onClickMuteAudio(int index, boolean mute);

        /**
         * 当点击禁视频/取消禁视频时触发
         *
         * @param index 麦位位置，从0开始
         * @param mute 是否禁视频
         */
        void onClickMuteVideo(int index, boolean mute);

        void onClickInvited(int index);
    }

}
