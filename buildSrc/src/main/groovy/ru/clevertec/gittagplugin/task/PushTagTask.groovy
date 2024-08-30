package ru.clevertec.gittagplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.model.Branch
import ru.clevertec.gittagplugin.service.ExistTagService
import ru.clevertec.gittagplugin.service.NoTagService

import static ru.clevertec.gittagplugin.util.Constants.*

class PushTagTask extends DefaultTask {

    @Input
    def noTagService = new NoTagService()

    @Input
    def existTagService = new ExistTagService()

    @TaskAction
    void pushTag() {
        def branchName = findCurrentBranchName()
        def tagFromDefineTagTask = project.extensions.getByName(TAG).toString()

        if (tagFromDefineTagTask.empty) {
            def latestTagVersion = findLatestTagsInBranch(branchName)
            handleTag(branchName, latestTagVersion)
        } else {
            print tagFromDefineTagTask
        }
    }

    private void handleTag(String branchName, String latestTagVersion) {
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

    private void pushTagToLocal(String tagTitle) {
        def execOutput = new ByteArrayOutputStream()
        project.exec {
            commandLine GIT, TAG, tagTitle
            standardOutput = execOutput
        }
        print "The current commit is assigned tag $tagTitle"
    }

    private void pushTagToOrigin(String tagTitle) {
        def execOutput = new ByteArrayOutputStream()
        project.exec {
            commandLine GIT, PUSH, ORIGIN, tagTitle
            standardOutput = execOutput
        }
    }

    private String findLatestTagsInBranch(String nameBranch) {
        def execOutput = new ByteArrayOutputStream()
        def result = project.exec {
            commandLine 'sh', '-c', "git describe --tags \$(git rev-list --tags --max-count=1 --branches=${nameBranch})"
            standardOutput = execOutput
            errorOutput = new ByteArrayOutputStream()
            ignoreExitValue = true
        }
        if (result.exitValue != 0) {
            return ""
        }
        return execOutput.toString()
    }

    private String findCurrentBranchName() {
        def execOutput = new ByteArrayOutputStream()
        project.exec {
            commandLine GIT, BRANCH, SHOW_CURRENT
            standardOutput = execOutput
            errorOutput = new ByteArrayOutputStream()
            ignoreExitValue = true
        }
        return execOutput.toString().trim()
    }
}