package ru.clevertec.gittagplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.exception.GitNotFoundException

class CheckInstallGitTask extends DefaultTask {

    @TaskAction
    void checkInstallGit() {
        def execOutput = new ByteArrayOutputStream();
        def result = project.exec {
            commandLine 'git', '--version'
            standardOutput = execOutput
        }
        if (result.exitValue != 0) {
            throw new GitNotFoundException("Git is not installed.")
        }
        println execOutput.toString()
    }
}