package br.com.zupacademy.bcb

data class OwnerBcbRequest(
    val type: TypeBcb,
    val name: String,
    val taxIdNumber: String
)

