package io.agora.auikit.service.collection

import android.support.annotation.IntDef

const val AUICollectionOperationTypeAdd = 0 //新增
const val AUICollectionOperationTypeUpdate = 1 //更新，对传递进来的map进行根节点上的替换
const val AUICollectionOperationTypeMerge = 2 //合并，对传递进来的map进行每个子节点的替换
const val AUICollectionOperationTypeRemove = 3 //删除
const val AUICollectionOperationTypeClean = 4 //清理对应scene的key/value，相当于在rtm metadata里移除这个collection的所有信息
const val AUICollectionOperationTypeIncrease = 10 //增加
const val AUICollectionOperationTypeDecrease = 11 //减少

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    AUICollectionOperationTypeAdd,
    AUICollectionOperationTypeUpdate,
    AUICollectionOperationTypeMerge,
    AUICollectionOperationTypeRemove,
    AUICollectionOperationTypeClean,
    AUICollectionOperationTypeIncrease,
    AUICollectionOperationTypeDecrease
)
annotation class AUICollectionOperationType


const val AUICollectionMessageTypeNormal = 1
const val AUICollectionMessageTypeReceipt = 2

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@IntDef(
    AUICollectionMessageTypeNormal,
    AUICollectionMessageTypeReceipt
)
annotation class AUICollectionMessageType

data class AUICollectionMessagePayload(
    @AUICollectionOperationType val type: Int = AUICollectionOperationTypeUpdate,
    val dataCmd: String?,
    val data: Map<String, Any>?
)

data class AUICollectionMessage(
    val channelName: String?,
    @AUICollectionMessageType val messageType: Int = AUICollectionMessageTypeNormal,
    val uniqueId: String?,
    val sceneKey: String?,
    val payload: AUICollectionMessagePayload?
)