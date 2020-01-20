package generator

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

import parser.ParseType
import parser.toTypescript
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

class TsFileGenerator {
    fun generateFromJson(json: String, fileName: String) {
        val tsCode = toTypescript(json)
        val file = File(fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        val out = BufferedWriter(FileWriter(file, true))
        try {
            out.append("\n\n")
            out.append(tsCode)
            out.flush()
        } finally {
            out.close()
        }

    }

    fun generateFromJsonByDocument(json: String, event: AnActionEvent, rootName: String?, parseType: ParseType) {
        val document = event.getData(CommonDataKeys.EDITOR)?.document
        val project = event.getData(CommonDataKeys.PROJECT)
        WriteCommandAction.runWriteCommandAction(project) {
            val tsCode = toTypescript(json, rootName!!, parseType)
            document?.apply {
                insertString(textLength, tsCode)
            }
        }


    }

    fun generateTsFile(json: String, event: AnActionEvent, rootName: String?, parseType: ParseType) {
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val project = event.getData(CommonDataKeys.PROJECT)
        val editor = event.getData(CommonDataKeys.EDITOR)
        WriteCommandAction.runWriteCommandAction(project) {
            val childFile = virtualFile?.createChildData(this, "${rootName}.d.ts")
            val tsCode = toTypescript(json, rootName!!, parseType)
            childFile?.apply {
                setBinaryContent(tsCode.toByteArray())
                refresh(true, true)
            }
        }
    }
}