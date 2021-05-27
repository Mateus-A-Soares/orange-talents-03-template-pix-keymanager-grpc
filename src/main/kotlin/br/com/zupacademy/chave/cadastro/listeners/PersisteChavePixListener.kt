package br.com.zupacademy.chave.cadastro.listeners

import br.com.zupacademy.chave.ChavePixRespository
import br.com.zupacademy.chave.cadastro.CadastraChaveEvent
import br.com.zupacademy.shared.exceptions.UniqueFieldAlreadyExistsException
import io.micronaut.core.annotation.Order
import io.micronaut.runtime.event.annotation.EventListener
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
@Order(Int.MAX_VALUE)
open class PersisteChavePixListener(@Inject private val repository: ChavePixRespository) {

    @EventListener
    @Transactional
    open fun onCadastraChaveEvent(event: CadastraChaveEvent) {
        if (repository.existsByChave(event.validatedProxy.chave!!)) throw UniqueFieldAlreadyExistsException(field = "chave")
        repository.save(event.chavePix)
    }
}