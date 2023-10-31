package editor.pages

import com.raquo.laminar.api.L.{*, given}

import editor.models.{*, given}

object SignalObserverPage:
  private val nodesVar: Var[Seq[graphs.Node]] = Var(Nil)

  private val nodesSignal = nodesVar.signal
  private val nodesWriter = collectionObserver(nodesVar.update(_))
   
  def apply: HtmlElement = 
    div(
      h1("SignalObserverPage"),
      button(
        onClick.map(_ => 
          CreateCommand[graphs.Node](() => graphs.Node("123"))
        ) --> nodesWriter
      ),
      button(
        onClick.map(_ => 
          DeleteCommand[graphs.Node](_._id == "123")
        ) --> nodesWriter
      )
    )
