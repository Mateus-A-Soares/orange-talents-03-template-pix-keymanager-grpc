package br.com.zupacademy.chave.cadastro

import br.com.zupacademy.CadastraChavePixRequest
import br.com.zupacademy.CadastraChavePixServiceGrpc
import br.com.zupacademy.ChavePixCadastradaResponse
import br.com.zupacademy.shared.exceptions.handlers.config.ErrorHandler
import io.grpc.stub.StreamObserver
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.validation.Validated
import io.micronaut.validation.validator.Validator
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class CadastraChavePixEndpoint(@Inject private val publisher: ApplicationEventPublisher) :
    CadastraChavePixServiceGrpc.CadastraChavePixServiceImplBase() {

    override fun registra(
        request: CadastraChavePixRequest,
        responseObserver: StreamObserver<ChavePixCadastradaResponse>
    ) {
        val chavePixValidada: ChavePixValidatedProxy = request.toValidatedProxy()
        val event: CadastraChaveEvent = CadastraChaveEvent(chavePixValidada)
        publisher.publishEvent(event)
        val chavePixCadastrada = event.chavePix
        val response = ChavePixCadastradaResponse.newBuilder().setChavePixId(chavePixCadastrada.id.toString()).build()
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}