package br.com.zupacademy.chave

import br.com.zupacademy.ChavePixCadastradaResponse
import br.com.zupacademy.itauerp.BuscarContaTipoItauErpResponse
import br.com.zupacademy.itauerp.ItauErpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChavePixService(@Inject val itauClient : ItauErpClient) {

    fun cadastra(chavePixValidada: ChavePixValidatedProxy): ChavePixCadastradaResponse {
//        TODO("IMPLEMENTAR CADASTRO DA CHAVE PIX")
        val conta: BuscarContaTipoItauErpResponse? = itauClient.buscaPorContaTipo(
            clienteId = chavePixValidada.clienteId,
            tipoConta = chavePixValidada.tipoConta!!.itauErpParameterName
        ).body()

        return ChavePixCadastradaResponse.newBuilder().setChavePixId("${(0..100).random().toString()} must be implemented").build()
    }
}