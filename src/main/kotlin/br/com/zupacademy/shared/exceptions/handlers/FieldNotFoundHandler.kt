package br.com.zupacademy.shared.exceptions.handlers

import br.com.zupacademy.shared.exceptions.FieldNotFoundException
import br.com.zupacademy.shared.exceptions.handlers.config.ExceptionHandler
import br.com.zupacademy.shared.exceptions.handlers.config.ExceptionHandler.StatusWithDetails
import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton

@Singleton
class FieldNotFoundHandler : ExceptionHandler<FieldNotFoundException> {

    override fun supports(e: Exception): Boolean {
        return e is FieldNotFoundException
    }

    override fun handle(e: FieldNotFoundException): StatusWithDetails {
        return StatusWithDetails(
            Status.newBuilder()
                .setCode(e.rpcCode.number)
                .setMessage(e.message)
                .build()
        )
    }
}
