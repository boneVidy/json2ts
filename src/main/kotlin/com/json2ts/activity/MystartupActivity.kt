package com.json2ts.activity

import com.intellij.ide.BrowserUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.json2ts.state.NotificationOnFirstRun
import javax.swing.event.HyperlinkEvent

class MyStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        val service = project.getService(NotificationOnFirstRun::class.java)
        if (!service.state.isFirstRun) {
            return
        }
        val notification = Notification(
            "Json2Ts",
            "Welcome to Json2Ts",
            "Thank you for installing Json2Ts. " +
                    "Please consider starring our" +
                    " <a href='https://github.com/boneVidy/json2ts' target='_blank'>GitHub repository</a>.",
            NotificationType.INFORMATION
        ) { _, hyperlinkEvent ->
            if (hyperlinkEvent.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                BrowserUtil.browse(hyperlinkEvent.url)
            }
        }
        Notifications.Bus.notify(notification, project)
        service.state.isFirstRun = false
    }
}
