package br.com.zupacademy.chave

import br.com.zupacademy.ChavePixCadastradaResponse
import javax.inject.Singleton

@Singleton
class ChavePixService(/*@Inject val itauClient : ItauErpClient*/) {

    fun cadastra(chavePixValidada: ChavePixValidatedProxy): ChavePixCadastradaResponse {
//        TODO("IMPLEMENTAR CADASTRO DA CHAVE PIX")
        return ChavePixCadastradaResponse.newBuilder().setChavePixId("${(0..100).random().toString()} must be implemented").build()
    }
}