package ui.application

import com.tajmoti.commonutils.jsObject
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import org.w3c.dom.HTMLInputElement
import react.Props
import react.dom.form
import react.dom.input
import react.dom.nav
import react.dom.span
import react.fc
import react.router.dom.Link
import react.router.useLocation
import react.router.useNavigate

val NavBar = fc<Props> {
    val nav = useNavigate()
    val location = useLocation()
    nav("navbar navbar-dark bg-dark") {
        Link {
            attrs.to = "/"
            span("navbar-brand text-light bg-dark") { +"Tulip" }
        }
        form(classes = "form-inline") {
            input(type = InputType.search, classes = "form-control mr-sm-2") {
                attrs.placeholder = "Search"
                attrs.onChangeFunction = { event ->
                    val query = (event.target as HTMLInputElement).value
                    val dstUrl = "/search?query=${query}"
                    if (location.pathname == "/search") {
                        nav.invoke(dstUrl, jsObject { replace = true })
                    } else {
                        nav.invoke(dstUrl)
                    }
                }
            }
        }
    }
}