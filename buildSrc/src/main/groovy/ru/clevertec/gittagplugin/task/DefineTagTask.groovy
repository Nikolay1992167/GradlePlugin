package ru.clevertec.gittagplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.exception.UncommittedChangesException

import static ru.clevertec.gittagplugin.util.Constants.DESCRIBE
import static ru.clevertec.gittagplugin.util.Constants.DIFF
import static ru.clevertec.gittagplugin.util.Constants.EXACT_MATCH
import static ru.clevertec.gittagplugin.util.Constants.GIT
import static ru.clevertec.gittagplugin.util.Constants.TAG
import static ru.clevertec.gittagplugin.util.Constants.TAGS

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
            project.extensions.add(TAG, "")
        } else {
            println tagVersion
            project.extensions.add(TAG, tagVersion)
        }
    }

    private String findCurrentTagVersion() {
        return executeGitCommand(GIT, DESCRIBE, EXACT_MATCH, TAGS)
    }

    private String findUncommittedChanges() {
        return executeGitCommand(GIT, DIFF)
    }

    private String executeGitCommand(String... command) {
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