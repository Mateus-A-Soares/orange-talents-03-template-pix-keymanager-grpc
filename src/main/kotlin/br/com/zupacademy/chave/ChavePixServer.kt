package br.com.zupacademy.chave

import br.com.zupacademy.CadastraChavePixRequest
import br.com.zupacademy.ChavePixCadastradaResponse
import br.com.zupacademy.ChavePixServiceGrpc
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@Singleton
class ChavePixServer(@Inject private val chavePixService: ChavePixService, @Inject private val validator: Validator) : ChavePixServiceGrpc.ChavePixServiceImplBase() {

    override fun registra(
        request: CadastraChavePixRequest,
        responseObserver: StreamObserver<ChavePixCadastradaResponse>
    ) {
        val chavePixValidada: ChavePixValidatedProxy = request.toValidatedProxy(validator)
        val response: ChavePixCadastradaResponse = chavePixService.cadastra(chavePixValidada)
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}