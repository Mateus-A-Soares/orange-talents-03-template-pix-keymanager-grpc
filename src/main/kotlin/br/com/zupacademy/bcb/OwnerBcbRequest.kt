package br.com.zupacademy.bcb

import br.com.zupacademy.itauerp.TitularResponse

data class OwnerBcbRequest(
    val type: TypeBcb,
    val name: String,
    val taxIdNumber: String
) {
    companion object {
        fun of(titularResponse: TitularResponse): OwnerBcbRequest {
            return OwnerBcbRequest(
                type = TypeBcb.NATURAL_PERSON,
                name = titularResponse.nome,
                taxIdNumber = titularResponse.cpf
            )
        }
    }
}

