package br.com.zupacademy.chave.cadastro.listeners

import br.com.zupacademy.chave.cadastro.CadastraChaveEvent
import br.com.zupacademy.itauerp.ItauErpClient
import br.com.zupacademy.shared.exceptions.FieldNotFoundException
import com.google.rpc.Code
import io.micronaut.core.annotation.Order
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
@Order(1)
class ProcuraContaItauClientListener(@Inject private val itauClient: ItauErpClient) {

    @EventListener
    fun onCadastraChaveEvent(@Valid event: CadastraChaveEvent) {
        val response = itauClient.buscaPorContaTipo(
            clienteId = event.validatedProxy.clienteId!!,
            tipoConta = event.validatedProxy.tipoConta!!.itauErpParameterName
        ).body() ?: throw FieldNotFoundException(
            field = "Conta",
            message = "Conta não foi encontrada",
            rpcCode = Code.FAILED_PRECONDITION
        )
        event.setItauClientResponse(response)
    }
}