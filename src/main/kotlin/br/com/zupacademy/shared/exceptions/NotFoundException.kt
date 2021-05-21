package br.com.zupacademy.shared.exceptions

class FieldNotFoundException(val field: String, override var message: String = "$field não foi encontrado") : ApiException()
