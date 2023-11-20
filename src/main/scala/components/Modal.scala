package editor.components

import scala.scalajs.js

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import js.annotation._

// https://github.com/themesberg/tailwind-typescript-starter

@js.native
@JSImport("flowbite", "Modal")
class Modal(val el: dom.HTMLElement) extends js.Object:
  def show(): Unit = js.native

object ModalButton:
  private var optModal: Option[Modal] = None

  def apply(mods: Modifier[HtmlElement]*): HtmlElement = 
    div(
      button(
        idAttr := "button",
        `type` := "button",
        cls := "text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 mr-2 mb-2 dark:bg-blue-600 dark:hover:bg-blue-700 focus:outline-none dark:focus:ring-blue-800",
        onClick --> {_ => optModal.foreach((m) => m.show())},
        "Show Modal"
      ),
      div(
        idAttr := "modal",
        tabIndex := -1,
        cls := "fixed top-0 left-0 right-0 z-50 hidden w-full p-4 overflow-x-hidden overflow-y-auto md:inset-0 h-[calc(100%-1rem)] max-h-full",
        div(
          cls:="relative w-full max-w-2xl max-h-full",
          mods,
        ),
        onMountUnmountCallback(
          mount = { ctx =>
            optModal = Some(Modal(ctx.thisNode.ref))
          },
          unmount = { thisNode => 
            optModal = None }
        ),
      )
    )
