package com.json2ts.views

import com.intellij.json.JsonLanguage
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField

class JsonEditor(project: Project?, s: String?) :
    LanguageTextField(JsonLanguage.INSTANCE, project, s.toString()) {
    companion object {
        @Suppress()
        fun createEditorByClipboard(): JsonEditor {
            return JsonEditor(null, "")
        }
    }
    override fun createEditor(): EditorEx {
        val editor = super.createEditor()
        editor.setVerticalScrollbarVisible(true)
        editor.setHorizontalScrollbarVisible(true)
        val settings = editor.settings
        editor.setPlaceholder("Please input a json text")
        settings.apply {
            isAllowSingleLogicalLineFolding = true
            isLineNumbersShown = true
            isAutoCodeFoldingEnabled = true
            isFoldingOutlineShown = true
            isAllowSingleLogicalLineFolding = true
            isRightMarginShown = true
        }
        return editor
    }
}
