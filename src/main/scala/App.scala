package editor

import concurrent.ExecutionContext.Implicits.global

import com.raquo.laminar.api.L.*
import com.raquo.waypoint.*
import io.laminext.fetch.*

import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSImport

import editor.pages.*

object App:
  val splitter = SplitRender[Routes.Page, HtmlElement](Routes.router.currentPageSignal)
    .collectStatic(Routes.Page.Home) { HomePage.apply }
    .collectStatic(Routes.Page.SignalObserver) { SignalObserverPage.apply }
    .collectStatic(Routes.Page.DerivedVar) { DerivedVarPage.apply }
    .collectStatic(Routes.Page.Updater) { UpdaterPage.apply }
  
  def main(args: Array[String]): Unit = 
    lazy val container = dom.document.getElementById("app")

    lazy val myApp = div(
      cls := "h-full flex flex-col",
      child <-- splitter.signal
    )

    renderOnDomContentLoaded(container, myApp)
