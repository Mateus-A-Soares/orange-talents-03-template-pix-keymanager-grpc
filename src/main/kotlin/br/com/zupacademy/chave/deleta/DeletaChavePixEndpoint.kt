package br.com.zupacademy.chave.deleta

import br.com.zupacademy.DeletaChavePixRequest
import br.com.zupacademy.DeletaChavePixResponse
import br.com.zupacademy.DeletaChavePixServiceGrpc
import br.com.zupacademy.shared.exceptions.handlers.config.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class DeletaChavePixEndpoint(@Inject val chavePixService: DeleteChavePixService) :
    DeletaChavePixServiceGrpc.DeletaChavePixServiceImplBase() {

    override fun deleta(request: DeletaChavePixRequest, observer: StreamObserver<DeletaChavePixResponse>) {
        chavePixService.deleta(clienteId = request.clienteId, chave = request.chavePixId)
        observer.onNext(DeletaChavePixResponse.newBuilder().build())
        observer.onCompleted()
    }
}