package br.com.zupacademy.chave.cadastro

import br.com.zupacademy.CadastraChavePixRequest
import br.com.zupacademy.toModel

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