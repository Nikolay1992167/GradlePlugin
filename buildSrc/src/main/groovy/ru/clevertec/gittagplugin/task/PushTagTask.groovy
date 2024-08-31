package ru.clevertec.gittagplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.model.Branch
import ru.clevertec.gittagplugin.service.ExistTagService
import ru.clevertec.gittagplugin.service.ProcessTagService
import ru.clevertec.gittagplugin.service.NoTagService

import static ru.clevertec.gittagplugin.util.Constants.TAG

class PushTagTask extends DefaultTask {
    @Input
    def noTagService = new NoTagService()

    @Input
    def existTagService = new ExistTagService()

    @Input
    def gitTagService = new ProcessTagService(project)

    @TaskAction
    void pushTag() {
        def branchName = gitTagService.findCurrentBranchName()
        def tagFromDefineTagTask = project.extensions.getByName(TAG).toString()

        if (tagFromDefineTagTask.empty) {
            if (gitTagService.findGitTags().isEmpty()) {
                def tagTitle = noTagService.createTagName(branchName, tagFromDefineTagTask)
                pushTagVersion(tagTitle)
            } else {
                setTagAccordingBranch(branchName, gitTagService)
            }
        } else {
            print "Tag is already installed: " + tagFromDefineTagTask
        }
    }

    void setTagAccordingBranch(String branchName, ProcessTagService gitTagService) {
        if (branchName.equalsIgnoreCase(Branch.MASTER.toString())) {
            def latestTag = gitTagService.findLatestTagsInBranch(branchName)
            def tagTitle = existTagService.createTagName(branchName, latestTag)
            pushTagVersion(tagTitle)
        } else {
            def latestTagVersion = gitTagService.findLatestIncrementTag()
            def preliminaryTag = ProcessTagService.setPostfix(latestTagVersion, branchName)
            def fixedTag = updateMajorVersionIfNotMaster(branchName, preliminaryTag, gitTagService)
            def tagTitle = existTagService.createTagName(branchName, fixedTag)
            pushTagVersion(tagTitle)
        }
    }

    private static String updateMajorVersionIfNotMaster(String branchName, String latestTagVersion, ProcessTagService gitTagService) {
        if (branchName != Branch.MASTER.toString()) {
            def lastTagMaster = gitTagService.findLatestTagMajorVersion()
            def majorVersion = ProcessTagService.extractMajorVersion(lastTagMaster)
            return ProcessTagService.setMajorVersion(latestTagVersion, majorVersion)
        }
        return latestTagVersion
    }

    private void pushTagVersion(String tagTitle) {
        pushTagToLocal(tagTitle)
        pushTagToOrigin(tagTitle)
    }

    private void pushTagToLocal(String tagTitle) {
        def execOutput = new ByteArrayOutputStream()
        project.exec {
            commandLine 'git', 'tag', tagTitle
            standardOutput = execOutput
        }
        print "The current commit is assigned tag $tagTitle"
    }

    private void pushTagToOrigin(String tagTitle) {
        def execOutput = new ByteArrayOutputStream()
        project.exec {
            commandLine 'git', 'push', 'origin', tagTitle
            standardOutput = execOutput
        }
    }
}