package com.json2ts.views

import com.intellij.ide.CopyPasteManagerEx
import com.intellij.json.JsonLanguage
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField
import java.awt.datatransfer.DataFlavor

class JsonEditor(project: Project?, s: String?) :
    LanguageTextField(JsonLanguage.INSTANCE, project, s.toString()) {
    companion object {
        @Suppress()
        fun createEditorByClipboard(): JsonEditor {
            val contents = CopyPasteManagerEx.getInstanceEx()
                .contents?.getTransferData(DataFlavor.stringFlavor) as String?
//            val rootJsonElement = JsonParser.parseString(contents ?: "")
//            val gson = GsonBuilder().setPrettyPrinting().create()
//            val jsonString = try {
//                gson.toJson(rootJsonElement)
//            }
            return JsonEditor(null, contents ?: "")
        }
    }
    override fun createEditor(): EditorEx {
        val editor = super.createEditor()
        editor.setVerticalScrollbarVisible(true)
        editor.setHorizontalScrollbarVisible(true)
        val settings = editor.settings
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
