package br.com.zupacademy.shared.exceptions.handlers

import br.com.zupacademy.FieldErrorDetails
import br.com.zupacademy.FieldErrorDetailsList
import br.com.zupacademy.shared.exceptions.handlers.config.ExceptionHandler
import br.com.zupacademy.shared.exceptions.handlers.config.ExceptionHandler.StatusWithDetails
import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.Status
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class ConstraintViolationHandler : ExceptionHandler<ConstraintViolationException> {

    override fun supports(e: Exception): Boolean {
        return e is ConstraintViolationException
    }

    override fun handle(e: ConstraintViolationException): StatusWithDetails {
        val details = FieldErrorDetailsList.newBuilder()
            .addAllFieldErrors(e.constraintViolations.map {
                FieldErrorDetails.newBuilder()
                    .setField(it.propertyPath.last().name ?: "not found")
                    .build()
            })
            .build()

        val statusProto = Status.newBuilder()
            .setCode(Code.INVALID_ARGUMENT_VALUE)
            .setMessage("Dados inválidos")
            .addDetails(Any.pack(details))
            .build()

        return StatusWithDetails(statusProto)
    }
}
