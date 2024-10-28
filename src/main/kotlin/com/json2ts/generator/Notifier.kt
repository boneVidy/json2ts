package com.json2ts.generator

import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

object Notifier {
    private const val INFO_GROUP = "Json2ts info"
    private const val ERROR_GROUP = "Json2ts error"
    fun notifyException(throwable: Throwable, project: Project) {
        val message = if (throwable.message != null) {
            "Json2ts error: ${throwable.message}"
        } else {
            "Json2ts error"
        }

        ApplicationManager.getApplication().invokeLater {
            NotificationGroupManager.getInstance()
                .getNotificationGroup(ERROR_GROUP)
                .createNotification(message, NotificationType.ERROR)
                .notify(project)
        }
    }

    fun notify(message: String, project: Project) {
        ApplicationManager.getApplication().invokeLater {
            NotificationGroupManager.getInstance()
                .getNotificationGroup(INFO_GROUP)
                .createNotification(message, NotificationType.INFORMATION)
                .notify(project)
        }
    }

    fun notifyWithHyperlink(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            NotificationGroupManager.getInstance()
                .getNotificationGroup(INFO_GROUP)
                .createNotification("Thank you for installing Json2Ts. \n" +
                        "Please consider starring our repo", NotificationType.INFORMATION)
                .addAction(object : NotificationAction("Star on GitHub") {
                    override fun actionPerformed(e: AnActionEvent, notification: Notification) {
                        BrowserUtil.browse("https://github.com/boneVidy/json2ts")
                        notification.hideBalloon()
                    }
                })
                .notify(project)
        }
    }
}
