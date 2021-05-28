package br.com.zupacademy.bcb

import br.com.zupacademy.chave.conta.ItauBank

data class DeletaChavePixBcbRequest(
    val key: String, val participant: String = ItauBank.ISPB
)
