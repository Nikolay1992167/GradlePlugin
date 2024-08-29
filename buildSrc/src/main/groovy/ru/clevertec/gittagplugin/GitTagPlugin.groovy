package ru.clevertec.gittagplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.clevertec.gittagplugin.extension.PushTagExtension
import ru.clevertec.gittagplugin.task.CheckInstallGitTask
import ru.clevertec.gittagplugin.task.DefineTagTask
import ru.clevertec.gittagplugin.task.PushTagTask

import static ru.clevertec.gittagplugin.util.Constants.*

class GitTagPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create(PUSH_TAG, PushTagExtension)

        project.tasks.register(CHECK_INSTALL_GIT, CheckInstallGitTask) {
            group = GIT
            doFirst {
                logger.warn LOGO
            }
        }

        project.tasks.register(DEFINE_TAG, DefineTagTask) {
            group = GIT
            dependsOn CHECK_INSTALL_GIT
            doFirst {
                logger.warn LOGO
            }
        }

        project.tasks.register(PUSH_TAG, PushTagTask) {
            group = GIT
            dependsOn DEFINE_TAG;
            doFirst {
                logger.warn LOGO
            }
        }
    }
}