package br.com.zupacademy.bcb

data class BankAccountBcbRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountTypeBcb
)

