package com.raillylinker.scalable_stomp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ScalableStompApplication

fun main(args: Array<String>) {
	runApplication<ScalableStompApplication>(*args)
}
