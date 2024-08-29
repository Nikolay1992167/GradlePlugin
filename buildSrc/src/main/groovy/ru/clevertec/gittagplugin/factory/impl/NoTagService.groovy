package ru.clevertec.gittagplugin.factory.impl

import ru.clevertec.gittagplugin.model.Branch

import static ru.clevertec.gittagplugin.util.Constants.DEFAULT_TAG_VERSION
import static ru.clevertec.gittagplugin.util.Constants.RC
import static ru.clevertec.gittagplugin.util.Constants.SNAPSHOT

class NoTagService {

    static String createTagName(String branchName, String latestTagVersion) {
        latestTagVersion = DEFAULT_TAG_VERSION
        switch (branchName) {
            case Branch.DEV.getName():
                latestTagVersion
                break
            case Branch.QA.getName():
                latestTagVersion
                break
            case Branch.STAGE.getName():
                "$latestTagVersion$RC"
                break
            case Branch.MASTER.getName():
                latestTagVersion
                break
            default:
                "$latestTagVersion$SNAPSHOT"
                break
        }
    }
}
