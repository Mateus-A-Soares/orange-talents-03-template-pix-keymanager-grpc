package br.com.zupacademy.chave.cadastro

import br.com.zupacademy.ChavePixCadastradaResponse
import br.com.zupacademy.bcb.*
import br.com.zupacademy.chave.ChavePixRespository
import br.com.zupacademy.chave.TipoConta
import br.com.zupacademy.itauerp.BuscarContaTipoItauErpResponse
import br.com.zupacademy.itauerp.ItauErpClient
import br.com.zupacademy.shared.exceptions.FieldNotFoundException
import br.com.zupacademy.shared.exceptions.UniqueFieldAlreadyExistsException
import com.google.rpc.Code
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Singleton
@Validated
class CadastraChavePixService(
    @Inject val itauClient: ItauErpClient,
    @Inject val bcbClient: BcbClient,
    @Inject val repository: ChavePixRespository
) {

    fun cadastra(@Valid chavePixValidada: ChavePixValidatedProxy): ChavePixCadastradaResponse {
        val contaResponse: BuscarContaTipoItauErpResponse? = itauClient.buscaPorContaTipo(
            clienteId = chavePixValidada.clienteId!!,
            tipoConta = chavePixValidada.tipoConta!!.itauErpParameterName
        ).body()
        contaResponse ?: throw FieldNotFoundException(
            field = "Conta",
            message = "Conta não foi encontrada",
            rpcCode = Code.FAILED_PRECONDITION
        )
        if (repository.existsByChave(chavePixValidada.chave!!)) throw UniqueFieldAlreadyExistsException(field = "chave")
        val conta = contaResponse.toModel()
        val chavePix = chavePixValidada.toModel(conta)
        bcbClient.cadastraChave(CadastraChavePixBcbRequest.of(chavePix, contaResponse.titular))
        repository.save(chavePix)
        return ChavePixCadastradaResponse.newBuilder().setChavePixId(chavePix.id.toString()).build()
    }
}