package br.com.zupacademy.shared.constraints

import br.com.zupacademy.chave.ChavePixValidatedProxy
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE
import kotlin.reflect.KClass

@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = [ValidChavePixValidator::class])
annotation class ValidChavePix(
    val message: String = "Chave Pix inválida (\${validatedValue.tipoChave})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidChavePixValidator : ConstraintValidator<ValidChavePix, ChavePixValidatedProxy>{

    override fun isValid(
        chavePix: ChavePixValidatedProxy,
        annotationMetadata: AnnotationValue<ValidChavePix>,
        context: ConstraintValidatorContext
    ): Boolean {
        return chavePix.tipoChave!!.validate(chavePix.chave)
    }
}
