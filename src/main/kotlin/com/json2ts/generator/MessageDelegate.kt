package com.json2ts.generator

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager

class MessageDelegate {
    companion object {
        private const val GROUP_LOG = "json2ts logs"
        private const val RESULT_INFO = "json2ts results"
    }

    private val logGroup = NotificationGroupManager.getInstance().getNotificationGroup(GROUP_LOG)

    private val resultNotification = NotificationGroupManager.getInstance().getNotificationGroup(RESULT_INFO)

    fun catchException(throwable: Throwable) {
        val message = if (throwable.message != null) {
            "json2ts error: ${throwable.message}"
        } else {
            "json2ts error"
        }

        sendNotification(
            logGroup.createNotification(message, NotificationType.ERROR)
        )
    }

    fun showMessage(message: String) {
        sendNotification(resultNotification.createNotification(message, NotificationType.INFORMATION))
    }

    private fun sendNotification(notification: Notification) {
        ApplicationManager.getApplication().invokeLater {
            val projects = ProjectManager.getInstance().openProjects
            Notifications.Bus.notify(notification, projects[0])
        }
    }
}
