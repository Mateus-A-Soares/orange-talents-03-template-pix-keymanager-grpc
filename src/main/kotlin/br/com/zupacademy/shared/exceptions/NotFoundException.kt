package br.com.zupacademy.shared.exceptions

class FieldNotFoundException(val field: String, override var message: String = "$field já tem o valor cadastrado") : ApiException()
