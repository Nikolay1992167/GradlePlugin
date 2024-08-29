package ru.clevertec.gittagplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.exception.UncommittedChangesException

class DefineTagTask extends DefaultTask {

    @TaskAction
    void defineTag() {
        def tagVersion = findCurrentTagVersion()
        if (project.pushTag.checkUncommitted) {
            def uncommitted = findUncommittedChanges()
            if (!uncommitted.isEmpty()) {
                def exceptionMessage = tagVersion.isEmpty()
                        ? 'Detected uncommitted changes in repository without tags'
                        : "Detected uncommitted changes in :\n$tagVersion" + '.uncommitted'
                throw new UncommittedChangesException(exceptionMessage)
            }
        }
        if (tagVersion.empty) {
            println "Tag is not exist!"
            project.extensions.add('tag', "")
        } else {
            project.extensions.add('tag', tagVersion)
        }
    }

    private String findCurrentTagVersion() {
        def execOutput = new ByteArrayOutputStream()
        def result = project.exec {
            commandLine 'git', 'describe', '--tags'
            standardOutput = execOutput
            errorOutput = errorOutput
            ignoreExitValue = true
        }
        if (result.exitValue != 0) {
            return ""
        }
        return execOutput.toString()
    }

    private String findUncommittedChanges() {
        def execOutput = new ByteArrayOutputStream()
        def result = project.exec {
            commandLine 'git', 'diff'
            standardOutput = execOutput
            errorOutput = errorOutput
            ignoreExitValue = true
        }
        if (result.exitValue != 0) {
            return ""
        }
        return execOutput.toString().trim()
    }
}