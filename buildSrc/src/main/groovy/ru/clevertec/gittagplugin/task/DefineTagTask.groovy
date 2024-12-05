package ru.clevertec.gittagplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.exception.UncommittedChangesException
import ru.clevertec.gittagplugin.service.ProcessTagService

import static ru.clevertec.gittagplugin.util.Constants.DESCRIBE
import static ru.clevertec.gittagplugin.util.Constants.DIFF
import static ru.clevertec.gittagplugin.util.Constants.EXACT_MATCH
import static ru.clevertec.gittagplugin.util.Constants.GIT
import static ru.clevertec.gittagplugin.util.Constants.TAG
import static ru.clevertec.gittagplugin.util.Constants.TAGS

class DefineTagTask extends DefaultTask {

    @Input
    def processTagService = new ProcessTagService(project)

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
        return processTagService.executeGitCommand(GIT, DESCRIBE, EXACT_MATCH, TAGS)
    }

    private String findUncommittedChanges() {
        return processTagService.executeGitCommand(GIT, DIFF)
    }
}