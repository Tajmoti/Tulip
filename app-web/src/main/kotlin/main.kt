import kotlinx.browser.document
import kotlinx.browser.window
import react.dom.render
import ui.common.TulipComponent

fun main() {
    window.onload = {
        render(document.getElementById("container")) {
            child(TulipComponent::class) {}
        }
    }
}

