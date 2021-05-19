package br.com.zupacademy.Mateus.chave

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class ChavePixValidatedProxy(
    @field:NotBlank
    val clienteId: String,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:NotBlank
    val chave: String,
    @field:NotNull
    val tipoConta: TipoConta?
)
