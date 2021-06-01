package br.com.zupacademy

import br.com.zupacademy.bcb.AccountTypeBcb
import br.com.zupacademy.bcb.KeyTypeBcb
import br.com.zupacademy.chave.TipoChave
import br.com.zupacademy.chave.TipoConta

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

fun TipoChave.toGrpcRequest(): TipoChaveRequest {
    return when (this) {
        TipoChave.ALEATORIA -> TipoChaveRequest.TIPO_CHAVE_ALEATORIA
        TipoChave.EMAIL -> TipoChaveRequest.TIPO_CHAVE_EMAIL
        TipoChave.TELEFONE -> TipoChaveRequest.TIPO_CHAVE_TELEFONE
        TipoChave.CPF -> TipoChaveRequest.TIPO_CHAVE_CPF
    }
}

fun TipoConta.toGrpcRequest(): TipoContaRequest {
    return when (this) {
        TipoConta.POUPANCA -> TipoContaRequest.TIPO_CONTA_POUPANCA
        TipoConta.CORRENTE -> TipoContaRequest.TIPO_CONTA_CORRENTE
    }
}

fun KeyTypeBcb.toGrpcRequest(): TipoChaveRequest {
    return when (this) {
        KeyTypeBcb.RANDOM -> TipoChaveRequest.TIPO_CHAVE_ALEATORIA
        KeyTypeBcb.EMAIL -> TipoChaveRequest.TIPO_CHAVE_EMAIL
        KeyTypeBcb.PHONE -> TipoChaveRequest.TIPO_CHAVE_TELEFONE
        KeyTypeBcb.CNPJ, KeyTypeBcb.CPF -> TipoChaveRequest.TIPO_CHAVE_CPF
    }
}

fun AccountTypeBcb.toGrpcRequest(): TipoContaRequest {
    return when (this) {
        AccountTypeBcb.SVGS -> TipoContaRequest.TIPO_CONTA_POUPANCA
        AccountTypeBcb.CACC -> TipoContaRequest.TIPO_CONTA_CORRENTE
    }
}