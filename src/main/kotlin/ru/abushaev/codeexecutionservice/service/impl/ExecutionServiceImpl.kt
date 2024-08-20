package ru.abushaev.codeexecutionservice.service.impl

import org.springframework.stereotype.Service
import ru.abushaev.codeexecutionservice.service.ExecutionService
import java.nio.file.Files
import java.nio.file.Path

@Service
class ExecutionServiceImpl(private val dockerClient: CodeExecutionDockerClient) : ExecutionService {

    override fun executeCode(sourceCodePath: Path, containerOutputPath: Path): String {
        if(Files.exists(sourceCodePath)) {
            println("Файл существует: $sourceCodePath")
        } else {
            println("Файл не найден: $sourceCodePath")
        }

        // Pull the required image
        dockerClient.pullImage("openjdk:11")

        // Create and start the container
        val containerId = dockerClient.createAndStartContainer("openjdk:11", sourceCodePath, containerOutputPath)

        // Wait for the container to complete execution
        val exitCode = dockerClient.waitForContainer(containerId)

        // Retrieve the logs
        val logs = dockerClient.getContainerLogs(containerId)

        // Clean up the container
        dockerClient.removeContainer(containerId)

        // Return the execution logs
        return logs
    }
}