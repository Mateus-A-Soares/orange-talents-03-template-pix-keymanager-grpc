package br.com.zupacademy.shared.exceptions.handlers

import br.com.zupacademy.shared.exceptions.handlers.config.ExceptionHandler
import br.com.zupacademy.shared.exceptions.handlers.config.ExceptionHandler.StatusWithDetails
import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton

@Singleton
class IllegalArgumentHandler : ExceptionHandler<IllegalArgumentException> {

    override fun supports(e: Exception): Boolean {
        return e is IllegalArgumentException
    }

    override fun handle(e: IllegalArgumentException): StatusWithDetails {
        val statusProto = Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage(e.message)
            .build()
        return StatusWithDetails(statusProto)
    }
}
