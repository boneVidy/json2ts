package com.json2ts.state


import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "NotificationOnFirstRun", storages = [Storage("NotificationOnFirstRun.xml")])
class NotificationOnFirstRun : PersistentStateComponent<NotificationOnFirstRun.State> {

    private var state = State()

    class State {
        var isFirstRun: Boolean = true
    }

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): NotificationOnFirstRun {
            return ApplicationManager.getApplication().getService(NotificationOnFirstRun::class.java)
        }
    }


}
