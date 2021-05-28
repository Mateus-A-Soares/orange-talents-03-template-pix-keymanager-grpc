package br.com.zupacademy.chave.cadastro.listeners

import br.com.zupacademy.bcb.*
import br.com.zupacademy.chave.cadastro.CadastraChaveEvent
import br.com.zupacademy.shared.exceptions.ApiException
import br.com.zupacademy.shared.exceptions.UniqueFieldAlreadyExistsException
import io.micronaut.core.annotation.Order
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.event.annotation.EventListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Order(2)
open class PersisteChaveServicoBcbListener(@Inject private val bcbClient: BcbClient) {

    @EventListener
    open fun onCadastraChaveEvent(event: CadastraChaveEvent) {
        try {
            val bcbResponse = bcbClient.cadastraChave(
                CadastraChavePixBcbRequest.of(
                    chave = event.validatedProxy,
                    bankAccount = BankAccountBcbRequest.of(event.conta),
                    owner = OwnerBcbRequest.of(event.titularResponse)
                )
            )
            var responseBody = bcbResponse.body()
            if (responseBody.keyType == KeyTypeBcb.RANDOM)
                event.validatedProxy.chave = responseBody.key
        } catch (e: HttpClientResponseException) {
            val errorMessage = e.response.getBody(Problem::class.java)
            errorMessage.ifPresent {
                if (it.detail?.equals("The informed Pix key exists already") == true)
                    throw UniqueFieldAlreadyExistsException(field = "chave", message = it.detail)
            }
            throw ApiException()
        }
    }
}