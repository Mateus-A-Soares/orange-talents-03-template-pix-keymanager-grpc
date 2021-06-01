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
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Validated
@Singleton
class ConsultaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BcbClient,
    @Inject val itauClient: ItauErpClient
) {

    fun consulta(
        @NotBlank @ValidUUID chaveIdString: String,
        @NotBlank @ValidUUID clienteIdString: String
    ): ConsultaChavePixResponse {
        val chaveId = UUID.fromString(chaveIdString)
        val clienteId = UUID.fromString(clienteIdString)
        val chavePix: ChavePix = repository.findByIdAndClienteId(chaveUuid = chaveId, clienteUuid = clienteId)
            ?: throw FieldNotFoundException(
                field = "chave",
                message = "Chave não está na lista de chaves do cliente",
                rpcCode = Code.NOT_FOUND
            )

        val bcbResponseStatus = bcbClient.buscaChave(chavePix.chave).status
        if (bcbResponseStatus != HttpStatus.OK) throw FieldNotFoundException(
            field = "chave",
            message = "Chave não está na lista de chaves do cliente",
            rpcCode = Code.NOT_FOUND
        )

        return chavePix.toGrpcResponse()
    }

    fun consultaInterna(@NotBlank @Size(max = 77) chave: String): ConsultaChavePixResponse {
        val chavePix = repository.findByChave(chave)
        if (chavePix != null) return chavePix?.toGrpcResponse()

        val chavePixBcb: ChavePixBcbDetailsResponse =
            bcbClient.buscaChave(chave = chave)?.body() ?: throw FieldNotFoundException(
                field = "chave",
                message = "Chave não está na lista de chaves do cliente",
                rpcCode = Code.NOT_FOUND
            )

        return chavePixBcb?.run {
            val timestamp = Timestamp.newBuilder().setSeconds(createdAt!!.toEpochSecond(ZoneOffset.UTC))
                .setNanos(createdAt.nano)

            val contaResponse = Conta.newBuilder().setTitular(owner.name)
                .setCpf(owner.taxIdNumber)
                .setInstituicao(bankAccount.participant)
                .setAgencia(bankAccount.branch)
                .setNumero(bankAccount.accountNumber)
                .setTipoConta(bankAccount.accountType.toGrpcRequest())
                .build()

            ConsultaChavePixResponse.newBuilder()
                .setTipoChave(keyType.toGrpcRequest())
                .setValorChave(key)
                .setConta(contaResponse)
                .setPersistenceTimestamp(timestamp)
                .build()
        }
    }

    private fun ChavePix.toGrpcResponse(): ConsultaChavePixResponse {
        val itauClientResponse =
            itauClient.buscaPorContaTipo(
                clienteId = clienteId.toString(),
                tipoConta = conta.tipo.itauErpParameterName
            )

        val contaResponse: Conta = itauClientResponse.body()?.run {
            Conta.newBuilder().setTitular(titular.nome)
                .setCpf(titular.cpf)
                .setInstituicao(instituicao.nome)
                .setAgencia(agencia)
                .setNumero(numero)
                .setTipoConta(conta.tipo.toGrpcRequest())
                .build()
        } ?: throw ApiException()

        val timestamp = Timestamp.newBuilder().setSeconds(persistenceTimestamp!!.epochSecond)
            .setNanos(persistenceTimestamp.nano)

        return ConsultaChavePixResponse.newBuilder()
            .setClienteId(clienteId.toString())
            .setChavePixId(id.toString())
            .setTipoChave(tipoChave.toGrpcRequest())
            .setValorChave(chave)
            .setConta(contaResponse)
            .setPersistenceTimestamp(timestamp)
            .build()
    }
}