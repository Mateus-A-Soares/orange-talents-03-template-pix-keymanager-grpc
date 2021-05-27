package br.com.zupacademy.chave.cadastro

import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.itauerp.BuscarContaTipoItauErpResponse

class CadastraChaveEvent(val validatedProxy: ChavePixValidatedProxy) {

    lateinit var chavePix: ChavePix private set
    var itauClientResponse: BuscarContaTipoItauErpResponse? = null
        set(itauClientResponse) {
            itauClientResponse?.let {
                chavePix = validatedProxy.toModel(it.toModel())
                field = it
            }
        }
}