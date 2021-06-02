package br.com.zupacademy.chave.consulta

import br.com.zupacademy.*
import br.com.zupacademy.chave.ChavePix
import br.com.zupacademy.chave.ChavePixRepository
import br.com.zupacademy.itauerp.ItauErpClient
import br.com.zupacademy.shared.constraints.ValidUUIDValidator
import br.com.zupacademy.shared.exceptions.FieldNotFoundException
import br.com.zupacademy.shared.exceptions.handlers.config.ErrorHandler
import com.google.protobuf.Timestamp
import com.google.rpc.Code
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ConsultaChavePixPorClienteEndpoint(
    @Inject val itauErpClient: ItauErpClient,
    @Inject val repository: ChavePixRepository,
    @Inject val uuidValidator: ValidUUIDValidator
) :
    ConsultaChavesPixPorClienteServiceGrpc.ConsultaChavesPixPorClienteServiceImplBase() {

    override fun consulta(
        request: ConsultaChavesPixPorClienteRequest,
        observer: StreamObserver<ChavesPixPorClienteResponseList>
    ) {
        if (request.clienteId.isNullOrBlank() || !uuidValidator.isValid(request.clienteId))
            throw IllegalArgumentException("Id inválido")
        val clienteId = UUID.fromString(request.clienteId)
        val itauResponse = itauErpClient.buscaCliente(clienteId.toString())
        if (itauResponse.status != HttpStatus.OK) throw FieldNotFoundException(
            field = "Conta",
            rpcCode = Code.FAILED_PRECONDITION
        )

        val listaChaves: List<ChavePix> = repository.findByClienteId(clienteId)
        val responseList = listaChaves.map { chavePix ->
            with(chavePix) {
                val timestamp = Timestamp.newBuilder().setSeconds(persistenceTimestamp!!.epochSecond)
                    .setNanos(persistenceTimestamp.nano).build()
                ChavesPixPorClienteResponse.newBuilder()
                    .setChavePixId(id.toString())
                    .setValorChave(chave)
                    .setTipoChave(tipoChave.toGrpcRequest())
                    .setClienteId(clienteId.toString())
                    .setTipoConta(conta.tipo.toGrpcRequest())
                    .setPersistenceTimestamp(timestamp)
                    .build()
            }
        }
        val response = ChavesPixPorClienteResponseList.newBuilder()
            .addAllListaDeChaves(responseList)
            .build()
        observer.onNext(response)
        observer.onCompleted()
    }
}