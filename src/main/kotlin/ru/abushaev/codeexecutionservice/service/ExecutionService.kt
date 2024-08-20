package ru.abushaev.codeexecutionservice.service

import java.nio.file.Path


interface ExecutionService {
    fun executeCode(sourceCodePath: Path, containerOutputPath: Path): String
}