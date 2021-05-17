package br.com.zupacademy.Mateus

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("br.com.zupacademy.Mateus")
		.start()
}

