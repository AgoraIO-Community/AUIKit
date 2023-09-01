package io.agora.auikit.ui.member

data class MemberInfo(
    var userId: String,
    var userName: String,
    var userAvatar: String
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