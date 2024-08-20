package ru.abushaev.codeexecutionservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CodeExecutionServiceApplication

fun main(args: Array<String>) {
    runApplication<CodeExecutionServiceApplication>(*args)
}
