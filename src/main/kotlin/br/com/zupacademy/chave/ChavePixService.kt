package br.com.zupacademy.chave

import br.com.zupacademy.ChavePixCadastradaResponse
import br.com.zupacademy.itauerp.BuscarContaTipoItauErpResponse
import br.com.zupacademy.itauerp.ItauErpClient
import io.micronaut.data.jpa.repository.JpaRepository
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChavePixService(@Inject val itauClient : ItauErpClient, @Inject val repository: ChavePixRespository) {

    fun cadastra(chavePixValidada: ChavePixValidatedProxy): ChavePixCadastradaResponse {
//        TODO("IMPLEMENTAR CADASTRO DA CHAVE PIX")
        val conta: BuscarContaTipoItauErpResponse? = itauClient.buscaPorContaTipo(
            clienteId = chavePixValidada.clienteId,
            tipoConta = chavePixValidada.tipoConta!!.itauErpParameterName
        ).body()
        conta?: throw RuntimeException("Conta já existente")
        if(repository.existsByChave(chavePixValidada.chave))
            throw RuntimeException("Chave já cadastrada")
        val chavePix = chavePixValidada.toModel()
        repository.save(chavePix)
        return ChavePixCadastradaResponse.newBuilder().setChavePixId(chavePix.id.toString()).build()
    }
}