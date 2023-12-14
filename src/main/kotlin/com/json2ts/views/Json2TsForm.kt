package com.json2ts.views

import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.json2ts.generator.MessageDelegate
import com.json2ts.parser.ParseType
import java.awt.Dimension
import java.awt.Insets
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JTextField

@Suppress("MagicNumber")
class Json2TsForm {
    lateinit var rootView: JPanel
    lateinit var editor: JsonEditor
    lateinit var generateButton: JButton
    lateinit var rootObjectName: JTextField
    lateinit var fileNameLabel: JLabel
    lateinit var typeRadio: JRadioButton
    lateinit var interfaceRadio: JRadioButton
    lateinit var jsDocRadio: JRadioButton
    lateinit var classRadio: JRadioButton
    private lateinit var buttonGroup: ButtonGroup
    lateinit var formatJsonBtn: JButton
    private var listener: OnGenerateClicked? = null
    fun setFormatHandle() {
        formatJsonBtn.addActionListener {
            val text = editor.text
            text.let {
                val jsonString = try {
                    val rootJsonElement = JsonParser().parse(text)
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    gson.toJson(rootJsonElement)
                } catch (e: JsonParseException) {
                    MessageDelegate().catchException(e)
                }
                editor.text = jsonString as String
            }
        }
    }
    fun setOnGenerateListener(listener: OnGenerateClicked) {
        this.listener = listener
        interfaceRadio.isSelected = true
        val radioList = listOf(interfaceRadio, jsDocRadio, typeRadio, classRadio)
        radioList.forEach {
            it.addActionListener {
                radioList
                    .filter { btn -> btn.text != it!!.actionCommand }
                    .forEach { btn -> btn.isSelected = false }
            }
        }
        generateButton.addActionListener {
            val selectedRadio = radioList.find { it.isSelected }
            selectedRadio?.let {
                val parseType = when (selectedRadio.text) {
                    "JsDoc" -> ParseType.JsDoc
                    "Type" -> ParseType.TypeStruct
                    "Class" -> ParseType.TSClass
                    "Interface" -> ParseType.InterfaceStruct
                    else -> ParseType.InterfaceStruct
                }
                val rootName = if (rootObjectName.text != "") {
                    rootObjectName.text
                } else "RootObject"
                this.listener?.onClicked(
                    rootName,
                    editor.text,
                    parseType
                )
            }

        }
    }

    private fun createUIComponents() {
        editor = JsonEditor.createEditorByClipboard()
        editor.apply {
            setOneLineMode(false)
        }
    }

    init {
        // GUI initializer generated by IntelliJ IDEA GUI Designer
        // >>> IMPORTANT!! <<<
        // DO NOT EDIT OR ADD ANY CODE HERE!
        `$$$setupUI$$$`()
        setFormatHandle()
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     * @noinspection ALL
     */
    @Suppress("LongMethod")
    private fun `$$$setupUI$$$`() {
        createUIComponents()
        rootView = JPanel()
        rootView.layout = GridLayoutManager(2, 4, Insets(0, 0, 0, 0), -1, -1)
        rootView.preferredSize = Dimension(500, 500)
        val scrollPane1 = JBScrollPane()
        rootView.add(
            scrollPane1,
            GridConstraints(
                0,
                0,
                1,
                4,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_BOTH,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_WANT_GROW,
                null,
                null,
                null,
                0,
                false
            )
        )
        scrollPane1.add(editor)
        generateButton = JButton()
        formatJsonBtn = createFormatButton()
        generateButton.text = "Generate"
        rootView.add(
            generateButton,
            GridConstraints(
                1,
                3,
                1,
                1,
                GridConstraints.ANCHOR_CENTER,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )

        rootObjectName = JTextField()
        rootView.add(
            rootObjectName,
            GridConstraints(
                1,
                2,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                Dimension(150, -1),
                null,
                0,
                false
            )
        )
        fileNameLabel = JLabel()
        fileNameLabel.text = "Root name:"
        rootView.add(
            fileNameLabel,
            GridConstraints(
                1,
                1,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )
        buttonGroup = ButtonGroup()
        buttonGroup.add(typeRadio)
        buttonGroup.add(interfaceRadio)
        buttonGroup.add(jsDocRadio)
    }
    private fun createFormatButton(): JButton {
        return JButton()
    }
    /**
     * @noinspection ALL
     */
    fun `$$$getRootComponent$$$`(): JComponent {
        return rootView
    }

    interface OnGenerateClicked {
        fun onClicked(rootName: String, json: String, parseType: ParseType)
    }
}
