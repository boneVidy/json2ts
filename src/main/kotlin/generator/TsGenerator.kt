package generator

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile


import parser.ParseType
import parser.toTypescript


class TsGenerator {

    fun generateFromJsonByDocument(json: String, event: AnActionEvent, rootName: String?, parseType: ParseType) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        val document =editor?.document
        val project = event.getData(CommonDataKeys.PROJECT)
        WriteCommandAction.runWriteCommandAction(project) {


            val tsCode = toTypescript(json, rootName!!, parseType)
            document?.apply {
                val selectModel = editor.selectionModel
                val caretModel = editor.caretModel
                val offset =  if (selectModel.hasSelection()) {
                    selectModel.selectionEnd
                } else  {
                    caretModel.offset
                }
                insertString(offset, tsCode)
            }
        }


    }

    fun generateTsFile(json: String, event: AnActionEvent, rootName: String?, parseType: ParseType) {
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        WriteCommandAction.runWriteCommandAction(project) {
            val childFile: VirtualFile? = if (virtualFile?.isDirectory!!) {
                virtualFile.createChildData(this, "${rootName}.ts")
            } else {
                virtualFile.parent.findOrCreateChildData(this, "${rootName}.ts")
            }
            val tsCode = toTypescript(json, rootName!!, parseType)
            childFile?.apply {
                setBinaryContent(tsCode.toByteArray())
                refresh(true, true)
            }
            val fileEditorManager = FileEditorManager.getInstance(project)
            childFile?.let { fileEditorManager.openFile(it, true) }
        }
    }
}