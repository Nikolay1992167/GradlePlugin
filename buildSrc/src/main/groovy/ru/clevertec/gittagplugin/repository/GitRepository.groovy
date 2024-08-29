package ru.clevertec.gittagplugin.repository

interface GitRepository {

//    String findGitVersion()

//    String findUncommittedChanges()
//
//    String findLatestTagVersion()
//
//    String findCurrentTagVersion()
//
//    String findCurrentBranchName()

    String findLatestDevAndQATagByTagVersion(String tagVersion)

    String findLatestSnapshotTagByTagVersion(String tagVersion)

    String pushTagToLocal(String tagName)

    String pushTagToOrigin(String tagName)

}
