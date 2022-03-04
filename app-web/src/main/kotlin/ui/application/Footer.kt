package ui.application

import com.tajmoti.libtulip.TulipBuildInfo
import kotlinx.browser.localStorage
import react.Props
import react.dom.a
import react.dom.footer
import react.dom.onClick
import react.dom.span
import react.fc

val Footer = fc<Props> {
    footer("navbar navbar-dark bg-dark text-light mt-auto mt-2") {
        span("navbar-text") {
            +"Build ${TulipBuildInfo.commit}"
        }
        a("/", classes = "navbar-text") {
            +"Reset"
            attrs.onClick = { localStorage.clear() }
        }
    }
}