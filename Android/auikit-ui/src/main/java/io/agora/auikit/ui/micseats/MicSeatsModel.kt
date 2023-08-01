package io.agora.auikit.ui.micseats

import java.io.Serializable

data class MicSeatOption(
    var micSeatCount:Int = 8,//麦位数 默认8麦位
    var micSeatType: MicSeatType = MicSeatType.EightTag,//布局类型 默认 8麦位布局
    var startAngle:Int = 0, //偏转角 默认 0
    var mRadius:Int = 0, //半径
    var mItemWidth:Int = 90, //麦位item 宽度
    var mItemHeight:Int = 120,//麦位item 高度
): Serializable

data class MicSeatItem(
    var number:Int, //麦位号
    var micX:Float, //麦位 x 坐标
    var micY:Float, //麦位 y 坐标
): Serializable


enum class MicSeatType(val value: Int) {
    OneTag(1), SixTag(2), EightTag(3),NineTag(4);

    companion object {
        fun fromString(value: String?): MicSeatType? {
            if (value != null) {
                for (enumValue in values()) {
                    if (value == enumValue.value.toString()) {
                        return enumValue
                    }
                }
            }
            return null
        }
    }
}

enum class MicSeatStatus {
    idle, used, locked
}