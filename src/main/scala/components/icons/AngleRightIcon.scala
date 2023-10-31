package editor.components.icons

import com.raquo.laminar.api.L.{*, given}

def AngleRightIcon(mods: Modifier[SvgElement]*): SvgElement =
  import svg.*
  svg(
    fill := "none",
    viewBox := "0 0 8 14",
    path(
      stroke := "currentColor",
      strokeLineCap := "round",
      strokeLineJoin := "round",
      strokeWidth := "2",
      d := "m1 13 5.7-5.326a.909.909 0 0 0 0-1.348L1 1",
    ),
    mods
  )
