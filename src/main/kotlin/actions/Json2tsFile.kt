package actions

import Json2TsForm
import Json2TsForm.OnGenerateClicked
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import generator.GeneratorDelegate
import parser.ParseType


class Json2tsFile : AnAction() {
    private val generatorDelegate = GeneratorDelegate()

    override fun actionPerformed(event: AnActionEvent) {
        DialogBuilder().apply {
            val form = Json2TsForm().apply {
                setOnGenerateListener(object : OnGenerateClicked {
                    override fun onClicked(rootName: String, json: String, parseType: ParseType) {
                        window.dispose()
                        generatorDelegate.runGenerationToFile(event, json, rootName, parseType)
                    }
                })
            }
            setCenterPanel(form.rootView)
            setTitle("create a ts file from json")
            removeAllActions()
            show()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled = true;
    }
}
