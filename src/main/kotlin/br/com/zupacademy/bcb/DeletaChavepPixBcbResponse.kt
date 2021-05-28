package br.com.zupacademy.bcb

import java.time.LocalDateTime

data class DeletaChavepPixBcbResponse(
    val key: String, val participant: String, val deletedAt: LocalDateTime
)
