package io.agora.auikit.ui.member

data class MemberInfo(
    val userId: String,
    val userName: String,
    val userAvatar: String
)

class MemberItemModel(
    val user: MemberInfo?,
    val micIndex: Int?
) {

    fun micString(): String? {
        if (micIndex == null) {
            return null
        }
        return if (micIndex == 0) {
            "房主"
        } else {
            "${micIndex + 1}号麦"
        }
    }
}