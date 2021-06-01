package br.com.zupacademy.chave.consulta

import br.com.zupacademy.*
import br.com.zupacademy.shared.exceptions.handlers.config.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ConsultaChavePixEndpoint(@Inject val service: ConsultaChavePixService) :
    ConsultaChavePixServiceGrpc.ConsultaChavePixServiceImplBase() {

    override fun consulta(
        request: ConsultaChavePixRequest,
        observer: StreamObserver<ConsultaChavePixResponse>
    ) {
        val chaveId = request.chavePixId
        val clienteId = request.clienteId
        val response: ConsultaChavePixResponse = service.consulta(chaveId, clienteId,)
        observer.onNext(response)
        observer.onCompleted()
    }

    override fun consultaInterna(
        request: ConsultaInternaChavePixRequest,
        observer: StreamObserver<ConsultaChavePixResponse>
    ){
        val response: ConsultaChavePixResponse = service.consultaInterna(request.chavePix)
        observer.onNext(response)
        observer.onCompleted()
    }
}