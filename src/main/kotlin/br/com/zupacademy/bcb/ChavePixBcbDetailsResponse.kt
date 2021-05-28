package br.com.zupacademy.bcb

import java.time.LocalDateTime

data class ChavePixBcbDetailsResponse(
    val keyType: KeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountBcbResponse,
    val owner: OwnerBcbResponse,
    val createdAt: LocalDateTime
)

data class OwnerBcbResponse(
    val type: TypeBcb,
    val name: String,
    val taxIdNumber: String
)

data class BankAccountBcbResponse(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountTypeBcb
)