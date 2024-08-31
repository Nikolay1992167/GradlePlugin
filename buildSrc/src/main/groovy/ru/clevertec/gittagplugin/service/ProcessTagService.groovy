package ru.clevertec.gittagplugin.service

import org.gradle.api.Project

import java.util.regex.Matcher
import java.util.regex.Pattern

class ProcessTagService {
    Project project

    ProcessTagService(Project project) {
        this.project = project
    }

    List<String> findGitTags() {
        def execOutput = new ByteArrayOutputStream()
        def result = project.exec {
            commandLine 'git', 'tag'
            standardOutput = execOutput
            errorOutput = new ByteArrayOutputStream()
            ignoreExitValue = true
        }
        if (result.exitValue != 0) {
            return Collections.emptyList()
        }
        def tags = execOutput.toString().trim().split('\n').toList()
        return tags.findAll { it.trim() }
    }

    String findLatestTagsInBranch(String nameBranch) {
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

    String findLatestTagMajorVersion() {
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

    String findLatestIncrementTag() {
        def execOutput = new ByteArrayOutputStream()
        def result = project.exec {
            commandLine 'sh', '-c', "git tag --list 'v*'"
            standardOutput = execOutput
            errorOutput = new ByteArrayOutputStream()
            ignoreExitValue = true
        }
        if (result.exitValue != 0) {
            return ""
        }
        def tags = execOutput.toString().trim().split('\n')
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

    String findCurrentBranchName() {
        def execOutput = new ByteArrayOutputStream()
        project.exec {
            commandLine 'git', 'branch', '--show-current'
            standardOutput = execOutput
            errorOutput = new ByteArrayOutputStream()
            ignoreExitValue = true
        }
        return execOutput.toString().trim()
    }

    static int extractMajorVersion(String tag) {
        Pattern pattern = Pattern.compile("v(\\d+)\\.\\d+")
        Matcher matcher = pattern.matcher(tag)
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1))
        }
        return 0
    }

    static String setMajorVersion(String tag, Integer majorVersion) {
        return tag.replaceAll("v\\d+\\.\\d+", "v" + majorVersion + ".0")
    }

    static String setPostfix(String latestTagVersion, String branchName) {
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
}