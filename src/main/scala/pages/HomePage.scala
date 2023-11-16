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
      ModalButton(
        div(
          cls := "relative bg-white rounded-lg shadow dark:bg-gray-700",
          div(
            cls := "flex items-start justify-between p-5 border-b rounded-t dark:border-gray-600",
            h3(
              cls := "text-xl font-semibold text-gray-900 lg:text-2xl dark:text-white",
              "Terms of Service"
            )
          ),
          div(
            cls := "p-6 space-y-6",
            p(
              cls := "text-base leading-relaxed text-gray-500 dark:text-gray-400",
              "With less than a month to go before the European Union enacts new consumer privacy laws for its citizens, companies around the world are updating their terms of service agreements to comply."
            ),
            p(
              cls := "text-base leading-relaxed text-gray-500 dark:text-gray-400",
              "The European Union’s General Data Protection Regulation (G.D.P.R.) goes into effect on May 25 and is meant to ensure a common set of data rights in the European Union. It requires organizations to notify users as soon as possible of high-risk data breaches that could personally affect them."
            ),
          ),
          div(
            cls := "flex items-center p-6 space-x-2 border-t border-gray-200 rounded-b dark:border-gray-600",
            button(
              cls := "text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center dark:bg-blue-600 dark:hover:bg-blue-700 dark:focus:ring-blue-800",
              `type` := "button",
              "I accept"
            ),
            button(
              cls := "text-gray-500 bg-white hover:bg-gray-100 focus:ring-4 focus:outline-none focus:ring-blue-300 rounded-lg border border-gray-200 text-sm font-medium px-5 py-2.5 hover:text-gray-900 focus:z-10 dark:bg-gray-700 dark:text-gray-300 dark:border-gray-500 dark:hover:text-white dark:hover:bg-gray-600",
              `type` := "button",
              "Decline"
            )
          )
        )
      ),
      Footer(),
    )
