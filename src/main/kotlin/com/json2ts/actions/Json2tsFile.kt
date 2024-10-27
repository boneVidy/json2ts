package com.json2ts.actions
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import com.json2ts.generator.GeneratorDelegate
import com.json2ts.generator.Notifier
import com.json2ts.parser.typescript.ParseType
import com.json2ts.views.Json2TsForm

class Json2tsFile : AnAction() {
    private val generatorDelegate = GeneratorDelegate()

    override fun actionPerformed(event: AnActionEvent) {
        DialogBuilder().apply {
            val form = Json2TsForm().apply {
                setOnGenerateListener(object : Json2TsForm.OnGenerateClicked {
                    override fun onClicked(rootName: String, json: String, parseType: ParseType) {
                        window.dispose()
                        generatorDelegate.runGenerationToFile(event, json, rootName, parseType)
                    }
                })
                setOnFormatListener(object : Json2TsForm.OnFormatClicked {
                    override fun format(json: String): String {
                        val jsonString = try {
                            val rootJsonElement = JsonParser.parseString(json)
                            val gson = GsonBuilder().setPrettyPrinting().create()
                            gson.toJson(rootJsonElement)
                        } catch (e: JsonParseException) {
                            Notifier.notifyException(e, event.project!!)
                            null
                        }
                        return jsonString ?: json
                    }
                })
            }
            setCenterPanel(form.rootView)
            setTitle("Create A Ts File From Json")
            removeAllActions()
            show()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
}
