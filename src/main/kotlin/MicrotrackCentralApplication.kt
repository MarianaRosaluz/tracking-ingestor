package com.microtrack

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MicrotrackCentralApplication

fun main(args: Array<String>) {
    runApplication<MicrotrackCentralApplication>(*args)
}

