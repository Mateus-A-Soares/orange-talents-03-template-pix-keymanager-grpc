package br.com.zupacademy.itauerp

import br.com.zupacademy.chave.TipoConta
import br.com.zupacademy.chave.conta.Conta

data class BuscarContaTipoItauErpResponse(
    val tipo: String,
    val agencia: String,
    val numero: String,
    val instituicao : InstituicaoResponse,
    val titular : TitularResponse
) {
    fun toModel() : Conta {
        return Conta(instituicao = instituicao.nome, agencia = agencia, numero = numero, tipo = TipoConta.fromItauErpParameterName(tipo)!!)
    }
}
