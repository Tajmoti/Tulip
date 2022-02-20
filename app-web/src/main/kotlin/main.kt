import kotlinx.browser.document
import kotlinx.browser.window
import react.dom.render
import ui.common.Tulip

fun main() {
    window.onload = {
        render(document.getElementById("container")!!) {
            Tulip {}
        }
    }
}

