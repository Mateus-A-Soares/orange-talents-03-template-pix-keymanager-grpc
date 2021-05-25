package br.com.zupacademy.chave

import br.com.zupacademy.chave.conta.Conta
import br.com.zupacademy.shared.constraints.ValidChavePix
import br.com.zupacademy.shared.constraints.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
@ValidChavePix
data class ChavePixValidatedProxy(
    @ValidUUID @field:NotBlank
    val clienteId: String?,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:Size(max = 77)
    val chave: String?,
    @field:NotNull
    val tipoConta: TipoConta?
) {
    fun toModel(conta: Conta): ChavePix {

        return ChavePix(
            clienteId = UUID.fromString(clienteId!!),
            tipoChave = tipoChave!!,
            chave = if (tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else chave!!,
            conta = conta
        )
    }
}
