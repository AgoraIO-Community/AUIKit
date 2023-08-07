package io.agora.auikit.service.http

data class CommonResp<Data>(
    val code: Int = 0,
    val message: String?,
    val data: Data?
)