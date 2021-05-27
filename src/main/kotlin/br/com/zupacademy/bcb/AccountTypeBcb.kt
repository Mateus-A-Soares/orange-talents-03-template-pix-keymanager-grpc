package br.com.zupacademy.bcb

import br.com.zupacademy.chave.TipoConta

enum class AccountTypeBcb {
    CACC, SVGS;

    companion object {
        fun of(tipoConta: TipoConta): AccountTypeBcb {
            return when(tipoConta) {
                TipoConta.CORRENTE -> CACC
                TipoConta.POUPANCA -> SVGS
            }
        }
    }
}