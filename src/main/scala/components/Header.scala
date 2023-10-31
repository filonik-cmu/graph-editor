package editor.components

import com.raquo.laminar.api.L.{*, given}

object Header:
  def apply(mods: Modifier[HtmlElement]*): HtmlElement = 
    headerTag(
      cls := "p-1 bg-slate-600 text-slate-200",
      mods
    )
