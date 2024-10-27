package com.json2ts.activity

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.json2ts.generator.Notifier
import com.json2ts.state.NotificationOnFirstRun

class StartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        val service = NotificationOnFirstRun.getInstance()
        if (!service.state.isFirstRun) {
            return
        }
        Notifier.notifyWithHyperlink(project)
        service.state.isFirstRun = false
    }
}
