package br.com.zupacademy.chave.cadastro

import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.conta.Conta
import br.com.zupacademy.itauerp.BuscarContaTipoItauErpResponse
import br.com.zupacademy.itauerp.TitularResponse
import javax.validation.Valid

class CadastraChaveEvent(@field:Valid val validatedProxy: ChavePixValidatedProxy) {

    lateinit var chavePix: ChavePix private set
    lateinit var conta: Conta private set
    lateinit var titularResponse: TitularResponse private set
    private lateinit var itauClientResponse: BuscarContaTipoItauErpResponse

    fun setItauClientResponse(itauClientResponse: BuscarContaTipoItauErpResponse) {
        this.itauClientResponse =itauClientResponse
        conta = itauClientResponse.toModel()
        titularResponse = itauClientResponse.titular
    }

    fun buildChavePix() {
        chavePix = validatedProxy.toModel(conta)
    }
}