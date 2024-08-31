package ru.clevertec.gittagplugin.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import ru.clevertec.gittagplugin.model.Branch
import ru.clevertec.gittagplugin.service.ExistTagService
import ru.clevertec.gittagplugin.service.NoTagService
import ru.clevertec.gittagplugin.service.ProcessTagService

import java.util.regex.Matcher
import java.util.regex.Pattern

import static ru.clevertec.gittagplugin.util.Constants.*

class PushTagTask extends DefaultTask {

    @Input
    def noTagService = new NoTagService()

    @Input
    def existTagService = new ExistTagService()

    @Input
    def processTagService = new ProcessTagService(project)

    @TaskAction
    void pushTag() {
        def nameBranch = findCurrentNameBranch()
        def tagFromDefineTagTask = project.extensions.getByName(TAG).toString()

        if (tagFromDefineTagTask.empty) {
            if (findGitTags().isEmpty()) {
                def tagTitle = noTagService.createTagName(nameBranch, tagFromDefineTagTask)
                pushTagVersion(tagTitle)
            } else {
                setTagAccordingBranch(nameBranch)
            }
        } else {
            print "Tag is already set: " + tagFromDefineTagTask
        }
    }

    void setTagAccordingBranch(String branchName) {
        if (branchName.equalsIgnoreCase(Branch.MASTER.toString())) {
            def latestTag = findLatestTagsInBranch(branchName)
            def tagTitle = existTagService.createTagName(branchName, latestTag)
            pushTagVersion(tagTitle)
        } else {
            def latestTagVersion = findLatestIncrementTag()
            def preliminaryTag = setPostfix(latestTagVersion, branchName)
            def fixedTag = updateMajorVersionIfNotMaster(branchName, preliminaryTag)
            def tagTitle = existTagService.createTagName(branchName, fixedTag)
            pushTagVersion(tagTitle)
        }
    }

    private String updateMajorVersionIfNotMaster(String branchName, String latestTagVersion) {
        if (branchName != Branch.MASTER.toString()) {
            def lastTagMaster = findLatestTagMajorVersion()
            def latestMajorVersion = extractMajorVersion(lastTagMaster)
            def currentMajorVersion = extractMajorVersion(latestTagVersion)
            if (latestMajorVersion != 0 && latestMajorVersion != currentMajorVersion) {
                return setMajorVersion(latestTagVersion, latestMajorVersion)
            }
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
        def tagsOutput = processTagService.executeGitCommand(GIT, TAG)
        if (tagsOutput.isEmpty()) {
            return Collections.emptyList()
        }
        def tags = tagsOutput.split('\n').toList()
        return tags.findAll { it.trim() }
    }

//    private List<String> findGitTags() {
//        def execOutput = new ByteArrayOutputStream()
//        def result = project.exec {
//            commandLine GIT, TAG
//            standardOutput = execOutput
//            errorOutput = new ByteArrayOutputStream()
//            ignoreExitValue = true
//        }
//        if (result.exitValue != 0) {
//            return Collections.emptyList()
//        }
//        def tags = execOutput.toString().trim().split('\n').toList()
//        return tags.findAll { it.trim() }
//    }

    private static int extractMajorVersion(String tag) {
        Pattern pattern = Pattern.compile("v(\\d+)\\.\\d+(-\\w+)?")
        Matcher matcher = pattern.matcher(tag)
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1))
        }
        return 0
    }

    private static String setMajorVersion(String tag, Integer majorVersion) {
        return tag.replaceAll("v\\d+\\.\\d+", "v" + majorVersion + ".0");
    }

    private void pushTagVersion(String tagTitle) {
        pushTagToLocal(tagTitle)
        pushTagToOrigin(tagTitle)
    }

    private void pushTagToLocal(String tagTitle) {
        processTagService.executeGitCommand(GIT, TAG, tagTitle)
        println "The current commit is assigned tag $tagTitle"
    }

    private void pushTagToOrigin(String tagTitle) {
        processTagService.executeGitCommand(GIT, PUSH, ORIGIN, tagTitle)
    }

//    private void pushTagToLocal(String tagTitle) {
//        def execOutput = new ByteArrayOutputStream()
//        project.exec {
//            commandLine GIT, TAG, tagTitle
//            standardOutput = execOutput
//        }
//        print "The current commit is assigned tag $tagTitle"
//    }
//
//    private void pushTagToOrigin(String tagTitle) {
//        def execOutput = new ByteArrayOutputStream()
//        project.exec {
//            commandLine GIT, PUSH, ORIGIN, tagTitle
//            standardOutput = execOutput
//        }
//    }

    private static String setPostfix(String latestTagVersion, String branchName) {
        String postfix = ""

        if (branchName.equalsIgnoreCase("stage")) {
            postfix = "-rc"
        } else if (!branchName.equalsIgnoreCase("master") &&
                !branchName.equalsIgnoreCase("dev") &&
                !branchName.equalsIgnoreCase("qa")) {
            postfix = "-SNAPSHOT"
        }
        return latestTagVersion + postfix
    }

    private String findLatestTagsInBranch(String nameBranch) {
        def command = "git describe --tags \$(git rev-list --tags --max-count=1 --branches=${nameBranch})"
        return processTagService.executeGitCommand('sh', '-c', command)
    }

//    private String findLatestTagsInBranch(String nameBranch) {
//        def execOutput = new ByteArrayOutputStream()
//        def result = project.exec {
//            commandLine 'sh', '-c', "git describe --tags \$(git rev-list --tags --max-count=1 --branches=${nameBranch})"
//            standardOutput = execOutput
//            errorOutput = new ByteArrayOutputStream()
//            ignoreExitValue = true
//        }
//        if (result.exitValue != 0) {
//            return ""
//        }
//        return execOutput.toString()
//    }
    private String findLatestIncrementTag() {
        def tagsOutput = processTagService.executeGitCommand('sh', '-c', "git tag --list 'v*'")
        if (tagsOutput.isEmpty()) {
            return ""
        }
        def tags = tagsOutput.split('\n')
        def incrementalTags = tags.findAll { it ==~ /v\d+\.\d+(-\w+)?$/ }
        if (incrementalTags.isEmpty()) {
            return ""
        }
        incrementalTags.sort { a, b ->
            def (aMajor, aMinor) = a.replaceAll(/v(\d+)\.(\d+).*/, '$1 $2').split(' ').collect { it.toInteger() }
            def (bMajor, bMinor) = b.replaceAll(/v(\d+)\.(\d+).*/, '$1 $2').split(' ').collect { it.toInteger() }
            return aMajor <=> bMajor ?: aMinor <=> bMinor
        }

        def latestTag = incrementalTags.last()
        return latestTag.replaceAll(/(-\w+)?$/, "")
    }


//    private String findLatestIncrementTag() {
//        def tagsOutput = processTagService.executeGitCommand('sh', '-c', "git tag --list 'v*'")
//        if (tagsOutput.isEmpty()) {
//            return ""
//        }
////        def execOutput = new ByteArrayOutputStream()
////        def result = project.exec {
////            commandLine 'sh', '-c', "git tag --list 'v*'"
////            standardOutput = execOutput
////            errorOutput = new ByteArrayOutputStream()
////            ignoreExitValue = true
////        }
////        if (result.exitValue != 0) {
////            return ""
////        }
//        def tags = execOutput.toString().trim().split('\n')
//        def incrementalTags = tags.findAll { it ==~ /v\d+\.\d+(-\w+)?$/ }
//        if (incrementalTags.isEmpty()) {
//            return ""
//        }
//        incrementalTags.sort { a, b ->
//            def (aMajor, aMinor) = a.replaceAll(/v(\d+)\.(\d+).*/, '$1 $2').split(' ').collect { it.toInteger() }
//            def (bMajor, bMinor) = b.replaceAll(/v(\d+)\.(\d+).*/, '$1 $2').split(' ').collect { it.toInteger() }
//            return aMajor <=> bMajor ?: aMinor <=> bMinor
//        }
//
//        def latestTag = incrementalTags.last()
//        return latestTag.replaceAll(/(-\w+)?$/, "")
//    }

    private String findCurrentNameBranch() {
        return processTagService.executeGitCommand(GIT, BRANCH, SHOW_CURRENT)
    }

//    private String findCurrentNameBranch() {
//        def execOutput = new ByteArrayOutputStream()
//        project.exec {
//            commandLine GIT, BRANCH, SHOW_CURRENT
//            standardOutput = execOutput
//            errorOutput = new ByteArrayOutputStream()
//            ignoreExitValue = true
//        }
//        return execOutput.toString().trim()
//    }

//    private String executeGitCommand(String... command) {
//        def execOutput = new ByteArrayOutputStream()
//        def result = project.exec {
//            commandLine command
//            standardOutput = execOutput
//            errorOutput = new ByteArrayOutputStream()
//            ignoreExitValue = true
//        }
//        if (result.exitValue != 0) {
//            return ""
//        }
//        return execOutput.toString().trim()
//    }
}