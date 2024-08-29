package ru.clevertec.gittagplugin

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import ru.clevertec.gittagplugin.extension.PushTagExtension
import ru.clevertec.gittagplugin.task.CheckInstallGitTask
import ru.clevertec.gittagplugin.task.PushTagTask
import ru.clevertec.gittagplugin.task.DefineTagTask

import static ru.clevertec.gittagplugin.util.Constants.CHECK_INSTALL_GIT;
import static ru.clevertec.gittagplugin.util.Constants.PUSH_TAG;
import static ru.clevertec.gittagplugin.util.Constants.DEFINE_TAG;
import static ru.clevertec.gittagplugin.util.Constants.GIT;
import static ru.clevertec.gittagplugin.util.Constants.LOGO;

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
//        project.tasks.register("checkInstallGit", CheckInstallGitTask){
//            group = GIT
//            doFirst {
//                logger.warn LOGO
//            }
//        }
//        project.tasks.register(DEFINE_TAG, DefineTagTask) {
//            group = GIT
//            doFirst {
//                logger.warn LOGO
//            }
//        }
        project.tasks.register(PUSH_TAG, PushTagTask) {
            group = GIT
            dependsOn DEFINE_TAG;
            doFirst {
                logger.warn LOGO
            }
        }
    }
}