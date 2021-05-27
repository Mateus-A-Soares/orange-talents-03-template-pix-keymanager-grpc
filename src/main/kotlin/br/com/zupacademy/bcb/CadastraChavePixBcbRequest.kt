package br.com.zupacademy.bcb

import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.cadastro.ChavePixValidatedProxy
import br.com.zupacademy.itauerp.TitularResponse

data class CadastraChavePixBcbRequest(
    val keyType: KeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountBcbRequest,
    val owner: OwnerBcbRequest,
) {
    companion object {
        fun of(chave: ChavePixValidatedProxy, owner: OwnerBcbRequest, bankAccount: BankAccountBcbRequest): CadastraChavePixBcbRequest {

            return CadastraChavePixBcbRequest(
                keyType = KeyTypeBcb.of(chave.tipoChave!!),
                key = chave.chave!!,
                bankAccount = bankAccount,
                owner = owner
            )

        }
    }
}