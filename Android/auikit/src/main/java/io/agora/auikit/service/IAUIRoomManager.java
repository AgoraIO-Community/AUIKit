package io.agora.auikit.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.auikit.model.AUICreateRoomInfo;
import io.agora.auikit.model.AUIRoomInfo;
import io.agora.auikit.service.callback.AUICallback;
import io.agora.auikit.service.callback.AUICreateRoomCallback;
import io.agora.auikit.service.callback.AUIRoomListCallback;

/**
 * 房间Service抽象协议
 */
public interface IAUIRoomManager extends IAUICommonService<IAUIRoomManager.AUIRoomManagerRespDelegate> {
    /**
     * 创建房间（房主调用），若房间不存在，系统将自动创建一个新房间
     *
     * @param createRoomInfo 房间内信息
     * @param callback       成功/失败回调
     */
    void createRoom(@NonNull AUICreateRoomInfo createRoomInfo, @Nullable AUICreateRoomCallback callback);

    /**
     * 销毁房间（房主调用）
     *
     * @param roomId   房间唯一id
     * @param callback 成功/失败回调
     */
    void destroyRoom(@NonNull String roomId, @Nullable AUICallback callback);

    /**
     * 进入房间（听众调用）
     *
     * @param roomId   房间唯一id
     * @param callback 成功/失败回调
     */
    void enterRoom(@NonNull String roomId, @NonNull String token, @Nullable AUICallback callback);

    /**
     * 退出房间（听众调用）
     *
     * @param roomId   房间唯一id
     * @param callback 成功/失败回调
     */
    void exitRoom(@NonNull String roomId, @Nullable AUICallback callback);

    /**
     * 获取指定房间id列表的详细信息，如果房间id列表为空，则获取所有房间的信息
     *
     * @param lastCreateTime 最后1条数据的创建时间, 返回数据list的createTime字段值，如果为空, 默认会设置为服务器当前时间戳
     * @param pageSize       分页大小
     * @param callback       成功/失败回调
     */
    void getRoomInfoList(@Nullable Long lastCreateTime, int pageSize, @Nullable AUIRoomListCallback callback);

    /**
     * 更新房间公告
     * @param roomId    房间id
     * @param content   公告信息
     * @param callback  操作完成回调
     */
    void updateAnnouncementInfo(String roomId,String content,AUICallback callback);

    /**
     * 把用户提出房间
     * @param roomId    房间id
     * @param userId    需要踢出房间的用户id
     * @param callback  操作完成回调
     */
    void kickUser(String roomId,int userId,AUICallback callback);

    interface AUIRoomManagerRespDelegate {

        /**
         * 房间被销毁的回调
         *
         * @param roomId 房间唯一id
         */
        default void onRoomDestroy(@NonNull String roomId) {

        }

        /**
         * 房间信息变更回调
         *
         * @param roomId   房间唯一id
         * @param roomInfo 房间信息
         */
        default void onRoomInfoChange(@NonNull String roomId, @NonNull AUIRoomInfo roomInfo) {

        }

        /**
         * 房间公告被更新
         * @param roomId    房间id
         * @param content   公告信息
         */
        default void onAnnouncementDidChange(String roomId,String content){

        }
    }
}
