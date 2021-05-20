package br.com.zupacademy.chave

import br.com.zupacademy.CadastraChavePixRequest
import br.com.zupacademy.TipoChaveRequest
import br.com.zupacademy.TipoContaRequest

/**
 *
 *  Transforma o objeto CadastraChavePixRequest em um ChavePixValidatedProxy
 */
fun CadastraChavePixRequest.toValidatedProxy(): ChavePixValidatedProxy {
    val validatedProxy = ChavePixValidatedProxy(
        clienteId = this.clienteId,
        tipoConta = this.tipoConta.toModel(),
        tipoChave = this.tipoChave.toModel(),
        chave = this.valorChave
    )
    return validatedProxy
}

fun TipoChaveRequest.toModel() : TipoChave? {
    return when(this) {
        TipoChaveRequest.TIPO_CHAVE_CPF -> TipoChave.CPF
        TipoChaveRequest.TIPO_CHAVE_EMAIL -> TipoChave.EMAIL
        TipoChaveRequest.TIPO_CHAVE_TELEFONE -> TipoChave.TELEFONE
        TipoChaveRequest.TIPO_CHAVE_ALEATORIA -> TipoChave.ALEATORIA
        else -> null
    }
}

fun TipoContaRequest.toModel() : TipoConta? {
    return when(this) {
        TipoContaRequest.TIPO_CONTA_CORRENTE -> TipoConta.CORRENTE
        TipoContaRequest.TIPO_CONTA_POUPANCA -> TipoConta.POUPANCA
        else -> null
    }
}