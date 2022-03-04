package ui.application

import com.tajmoti.libtulip.TulipBuildInfo
import react.Props
import react.dom.footer
import react.dom.span
import react.fc

val Footer = fc<Props> {
    footer("navbar navbar-dark bg-dark text-light mt-auto mt-2") {
        span("navbar-text") {
            +"Build ${TulipBuildInfo.commit}"
        }
    }
}