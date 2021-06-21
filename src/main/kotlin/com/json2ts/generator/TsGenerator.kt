package com.json2ts.generator

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.json2ts.parser.JsDocConverter
import com.json2ts.parser.ParseType
import icons.com.json2ts.parser.TsConverter

class TsGenerator {
    fun generateFromJsonByDocument(json: String, event: AnActionEvent, rootName: String?, parseType: ParseType) {
        val editor = event.getData(CommonDataKeys.EDITOR)
        val document = editor?.document
        val project = event.getData(CommonDataKeys.PROJECT)
        WriteCommandAction.runWriteCommandAction(project) {
            val code = if (parseType == ParseType.JsDoc) {
                JsDocConverter(json, rootName!!).toCode()
            } else {
                TsConverter(json, rootName!!, parseType).toCode()
            }
            document?.apply {
                val selectModel = editor.selectionModel
                val caretModel = editor.caretModel
                val offset = if (selectModel.hasSelection()) {
                    selectModel.selectionEnd
                } else {
                    caretModel.offset
                }
                insertString(offset, code)
            }
        }
    }

    fun generateTsFile(json: String, event: AnActionEvent, rootName: String?, parseType: ParseType) {
        val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        WriteCommandAction.runWriteCommandAction(project) {
            val fileName = "$rootName.ts"
            val childFile: VirtualFile = if (virtualFile?.isDirectory!!) {
                virtualFile.createChildData(this, fileName)
            } else {
                virtualFile.parent.findOrCreateChildData(this, fileName)
            }
            val converter = TsConverter(json, rootName!!, parseType)
            val tsCode = converter.toCode()
            childFile.apply {
                setBinaryContent(tsCode.toByteArray())
                refresh(true, true)
            }
            val fileEditorManager = FileEditorManager.getInstance(project)
            childFile.let { fileEditorManager.openFile(it, true) }
        }
    }
}
