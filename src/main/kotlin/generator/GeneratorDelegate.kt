package generator

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.json2ts.parser.ParseType
import exceptions.FileIOException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

class GeneratorDelegate(private val messageDelegate: MessageDelegate = MessageDelegate()) {
    @Suppress("TooGenericExceptionCaught")
    fun runGeneration(event: AnActionEvent, json: String, rootName: String = "RootObject", parseType: ParseType) {
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(
                event.project, "Ts code is generating....", false
            ) {
                override fun run(indicator: ProgressIndicator) = runBlocking<Unit> {
                    launch {
                        try {
                            val generator = TsGenerator()
                            generator.generateFromJsonByDocument(json, event, rootName, parseType)
                            messageDelegate.showMessage("Ts interface has been generated")
                        } catch (e: Exception) {
                            when (e) {
                                is IOException -> messageDelegate.onException(FileIOException())
                                else -> messageDelegate.onException(e)
                            }
                        } finally {
                            indicator.stop()
                            ProjectView.getInstance(event.project).refresh()
                            event.getData(LangDataKeys.VIRTUAL_FILE)?.refresh(false, true)
                        }
                    }
                }
            }
        )
    }
    @Suppress("TooGenericExceptionCaught")
    fun runGenerationToFile(event: AnActionEvent, json: String, rootName: String = "RootObject", parseType: ParseType) {
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(
                event.project, "Ts code is generating....", false
            ) {
                override fun run(indicator: ProgressIndicator) = runBlocking<Unit> {
                    launch {
                        try {
                            val generator = TsGenerator()
                            generator.generateTsFile(json, event, rootName, parseType)
                            messageDelegate.showMessage("Ts interface has been generated")
                        } catch (e: Throwable) {
                            when (e) {
                                is IOException -> messageDelegate.onException(FileIOException())
                                else -> messageDelegate.onException(e)
                            }
                        } finally {
                            indicator.stop()
                            ProjectView.getInstance(event.project).refresh()
                            event.getData(LangDataKeys.VIRTUAL_FILE)?.refresh(false, true)
                        }
                    }
                }
            }
        )
    }
}
