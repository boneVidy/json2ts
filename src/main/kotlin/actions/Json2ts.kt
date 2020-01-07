package actions

import Json2TsForm
import Json2TsForm.OnGenerateClicked
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogBuilder
import generator.GeneratorDelegate


class Json2ts : AnAction() {
    private val generatorDelegate = GeneratorDelegate()

    override fun actionPerformed(event: AnActionEvent) {
        DialogBuilder().apply {
            val form = Json2TsForm().apply {
                setOnGenerateListener(object : OnGenerateClicked {
                    override fun onClicked(fileName: String, json: String) {
                        window.dispose()
                        generatorDelegate.runGeneration(event, fileName, json )
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
        e.presentation.isEnabled = true;
    }
}
