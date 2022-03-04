package ui.shared

import kotlinx.html.Tag
import react.Props
import react.dom.RDOMBuilder

external interface RenderProp<T : Tag> : Props {
    var contents: (RDOMBuilder<T>) -> Unit
}
