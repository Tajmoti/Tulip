import kotlinx.browser.document
import kotlinx.browser.window
import react.dom.render
import ui.application.Application

fun main() {
    window.onload = {
        render(document.getElementById("container")!!) {
            Application {}
        }
    }
}

