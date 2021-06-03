package generator

import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager

class MessageDelegate {
    companion object {
        private const val GROUP_LOG = "json2ts logs"
        private const val RESULT_INFO = "json2ts results"
    }

    private val logGroup =
        NotificationGroup(GROUP_LOG, NotificationDisplayType.BALLOON, true)

    private val resultNotification =
        NotificationGroup(RESULT_INFO, NotificationDisplayType.BALLOON, true)

    fun onException(throwable: Throwable) {
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
