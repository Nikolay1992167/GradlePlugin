package ru.clevertec.gittagplugin.factory

interface TagFactory {

    String createTagName(String branchName, String latestTagVersion)
}