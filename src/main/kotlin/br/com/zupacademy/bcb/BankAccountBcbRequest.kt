package br.com.zupacademy.bcb

import br.com.zupacademy.chave.conta.Conta
import br.com.zupacademy.chave.conta.ItauBank

data class BankAccountBcbRequest(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountTypeBcb
) {
    companion object {
        fun of(conta: Conta): BankAccountBcbRequest {
            return BankAccountBcbRequest(
                participant = ItauBank.ISPB,
                branch = conta.agencia,
                accountNumber = conta.numero,
                accountType = AccountTypeBcb.of(conta.tipo)
            )
        }
    }
}

