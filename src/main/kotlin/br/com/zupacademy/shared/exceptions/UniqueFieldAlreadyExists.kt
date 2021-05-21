package br.com.zupacademy.shared.exceptions

class UniqueFieldAlreadyExists(val field: String, override var message: String = "$field já tem o valor cadastrado") : ApiException()
