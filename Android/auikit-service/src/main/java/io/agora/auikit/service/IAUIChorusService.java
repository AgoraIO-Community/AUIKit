package io.agora.auikit.service;

import androidx.annotation.Nullable;

import io.agora.auikit.model.AUIChoristerModel;
import io.agora.auikit.service.callback.AUICallback;
import io.agora.auikit.service.callback.AUIChoristerListCallback;
import io.agora.auikit.service.callback.AUISwitchSingerRoleCallback;

public interface IAUIChorusService extends IAUICommonService<IAUIChorusService.AUIChorusRespObserver> {


    // 获取合唱者列表
    void getChoristersList(@Nullable AUIChoristerListCallback callback);

    // 加入合唱
    void joinChorus(@Nullable String songCode, @Nullable String userId, @Nullable AUICallback callback);

    // 退出合唱
    void leaveChorus(@Nullable String songCode, @Nullable String userId, @Nullable AUICallback callback);

    // 切换角色
    void switchSingerRole(int newRole, @Nullable AUISwitchSingerRoleCallback callback);

    interface AUIChorusRespObserver {
        /// 合唱者加入
        /// - Parameter chorus: <#chorus description#>
        void onChoristerDidEnter(AUIChoristerModel chorister);

        /// 合唱者离开
        /// - Parameter chorister: <#chorister description#>
        void onChoristerDidLeave(AUIChoristerModel chorister);

        /// 角色切换回调
        /// - Parameters:
        ///   - oldRole: <#oldRole description#>
        ///   - newRole: <#newRole description#>
        void onSingerRoleChanged(int oldRole, int newRole);

        void onChoristerDidChanged();
    }
}
