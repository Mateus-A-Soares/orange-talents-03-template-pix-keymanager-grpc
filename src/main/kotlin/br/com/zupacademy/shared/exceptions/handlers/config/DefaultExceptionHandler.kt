package br.com.zupacademy.shared.exceptions.handlers.config

import io.grpc.Status
import br.com.zupacademy.shared.exceptions.handlers.config.ExceptionHandler.StatusWithDetails

class DefaultExceptionHandler : ExceptionHandler<Exception> {

    override fun supports(e: Exception): Boolean {
        return true
    }

    override fun handle(e: Exception): StatusWithDetails {
        e.printStackTrace()
        return StatusWithDetails(Status.UNKNOWN.withCause(e))
    }
}
