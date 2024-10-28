package com.json2ts.generator

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.json2ts.exceptions.FileIOException
import com.json2ts.parser.typescript.ParseType
import kotlinx.coroutines.runBlocking
import java.io.IOException

class GeneratorDelegate {
    @Suppress("TooGenericExceptionCaught")
    fun runGeneration(event: AnActionEvent, json: String, rootName: String = "RootObject", parseType: ParseType) {
        ProgressManager.getInstance().run(
            object : Task.Backgroundable(
                event.project, "Ts code is generating....", false
            ) {
                override fun run(indicator: ProgressIndicator) = runBlocking<Unit> {
                    try {
                        val generator = TsGenerator()
                        generator.generateFromJsonByDocument(json, event, rootName, parseType)
                        event.project?.let { Notifier.notify("Successfully generated a typescript type", it) }
                    } catch (e: Exception) {
                        event.project?.let {
                            when (e) {
                                is IOException -> Notifier.notifyException(FileIOException(), it)
                                else -> Notifier.notifyException(e, it)
                            }
                        }
                    } finally {
                        indicator.stop()
                        event.project?.let { ProjectView.getInstance(it).refresh() }
                        event.getData(LangDataKeys.VIRTUAL_FILE)?.refresh(false, true)
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
                    try {
                        val generator = TsGenerator()
                        generator.generateTsFile(json, event, rootName, parseType)
                        event.project?.let { Notifier.notify("Ts interface has been generated", it) }
                    } catch (e: Throwable) {
                        event.project?.let {
                            when (e) {
                                is IOException -> Notifier.notifyException(FileIOException(), it)
                                else -> Notifier.notifyException(e, it)
                            }
                        }
                    } finally {
                        indicator.stop()
                        event.project?.let { ProjectView.getInstance(it).refresh() }
                        event.getData(LangDataKeys.VIRTUAL_FILE)?.refresh(false, true)
                    }
                }
            }
        )
    }
}
