package ru.clevertec.gittagplugin.service

import org.gradle.api.Project

class ProcessTagService {

    Project project

    ProcessTagService(Project project) {
        this.project = project
    }

    String executeGitCommand(String... command) {
        def execOutput = new ByteArrayOutputStream()
        def result = project.exec {
            commandLine command
            standardOutput = execOutput
            errorOutput = new ByteArrayOutputStream()
            ignoreExitValue = true
        }
        if (result.exitValue != 0) {
            return ""
        }
        return execOutput.toString().trim()
    }
}