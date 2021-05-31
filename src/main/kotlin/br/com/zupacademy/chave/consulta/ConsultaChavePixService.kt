package br.com.zupacademy.chave.consulta

import br.com.zupacademy.ConsultaChavePixResponse
import br.com.zupacademy.Conta
import br.com.zupacademy.bcb.BcbClient
import br.com.zupacademy.bcb.ChavePixBcbDetailsResponse
import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.ChavePixRepository
import br.com.zupacademy.itauerp.ItauErpClient
import br.com.zupacademy.shared.constraints.ValidUUID
import br.com.zupacademy.shared.exceptions.ApiException
import br.com.zupacademy.shared.exceptions.FieldNotFoundException
import br.com.zupacademy.toGrpcRequest
import com.google.protobuf.Timestamp
import com.google.rpc.Code
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotNull

@Validated
@Singleton
class ConsultaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BcbClient,
    @Inject val itauClient: ItauErpClient
) {


    fun consulta(
        @NotNull @ValidUUID chaveIdString: String,
        @NotNull @ValidUUID clienteIdString: String
    ): ConsultaChavePixResponse {
        val chaveId = UUID.fromString(chaveIdString)
        val clienteId = UUID.fromString(clienteIdString)
        val chavePix: ChavePix = repository.findByIdAndClienteId(chaveUuid = chaveId, clienteUuid = clienteId)
            ?: throw FieldNotFoundException(
                field = "chave",
                message = "Chave não está na lista de chaves do cliente",
                rpcCode = Code.NOT_FOUND
            )
        val bcbResponse: ChavePixBcbDetailsResponse? =
            bcbClient.buscaChave(chavePix.chave).body() ?: throw FieldNotFoundException(
                field = "chave",
                message = "Chave não está na lista de chaves do cliente",
                rpcCode = Code.NOT_FOUND
            )
        val itauClientResponse =
            itauClient.buscaPorContaTipo(
                clienteId = clienteIdString,
                tipoConta = chavePix.conta.tipo.itauErpParameterName
            )

        val contaResponse: Conta = itauClientResponse.body()?.run {
            Conta.newBuilder().setTitular(titular.nome)
                .setCpf(titular.cpf)
                .setInstituicao(instituicao.nome)
                .setAgencia(agencia)
                .setNumero(numero)
                .setTipoConta(chavePix.conta.tipo.toGrpcRequest())
                .build()
        } ?: throw ApiException()

        chavePix.run {
            val timestamp = Timestamp.newBuilder().setSeconds(persistenceTimestamp!!.epochSecond)
                .setNanos(persistenceTimestamp.nano)

            return ConsultaChavePixResponse.newBuilder()
                .setClienteId(clienteIdString)
                .setChavePixId(chaveIdString)
                .setTipoChave(tipoChave.toGrpcRequest())
                .setValorChave(chave)
                .setConta(contaResponse)
                .setPersistenceTimestamp(timestamp)
                .build()
        }
    }
}