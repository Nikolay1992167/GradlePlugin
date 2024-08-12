package ru.clevertec.gittagplugin.repository.impl

import ru.clevertec.gittagplugin.builder.CommandBuilder
import ru.clevertec.gittagplugin.model.SortOrder
import ru.clevertec.gittagplugin.repository.GitRepository

import static ru.clevertec.gittagplugin.util.Constants.RC
import static ru.clevertec.gittagplugin.util.Constants.SNAPSHOT

class GitRepositoryImpl implements GitRepository {

    @Override
    String findGitVersion() {
        CommandBuilder.builder()
                .git()
                .version()
                .execute()
    }

    @Override
    String findUncommittedChanges() {
        CommandBuilder.builder()
                .git()
                .diff()
                .execute()
    }

    @Override
    String findLatestTagVersion() {
        def result = CommandBuilder.builder()
                .git()
                .describe()
                .tags()
                .abbrev(0)
                .execute()
        if (result.contains("fatal:")) {
            return ""
        }
        return result
    }

    @Override
    String findCurrentTagVersion() {
        def result = CommandBuilder.builder()
                .git()
                .describe()
                .tags()
                .execute()
        if (result.contains("fatal:")) {
            return ""
        }
        return result
    }

    @Override
    String findCurrentBranchName() {
        CommandBuilder.builder()
                .git()
                .branch()
                .showCurrent()
                .execute()
    }

    @Override
    String findLatestDevAndQATagByTagVersion(String tagVersion) {
        CommandBuilder.builder()
                .git()
                .tag()
                .list()
                .command(/${tagVersion.find(/v(\d+)/)}\.*/)
                .sort('version:refname', SortOrder.DESC)
                .execute()
                .lines()
                .filter { !it.endsWith(SNAPSHOT) }
                .filter { !it.endsWith(RC) }
                .findFirst()
                .orElse(tagVersion)
    }

    @Override
    String findLatestSnapshotTagByTagVersion(String tagVersion) {
        CommandBuilder.builder()
                .git()
                .tag()
                .list()
                .command(/${tagVersion.find(/v(\d+)/)}\.*$SNAPSHOT/)
                .sort('version:refname', SortOrder.DESC)
                .execute()
                .lines()
                .findFirst()
                .orElse(tagVersion)
    }

    @Override
    String pushTagToLocal(String tagName) {
        CommandBuilder.builder()
                .git()
                .tag()
                .command(tagName)
                .execute()
    }

    @Override
    String pushTagToOrigin(String tagName) {
        CommandBuilder.builder()
                .git()
                .push()
                .origin()
                .command(tagName)
                .execute()
    }
}