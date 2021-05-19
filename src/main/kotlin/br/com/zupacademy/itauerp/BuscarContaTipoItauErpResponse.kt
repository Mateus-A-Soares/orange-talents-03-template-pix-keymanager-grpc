package br.com.zupacademy.itauerp

data class BuscarContaTipoItauErpResponse(
    val tipo: String,
    val agencia: String,
    val numero: String,
    val instituicao : InstituicaoResponse,
    val titular : TitularResponse
)
