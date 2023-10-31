package editor.components.icons

import com.raquo.laminar.api.L.{*, given}

def StopIcon(mods: Modifier[SvgElement]*): SvgElement =
  import svg.*
  svg(
    fill := "none",
    viewBox := "0 0 24 24",
    path(
      stroke := "currentColor",
      strokeLineCap := "round",
      strokeLineJoin := "round",
      strokeWidth := "2",
      d := "M5.25 7.5A2.25 2.25 0 017.5 5.25h9a2.25 2.25 0 012.25 2.25v9a2.25 2.25 0 01-2.25 2.25h-9a2.25 2.25 0 01-2.25-2.25v-9z",
    ),
    mods
  )
