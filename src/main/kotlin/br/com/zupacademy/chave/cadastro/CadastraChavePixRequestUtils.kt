package br.com.zupacademy.chave.cadastro

import br.com.zupacademy.CadastraChavePixRequest
import br.com.zupacademy.TipoChaveRequest
import br.com.zupacademy.TipoContaRequest
import br.com.zupacademy.chave.TipoChave
import br.com.zupacademy.chave.TipoConta

/**
 *
 *  Transforma o objeto CadastraChavePixRequest em um ChavePixValidatedProxy
 */
fun CadastraChavePixRequest.toValidatedProxy(): ChavePixValidatedProxy {
    return ChavePixValidatedProxy(
        clienteId = this.clienteId,
        tipoConta = this.tipoConta.toModel(),
        tipoChave = this.tipoChave.toModel(),
        chave = this.valorChave
    )
}

fun TipoChaveRequest.toModel(): TipoChave? {
    return when (this) {
        TipoChaveRequest.TIPO_CHAVE_CPF -> TipoChave.CPF
        TipoChaveRequest.TIPO_CHAVE_EMAIL -> TipoChave.EMAIL
        TipoChaveRequest.TIPO_CHAVE_TELEFONE -> TipoChave.TELEFONE
        TipoChaveRequest.TIPO_CHAVE_ALEATORIA -> TipoChave.ALEATORIA
        else -> null
    }
}

fun TipoContaRequest.toModel(): TipoConta? {
    return when (this) {
        TipoContaRequest.TIPO_CONTA_CORRENTE -> TipoConta.CORRENTE
        TipoContaRequest.TIPO_CONTA_POUPANCA -> TipoConta.POUPANCA
        else -> null
    }
}