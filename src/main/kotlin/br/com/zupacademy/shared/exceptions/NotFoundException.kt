package br.com.zupacademy.shared.exceptions

import com.google.rpc.Code

class FieldNotFoundException(val field: String, override var message: String = "$field não foi encontrado", val rpcCode: Code) : ApiException() {
}
