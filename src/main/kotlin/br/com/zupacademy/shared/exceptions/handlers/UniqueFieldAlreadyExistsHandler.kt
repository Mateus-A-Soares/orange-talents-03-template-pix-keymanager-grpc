package br.com.zupacademy.shared.exceptions.handlers

import br.com.zupacademy.shared.exceptions.UniqueFieldAlreadyExistsException
import br.com.zupacademy.shared.exceptions.handlers.config.ExceptionHandler
import br.com.zupacademy.shared.exceptions.handlers.config.ExceptionHandler.StatusWithDetails
import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton

@Singleton
class UniqueFieldAlreadyExistsHandler : ExceptionHandler<UniqueFieldAlreadyExistsException> {

    override fun supports(e: Exception): Boolean {
        return e is UniqueFieldAlreadyExistsException
    }

    override fun handle(e: UniqueFieldAlreadyExistsException): StatusWithDetails {
        return StatusWithDetails(
            Status.newBuilder()
                .setCode(Code.ALREADY_EXISTS_VALUE)
                .setMessage(e.message)
                .build()
        )
    }
}
