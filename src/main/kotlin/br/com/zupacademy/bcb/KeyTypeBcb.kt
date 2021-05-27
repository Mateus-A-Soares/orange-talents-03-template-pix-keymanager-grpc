package br.com.zupacademy.bcb

import br.com.zupacademy.chave.TipoChave

enum class KeyTypeBcb {
    CPF, CNPJ, PHONE, EMAIL, RANDOM;

    companion object {
        fun of(tipoChave: TipoChave): KeyTypeBcb {
            return when(tipoChave) {
                TipoChave.CPF -> KeyTypeBcb.CPF
                TipoChave.EMAIL -> KeyTypeBcb.EMAIL
                TipoChave.TELEFONE -> KeyTypeBcb.PHONE
                TipoChave.ALEATORIA -> KeyTypeBcb.RANDOM
            }
        }
    }
}