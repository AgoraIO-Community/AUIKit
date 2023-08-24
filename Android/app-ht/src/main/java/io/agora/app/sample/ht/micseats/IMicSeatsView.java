package io.agora.app.sample.ht.micseats;


/**
 * 麦位列表View
 */
public interface IMicSeatsView {

    /**
     * 设置麦位数，默认8个
     *
     * @param count 麦位数
     * @return 所有麦位，可用于初始化
     */
    IMicSeatItemView[] setMicSeatCount(int count);

    /**
     * 获取麦位View，可用于操作麦位ui
     *
     * @return 麦位View
     */
    IMicSeatItemView findMicSeatItemView(int userId);

    /**
     * 上麦
     *
     * @param userId 用户id
     * @param seatIndex 麦位位置，-1时会分配一个空麦位，>=0时如果该麦位为空则会使用该麦位
     * @return 麦位View
     */
    IMicSeatItemView upMicSeat(int userId, int seatIndex);

    /**
     * 下麦
     *
     * @param userId 用户id
     * @return 麦位View
     */
    IMicSeatItemView downMicSeat(int userId);

    /**
     * 设置点击事件回调
     *
     * @param actionDelegate 事件回调
     */
    void setMicSeatActionDelegate(ActionDelegate actionDelegate);

    /**
     * 事件回调
     */
    interface ActionDelegate {

        /**
         * 点击麦位时触发
         *
         * @param userId 用户id，如果该麦位没有用户则返回 -1
         * @param itemView 麦位View
         */
        void onClickSeat(int userId, IMicSeatItemView itemView);

    }

}
