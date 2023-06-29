package io.agora.auikit.model

import java.io.Serializable

data class AUIActionModel constructor(
    var userList: MutableList<AUIUserInfo> = mutableListOf(),
    var invitedIndex:Int? = -1
): Serializable

 /**
    邀请
    1：被邀请人同意
    2：被邀请人拒绝
    3：邀请人人取消
    4：超时
    5：并发上麦失败 别人先上了
    申请
    被移除原因：
    1：房主同意
    2：房主拒绝
    3：申请人取消
    4：超时
    5：并发上麦失败 别人先上了
  */
enum class AUIActionOperation {
    agree, refuse,cancel,timeout,failed;
}