package io.agora.auikit.ui.praiseEffect

import io.agora.auikit.ui.basic.AUIImageView
import io.agora.auikit.ui.praiseEffect.impI.AUIAbstractPathAnimator
import io.agora.auikit.ui.praiseEffect.impI.AUIPraiseEffectLayout

interface IAUIPraiseEffect {

    fun setOnHearLayoutListener(onHearLayoutListener: AUIPraiseEffectLayout.OnHearLayoutListener?){}

    fun getPraiseEffectView(): AUIImageView?

    fun setPraiseEffectViewIcon(ResId:Int){}

    fun setPraiseEffectViewSize(width:Int,height:Int){}

    fun getAnimator(): AUIAbstractPathAnimator?

    fun setAnimator(animator: AUIAbstractPathAnimator?){}

    fun setDrawableIds(drawableIds: Array<Int>){}

    fun addFavor(){}
}