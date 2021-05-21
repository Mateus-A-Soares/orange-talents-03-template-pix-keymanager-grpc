package br.com.zupacademy.shared.exceptions.handlers.config

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionHandlerResolver(@Inject private val handlers: List<ExceptionHandler<Exception>>) {
    private var defaultHandler: ExceptionHandler<Exception> = DefaultExceptionHandler()

    constructor(handlers: List<ExceptionHandler<Exception>>, defaultHandler: ExceptionHandler<Exception>) : this(
        handlers
    ) {
        this.defaultHandler = defaultHandler
    }

    fun resolve(e: Exception): ExceptionHandler<Exception> {
        val foundHandlers = handlers.filter { h -> h.supports(e) }
        if (foundHandlers.size > 1)
            throw IllegalStateException("Mais de um handler para a exceção ${e.javaClass.name}, sendo eles: $foundHandlers")
        return foundHandlers.firstOrNull() ?: defaultHandler
    }
}
