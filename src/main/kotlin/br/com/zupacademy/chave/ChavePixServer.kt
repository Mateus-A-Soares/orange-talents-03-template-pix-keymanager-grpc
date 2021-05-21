package br.com.zupacademy.chave

import br.com.zupacademy.CadastraChavePixRequest
import br.com.zupacademy.ChavePixCadastradaResponse
import br.com.zupacademy.ChavePixServiceGrpc
import br.com.zupacademy.shared.exceptions.FieldNotFoundException
import br.com.zupacademy.shared.exceptions.UniqueFieldAlreadyExists
import com.google.rpc.Code
import com.google.rpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ChavePixServer(@Inject private val chavePixService: ChavePixService) :
    ChavePixServiceGrpc.ChavePixServiceImplBase() {

    override fun registra(
        request: CadastraChavePixRequest,
        responseObserver: StreamObserver<ChavePixCadastradaResponse>
    ) {
        try {
            val chavePixValidada: ChavePixValidatedProxy = request.toValidatedProxy()
            val response: ChavePixCadastradaResponse = chavePixService.cadastra(chavePixValidada)
            responseObserver.onNext(response)
            responseObserver.onCompleted()
        } catch (e: ConstraintViolationException) {
            val status = Status.newBuilder()
                .setCode(Code.INVALID_ARGUMENT_VALUE)
                .setMessage(e.message)
                .build()
            responseObserver.onError(StatusProto.toStatusRuntimeException(status))
        } catch (e: FieldNotFoundException) {
            val status = Status.newBuilder()
                .setCode(Code.NOT_FOUND_VALUE)
                .setMessage(e.message)
                .build()
            responseObserver.onError(StatusProto.toStatusRuntimeException(status))
        } catch (e: UniqueFieldAlreadyExists) {
            val status = Status.newBuilder()
                .setCode(Code.ALREADY_EXISTS_VALUE)
                .setMessage(e.message)
                .build()
            responseObserver.onError(StatusProto.toStatusRuntimeException(status))
        } catch (e: Throwable) {
            val status: Status = Status.newBuilder()
                .setCode(Code.INTERNAL_VALUE)
                .build()
            responseObserver.onError(StatusProto.toStatusRuntimeException(status))
        }
    }
}