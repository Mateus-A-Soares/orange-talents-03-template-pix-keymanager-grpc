package br.com.zupacademy.chave.cadastro.listeners

import br.com.zupacademy.bcb.BcbClient
import br.com.zupacademy.bcb.CadastraChavePixBcbRequest
import br.com.zupacademy.bcb.OwnerBcbRequest
import br.com.zupacademy.chave.cadastro.CadastraChaveEvent
import br.com.zupacademy.itauerp.BuscarContaTipoItauErpResponse
import br.com.zupacademy.itauerp.ItauErpClient
import br.com.zupacademy.shared.exceptions.FieldNotFoundException
import com.google.rpc.Code
import io.micronaut.core.annotation.Order
import io.micronaut.runtime.event.annotation.EventListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Order(2)
open class PersisteChaveServicoBcbListener(@Inject private val bcbClient: BcbClient) {

    @EventListener
    open fun onCadastraChaveEvent(event: CadastraChaveEvent) {
        bcbClient.cadastraChave(
            CadastraChavePixBcbRequest.of(
                chave = event.chavePix,
                owner = OwnerBcbRequest.of(event.itauClientResponse!!.titular)
            )
        )
    }
}