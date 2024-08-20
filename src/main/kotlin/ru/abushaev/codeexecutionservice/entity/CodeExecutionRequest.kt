package ru.abushaev.codeexecutionservice.entity

data class CodeExecutionRequest(
    val sourceCodePath: String,
    val containerOutputPath: String
)
