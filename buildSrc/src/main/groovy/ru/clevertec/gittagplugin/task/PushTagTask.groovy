package ru.clevertec.gittagplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.model.Branch
import ru.clevertec.gittagplugin.service.ExistTagService
import ru.clevertec.gittagplugin.service.NoTagService

import java.util.regex.Matcher
import java.util.regex.Pattern

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
            def fixedTag = updateMajorVersionIfNotMaster(branchName, latestTagVersion)
            def tagTitle = existTagService.createTagName(branchName, fixedTag)
            pushTagToLocal(tagTitle)
            pushTagToOrigin(tagTitle)
        }
    }

    private String updateMajorVersionIfNotMaster(String branchName, String latestTagVersion) {
        if (branchName != Branch.MASTER.toString()) {
            def lastTagMaster = findLatestTagMajorVersion()
            def majorVersion = extractMajorVersion(lastTagMaster)
            return setMajorVersion(latestTagVersion, majorVersion)
        }
        return latestTagVersion
    }

    private String findLatestTagMajorVersion() {
        List<String> tags = findGitTags()

        int maxMajorVersion = -1;
        String latestTag = "";

        for (String tag : tags) {
            int majorVersion = extractMajorVersion(tag);
            if (majorVersion > maxMajorVersion) {
                maxMajorVersion = majorVersion;
                latestTag = tag;
            }
        }

        return latestTag;
    }

    private List<String> findGitTags() {
        def execOutput = new ByteArrayOutputStream()
        def result = project.exec {
            commandLine GIT, TAG
            standardOutput = execOutput
            errorOutput = new ByteArrayOutputStream()
            ignoreExitValue = true
        }
        if (result.exitValue != 0) {
            return Collections.emptyList()
        }
        return execOutput.toString().trim().split('\n').toList()
    }

    private static int extractMajorVersion(String tag) {
        Pattern pattern = Pattern.compile("v(\\d+)\\.\\d+");
        Matcher matcher = pattern.matcher(tag);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    private static String setMajorVersion(String tag, Integer majorVersion) {
        return tag.replaceAll("v\\d+\\.\\d+", "v" + majorVersion + ".0");
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