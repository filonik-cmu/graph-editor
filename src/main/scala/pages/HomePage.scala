package editor.pages

import com.raquo.laminar.api.L.{*, given}

import editor.components.*
import editor.Routes.*

object HomePage:
  def routerLink(page: Page) = 
    a(
      onClick.preventDefault --> (_ => router.pushState(page)),
      href := router.absoluteUrlForPage(page),
      page.title,
    )
  
  def apply: HtmlElement = 
    div(
      cls := "h-full flex flex-col",
      Header("Graph Editor"),
      mainTag(
        cls := "flex-grow p-2",
        ul(
          li(routerLink(Page.SignalObserver)),
          li(routerLink(Page.DerivedVar)),
          li(routerLink(Page.Updater)),
          li(routerLink(Page.GraphEditor)),
        ),
      ),
      Footer(),
    )
