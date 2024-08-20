package ru.abushaev.codeexecutionservice.service.impl

import com.github.dockerjava.api.DockerClient
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.command.CreateContainerResponse
import com.github.dockerjava.api.command.PullImageResultCallback
import com.github.dockerjava.api.command.WaitContainerResultCallback
import com.github.dockerjava.api.model.Bind
import com.github.dockerjava.api.model.Frame
import com.github.dockerjava.api.model.HostConfig
import com.github.dockerjava.api.model.Volume
import com.github.dockerjava.core.DockerClientBuilder
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import java.nio.file.Path
import java.time.Duration

@Component
class CodeExecutionDockerClient {

    private val dockerClient: DockerClient
    private val log = LoggerFactory.getLogger(CodeExecutionDockerClient::class.java)

    init {
        val dockerHost = URI.create("unix:///var/run/docker.sock")

        val dockerHttpClient = ApacheDockerHttpClient.Builder()
            .dockerHost(dockerHost)
            .maxConnections(100)
            .connectionTimeout(Duration.ofSeconds(30))
            .responseTimeout(Duration.ofSeconds(45))
            .build()

        dockerClient = DockerClientBuilder.getInstance()
            .withDockerHttpClient(dockerHttpClient)
            .build()
    }

    fun pullImage(imageName: String) {
        log.info("Pulling Docker image: $imageName")
        dockerClient.pullImageCmd(imageName).exec(PullImageResultCallback()).awaitCompletion()
        log.info("Image $imageName pulled successfully")
    }

    fun createAndStartContainer(
        imageName: String,
        sourceCodePath: Path,
        containerOutputPath: Path
    ): String {
        log.info("Creating Docker container for image: $imageName")

        val sourceVolume = Volume("/usr/src/app/Main.java")

        val outputVolume = Volume("/usr/src/output")

        val hostConfig = HostConfig()
            .withBinds(
                Bind(sourceCodePath.toAbsolutePath().toString(), sourceVolume),
                Bind(containerOutputPath.toAbsolutePath().toString(), outputVolume)
            )

        val container: CreateContainerResponse = dockerClient.createContainerCmd(imageName)
            .withHostConfig(hostConfig)
            .withVolumes(sourceVolume, outputVolume)
            .withCmd("sh", "-c", "javac /usr/src/app/Main.java && java -cp /usr/src/app Main > /usr/src/output/result.txt")
//            .withCmd("sh", "-c", "javac /usr/src/app/Main.java && java -cp /usr/src/app Main")
            .exec()

        val containerId = container.id
        log.info("Docker container $containerId created successfully")

        dockerClient.startContainerCmd(containerId).exec()
        log.info("Docker container $containerId started successfully")

        val containerInfo = dockerClient.inspectContainerCmd(containerId).exec()
        log.info("Container info: $containerInfo")

        return containerId
    }

    fun waitForContainer(containerId: String): Int {
        log.info("Waiting for Docker container $containerId to finish execution")

        val exitCode = dockerClient.waitContainerCmd(containerId).exec(WaitContainerResultCallback()).awaitStatusCode()
        log.info("Docker container $containerId finished with exit code $exitCode")

        return exitCode
    }

    fun removeContainer(containerId: String) {
        log.info("Removing Docker container $containerId")

        dockerClient.removeContainerCmd(containerId).exec()
        log.info("Docker container $containerId removed successfully")
    }

    fun getContainerLogs(containerId: String): String {
        log.info("Retrieving logs for Docker container $containerId")

        val logStream = StringBuilder()

        dockerClient.logContainerCmd(containerId)
            .withStdOut(true)
            .withStdErr(true)
            .exec(object : ResultCallback.Adapter<Frame>() {
                override fun onNext(item: Frame) {
                    logStream.append(String(item.payload))
                }
            }).awaitCompletion() // ждем завершения чтения логов

        log.info("Logs for Docker container $containerId retrieved successfully")
        return logStream.toString()
    }
}