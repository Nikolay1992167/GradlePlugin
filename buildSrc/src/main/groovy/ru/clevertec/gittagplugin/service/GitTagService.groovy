package ru.clevertec.gittagplugin.service

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.exception.TagAlreadyException
import ru.clevertec.gittagplugin.exception.GitNotFoundException
import ru.clevertec.gittagplugin.exception.UncommittedChangesException

import ru.clevertec.gittagplugin.repository.impl.GitRepositoryImpl

class GitTagService extends DefaultTask {

//    @Input
//    def gitRepository = new GitRepositoryImpl()
//    @Input
//    def noTagExistsFactory = new NoTagExistsFactory()
//    @Input
//    def tagExistsFactory = new TagExistsFactory()
//
//    @TaskAction
//    void pushTag() {
//        try {
//            gitRepository.findGitVersion()
//        } catch (IOException e) {
//            logger.error e.getMessage()
//            throw new GitNotFoundException('Git not found on current device')
//        }
//        if (project.pushTag.checkUncommitted) {
//            def uncommitted = gitRepository.findUncommittedChanges()
//            if (!uncommitted.isEmpty()) {
//                def tagVersion = gitRepository.findCurrentTagVersion()
//                def exceptionMessage = tagVersion.isEmpty()
//                        ? 'Detected uncommitted changes in repository without tags'
//                        : "Detected uncommitted changes in :\n$tagVersion" + '.uncommitted'
//                throw new UncommittedChangesException(exceptionMessage)
//            }
//        }
//        def latestTagVersion = gitRepository.findLatestTagVersion()
//        def branchName = gitRepository.findCurrentBranchName()
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
//    }
}