package ru.clevertec.gittagplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.factory.impl.ExistTagService
import ru.clevertec.gittagplugin.factory.impl.NoTagService
import ru.clevertec.gittagplugin.model.Branch

class PushTagTask extends DefaultTask {

    @Input
    def noTagService = new NoTagService()

    @Input
    def existTagService = new ExistTagService()

    @TaskAction
    void pushTag() {
        def branchName = findCurrentBranchName()
        println branchName
        def tagFromDefineTagTask = project.extensions.getByName('tag').toString()
        if (tagFromDefineTagTask.empty) {
            def latestTagVersion = findLatestTagsInBranch(branchName)
            if (latestTagVersion.empty) {
                def tagTitle = noTagService.createTagName(branchName, latestTagVersion)
                pushTagToLocal(tagTitle)
                pushTagToOrigin(tagTitle)
            } else {
                updateMajorVersionIfNotMaster(branchName, latestTagVersion)
                def tagTitle = existTagService.createTagName(branchName, latestTagVersion)
                pushTagToLocal(tagTitle)
                pushTagToOrigin(tagTitle)
            }
        } else {
            println tagFromDefineTagTask
        }
    }

    private void updateMajorVersionIfNotMaster(String branchName, String latestTagVersion) {
        if (branchName != Branch.MASTER.toString()) {
            def lastTagMaster = findLatestTagsInBranch(Branch.MASTER.toString())
            def majorVersion = findMajorVersion(lastTagMaster)
            setMajorVersion(latestTagVersion, majorVersion)
        }
    }

    private static void setMajorVersion(String tag, String majorVersion) {
        tag.replaceAll("v\\d+", "v" + majorVersion);
    }

    private static String findMajorVersion(String latestTagVersion) {
        return latestTagVersion.replaceAll("v(\\d+)\\.\\d+", "1");
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
}