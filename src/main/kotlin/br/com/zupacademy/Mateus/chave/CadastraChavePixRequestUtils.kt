package br.com.zupacademy.Mateus.chave

import br.com.zupacademy.Mateus.CadastraChavePixRequest
import br.com.zupacademy.Mateus.TipoChaveRequest
import br.com.zupacademy.Mateus.TipoContaRequest
import javax.validation.ConstraintViolation
import javax.validation.Validator

/**
 *  Transforma o objeto CadastraChavePixRequest em um ChavePixValidatedProxy, validado pelo Validator passado como
 * parâmetro.
 */
fun CadastraChavePixRequest.toValidatedProxy(validator: Validator): ChavePixValidatedProxy {
    val validatedProxy = ChavePixValidatedProxy(
        clienteId = this.clienteId,
        tipoConta = this.tipoConta.toModel(),
        tipoChave = this.tipoChave.toModel(),
        chave = this.valorChave
    )
    val violations: MutableSet<ConstraintViolation<ChavePixValidatedProxy>> = validator.validate(validatedProxy)
    //TODO("IMPLEMENTAR TRATAMENTO PARA VALIDAÇÂO DOS ERROS")
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