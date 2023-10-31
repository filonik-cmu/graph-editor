package editor.components.icons

import com.raquo.laminar.api.L.{*, given}

def AngleDownIcon(mods: Modifier[SvgElement]*): SvgElement =
  import svg.*
  svg(
    fill := "none",
    viewBox := "0 0 14 8",
    path(
      stroke := "currentColor",
      strokeLineCap := "round",
      strokeLineJoin := "round",
      strokeWidth := "2",
      d := "m1 1 5.326 5.7a.909.909 0 0 0 1.348 0L13 1"
    ),
    mods
  )
