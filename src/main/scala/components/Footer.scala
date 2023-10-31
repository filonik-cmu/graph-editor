package editor.components

import com.raquo.laminar.api.L.{*, given}

object Footer:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement =
    footerTag(
      cls := "p-1 bg-slate-600 text-slate-400",
      mods
    )
