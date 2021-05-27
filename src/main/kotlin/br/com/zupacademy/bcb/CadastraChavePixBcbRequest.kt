package br.com.zupacademy.bcb

import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.itauerp.TitularResponse

data class CadastraChavePixBcbRequest(
    val keyType: KeyTypeBcb,
    val key: String,
    val bankAccount: BankAccountBcbRequest,
    val owner: OwnerBcbRequest,
) {
    companion object {
        fun of(chave: ChavePix, titular: TitularResponse): CadastraChavePixBcbRequest {

            return CadastraChavePixBcbRequest(
                keyType = KeyTypeBcb.of(chave.tipoChave),
                key = chave.chave,
                bankAccount = BankAccountBcbRequest.of(chave.conta),
                owner = OwnerBcbRequest.of(titular)
            )

        }
    }
}