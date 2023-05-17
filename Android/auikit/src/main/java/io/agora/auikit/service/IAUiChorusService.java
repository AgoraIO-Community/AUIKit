package io.agora.auikit.service;

import androidx.annotation.Nullable;

import io.agora.auikit.model.AUiChoristerModel;
import io.agora.auikit.service.callback.AUiCallback;
import io.agora.auikit.service.callback.AUiChoristerListCallback;
import io.agora.auikit.service.callback.AUiSwitchSingerRoleCallback;

public interface IAUiChorusService extends IAUiCommonService<IAUiChorusService.AUiChorusRespDelegate>{


    // 获取合唱者列表
    void getChoristersList(@Nullable AUiChoristerListCallback callback);

    // 加入合唱
    void joinChorus(@Nullable String songCode, @Nullable String userId, @Nullable AUiCallback callback);

    // 退出合唱
    void leaveChorus(@Nullable String songCode, @Nullable String userId, @Nullable AUiCallback callback);

    // 切换角色
    void switchSingerRole(int newRole, @Nullable AUiSwitchSingerRoleCallback callback);

    interface AUiChorusRespDelegate {
        /// 合唱者加入
        /// - Parameter chorus: <#chorus description#>
        void onChoristerDidEnter(AUiChoristerModel chorister);

        /// 合唱者离开
        /// - Parameter chorister: <#chorister description#>
        void onChoristerDidLeave(AUiChoristerModel chorister);

        /// 角色切换回调
        /// - Parameters:
        ///   - oldRole: <#oldRole description#>
        ///   - newRole: <#newRole description#>
        void onSingerRoleChanged(int oldRole, int newRole);

        void onChoristerDidChanged();
    }
}
