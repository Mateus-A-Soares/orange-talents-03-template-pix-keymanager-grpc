package br.com.zupacademy.chave

import br.com.zupacademy.chave.conta.Conta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ChavePixValidatedProxy(
    @field:NotBlank
    val clienteId: String?,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:NotBlank
    val chave: String?,
    @field:NotNull
    val tipoConta: TipoConta?
) {
    fun toModel(conta : Conta): ChavePix {
        return ChavePix(clienteId = UUID.fromString(clienteId!!), tipoChave = tipoChave!!, chave = chave!!, conta = conta)
    }
}
