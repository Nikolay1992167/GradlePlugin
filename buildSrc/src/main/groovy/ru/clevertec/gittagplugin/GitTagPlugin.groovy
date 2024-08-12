package ru.clevertec.gittagplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.clevertec.gittagplugin.model.PushTagExtension
import ru.clevertec.gittagplugin.service.GitTagService

import static ru.clevertec.gittagplugin.util.Constants.PUSH_TAG;
import static ru.clevertec.gittagplugin.util.Constants.GIT;
import static ru.clevertec.gittagplugin.util.Constants.LOGO;

class GitTagPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create(PUSH_TAG, PushTagExtension)
        project.tasks.register(PUSH_TAG, GitTagService) {
            group = GIT
            doFirst {
                logger.warn LOGO
            }
        }
    }
}