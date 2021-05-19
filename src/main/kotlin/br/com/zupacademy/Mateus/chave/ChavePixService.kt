package br.com.zupacademy.Mateus.chave

import br.com.zupacademy.Mateus.ChavePixCadastradaResponse
import javax.inject.Singleton

@Singleton
class ChavePixService {

    fun cadastra(chavePixValidada: ChavePixValidatedProxy): ChavePixCadastradaResponse {
//        TODO("IMPLEMENTAR CADASTRO DA CHAVE PIX")
        return ChavePixCadastradaResponse.newBuilder().setChavePixId("${(0..100).random().toString()} must be implemented").build()
    }
}