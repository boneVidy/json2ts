package icons

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

@Suppress("unused")
object Icons {
    @JvmField
    val pluginIcon: Icon = IconLoader.getIcon("/icons/pluginIcon.svg",Icons::class.java).apply {
    }
}
