package com.json2ts.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import com.json2ts.generator.GeneratorDelegate
import com.json2ts.parser.ParseType
import com.json2ts.views.Json2TsForm

class Json2ts : AnAction() {
    private val generatorDelegate = GeneratorDelegate()

    override fun actionPerformed(event: AnActionEvent) {
        DialogBuilder().apply {
            val form = Json2TsForm().apply {
                setOnGenerateListener(object : Json2TsForm.OnGenerateClicked {
                    override fun onClicked(rootName: String, json: String, parseType: ParseType) {
                        window.dispose()
                        generatorDelegate.runGeneration(event, json, rootName, parseType)
                    }
                })
            }
            setCenterPanel(form.rootView)
            setTitle("Json2Ts")
            removeAllActions()
            show()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
    }
}
