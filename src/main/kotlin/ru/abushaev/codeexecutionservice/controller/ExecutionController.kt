package ru.abushaev.codeexecutionservice.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.abushaev.codeexecutionservice.entity.CodeExecutionRequest
import ru.abushaev.codeexecutionservice.service.ExecutionService
import java.nio.file.Paths

@RestController
@RequestMapping("/api/execute")
class ExecutionController(private val executionService: ExecutionService) {

    @PostMapping("/run")
    fun executeCode(@RequestBody request: CodeExecutionRequest): ResponseEntity<String> {
        val sourceCodePath = Paths.get(request.sourceCodePath)
        val containerOutputPath = Paths.get(request.containerOutputPath)

        val str = "adasda"
        str.length

        return try {
            val executionLogs = executionService.executeCode(sourceCodePath, containerOutputPath)
            ResponseEntity.ok(executionLogs)
        } catch (ex: Exception) {
            ResponseEntity.status(500).body("Ошибка выполнения кода: ${ex.message}")
        }
    }
}