package br.com.zupacademy.chave.cadastro

import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.TipoChave
import br.com.zupacademy.chave.TipoConta
import br.com.zupacademy.chave.conta.Conta
import br.com.zupacademy.shared.constraints.ValidChavePix
import br.com.zupacademy.shared.constraints.ValidUUID
import io.micronaut.core.annotation.Introspected
import io.micronaut.validation.Validated
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ValidChavePix
data class ChavePixValidatedProxy(
    @field:ValidUUID @field:NotBlank
    val clienteId: String?,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:Size(max = 77)
    var chave: String?,
    @field:NotNull
    val tipoConta: TipoConta?
) {
    fun toModel(conta: Conta): ChavePix {

        return ChavePix(
            clienteId = UUID.fromString(clienteId!!),
            tipoChave = tipoChave!!,
            chave = chave!!,
            conta = conta
        )
    }
}
