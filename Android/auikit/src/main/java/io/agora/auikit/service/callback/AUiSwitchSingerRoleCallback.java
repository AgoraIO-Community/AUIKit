package io.agora.auikit.service.callback;

public interface AUiSwitchSingerRoleCallback {
    /**
     * 切换演唱角色成功
     */
    void onSwitchRoleSuccess();

    /**
     * 切换演唱角色失败
     * @param reason 切换演唱角色失败的原因
     */
    void onSwitchRoleFail(int reason);
}
