package br.com.zupacademy.itauerp

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itauErpService.url}")
interface ItauErpClient {

    @Get("\${itauErpService.cliente.contas.path}{?tipo}")
    fun buscaPorContaTipo(@PathVariable("id") clienteId : String, @QueryValue("tipo") tipoConta : String) : HttpResponse<BuscarContaTipoItauErpResponse>
}
