package br.com.zupacademy.bcb

data class CadastraChavePixBcbRequest(
    val keyType: KeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountBcbRequest,
    val owner: OwnerBcbRequest,
)