package ru.clevertec.gittagplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.factory.impl.ExistTagService
import ru.clevertec.gittagplugin.factory.impl.NoTagService

class PushTagTask extends DefaultTask {

    @Input
    def noTagService = new NoTagService()

    @Input
    def existTagService = new ExistTagService()

    @TaskAction
    void pushTag() {
//        def latestTagVersion = findLatestTagVersion()
        def branchName = findCurrentBranchName()
        println branchName
        def tagFromTask = project.extensions.getByName('tag').toString()
        if (tagFromTask.empty) {
            def latestTagVersion = findLatestTagsInBranch(branchName)
            if (latestTagVersion.empty) {
                def tagTitle = noTagService.createTagName(branchName, latestTagVersion)
                pushTagToLocal(tagTitle)
                pushTagToOrigin(tagTitle)
            } else {
                existTagService.createTagName(branchName, latestTagVersion)
            }
        } else {
            println tagFromTask
        }

    }

    void pushTagToLocal(String tagTitle) {
        def execOutput = new ByteArrayOutputStream()
        project.exec {
            commandLine 'git', 'tag', tagTitle
            standardOutput = execOutput
        }
        logger.warn "The current commit is assigned tag $tagTitle"
    }

    void pushTagToOrigin(String tagTitle) {
        def execOutput = new ByteArrayOutputStream()
        project.exec {
            commandLine 'git', 'push', 'origin', tagTitle
            standardOutput = execOutput
        }
    }

    String findLatestTagsInBranch(String nameBranch) {
        def execOutput = new ByteArrayOutputStream()
        def result = project.exec {
            commandLine 'sh', '-c', "git describe --tags \$(git rev-list --tags --max-count=1 --branches=${nameBranch})"
            standardOutput = execOutput
            errorOutput = errorOutput
            ignoreExitValue = true
        }
        if (result.exitValue != 0) {
            return ""
        }
        return execOutput.toString()
    }

    String findCurrentBranchName() {
        def execOutput = new ByteArrayOutputStream()
        project.exec {
            commandLine 'git', 'branch', '--show-current'
            standardOutput = execOutput
        }
        return execOutput.toString()
    }

    String findLatestTagVersion() {
        def execOutput = new ByteArrayOutputStream()
        def result = project.exec {
            commandLine 'git', 'describe', '--tags', '--abbrev=0'
            standardOutput = execOutput
            errorOutput = errorOutput
            ignoreExitValue = true
        }
        if (result.exitValue != 0) {
            return ""
        }
        return execOutput.toString()
    }


//        if (latestTagVersion.isEmpty()) {
//            def tagName = noTagExistsFactory.createTagName(branchName, latestTagVersion)
//            gitRepository.pushTagToLocal(tagName)
//            gitRepository.pushTagToOrigin(tagName)
//            logger.warn "The current commit is assigned tag $tagName"
//            return
//        }
//        def currentTagVersion = gitRepository.findCurrentTagVersion()
//        if (!latestTagVersion.isEmpty()
//                && !currentTagVersion.isEmpty()
//                && latestTagVersion == currentTagVersion) {
//            throw new TagAlreadyException("The current state of the project is already tagged $currentTagVersion by git")
//        } else {
//            def tagName = tagExistsFactory.createTagName(branchName, latestTagVersion)
//            gitRepository.pushTagToLocal(tagName)
//            gitRepository.pushTagToOrigin(tagName)
//            logger.warn "The current commit is assigned tag $tagName"
//        }
}
