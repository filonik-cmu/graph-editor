package editor.pages

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.scalajs.js.JSON

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import editor.components.{icons}
import editor.models.{*, given}
//import editor.models.CopyableExtension.*
import editor.utilities

/*
class NodeBuilder:
  private var label: js.UndefOr[String] = js.undefined

  def setLabel(label: js.UndefOr[String]): NodeBuilder =
    this.label = label
    this
  
  def create(): graphs.Node = 
    val id = NodeBuilder.nodeIdGenerator().toString()
    graphs.Node(id, label)

object NodeBuilder:
  private val nodeIdGenerator = new utilities.IntGenerator()
*/

import js.annotation._

@js.native
@JSImport("/catlab-widgets.js", "forceDirectedGraph")
def forceDirectedGraph(config: js.Any): js.Any = js.native

import scala.reflect._

extension [T](self: Signal[T])
  def splitByType[T1 <: T : ClassTag, T2 <: T : ClassTag, Output]/*(
    key: PartialFunction[T, Key]
    //k1: (T1) => Key
    //k2: (T2) => Key
  )*/(
    //signal: PartialFunction[(T, Signal[T]), Output],
    s1: (T1, Signal[T1]) => Output,
    s2: (T2, Signal[T2]) => Output
  ): Signal[Output] =
    self.splitOne(_ match
      case a: T1 => 1 // (1, key(a))
      case b: T2 => 2 // (2, key(b))
    )((key, value, valueSignal) => key match
      case 1 => s1(value.asInstanceOf[T1], valueSignal.map(_.asInstanceOf[T1]))
      case 2 => s2(value.asInstanceOf[T2], valueSignal.map(_.asInstanceOf[T2]))
    )

object NodeFactory:
  private val nodeIdGenerator = new utilities.IntGenerator()
  def create(template: (id: graphs.Id) => graphs.Node) =
    () => template(nodeIdGenerator().toString())
  def delete = (node: graphs.Node) => ()

object LinkFactory:
  private val linkIdGenerator = new utilities.IntGenerator()
  def create(template: (id: graphs.Id) => graphs.Link) = 
    () => template(linkIdGenerator().toString())
  def delete = (link: graphs.Link) => ()

object UpdaterPage:
  private val graph = Var(graphs.Graph())

  private val graphSignal: Signal[graphs.Graph] = graph.signal
  private val graphUpdater: Updater[graphs.Graph] = graph.update(_)
  
  def nodesSignal = graphSignal.map(_.nodes.toSeq)
  def linksSignal = graphSignal.map(_.links.toSeq)

  /*
  // Mutable
  private def nodesUpdater = (update: Resource.Update[Seq[graphs.Node]]) => graphUpdater({ graph => 
    graph.nodes = update(graph.nodes.toSeq).toJSArray
    graph
  })
  */

  // Immutable
  private def nodesUpdater = (update: Update[Seq[graphs.Node]]) => graphUpdater(graph => graph.copy(
    "nodes" -> update(graph.nodes.toSeq).toJSArray
  ))

  private def linksUpdater = (update: Update[Seq[graphs.Link]]) => graphUpdater(graph => graph.copy(
    "links" -> update(graph.links.toSeq).toJSArray
  ))

  /*
  sealed trait Selection
  case object GraphSelection extends Selection
  case class NodeSelection(val id: graphs.Id) extends Selection
  case class LinkSelection(val id: graphs.Id) extends Selection
  */

  type Selection = Option[(graphs.Path, graphs.Id)]

  private val selectionVar: Var[Selection] = Var(None)
  private val selectionSignal: Signal[Selection] = selectionVar.signal
  private val selectionUpdater: Updater[Selection] = selectionVar.update(_)

  private def isNodeSelected(selection: Selection, node: graphs.Node): Boolean =
    selection.map((path, id) => path == "nodes" && id == node._id).getOrElse(false)
  
  private def isLinkSelected(selection: Selection, link: graphs.Link): Boolean =
    selection.map((path, id) => path == "links" && id == link._id).getOrElse(false)

  private def selectedValueSignal: Signal[Option[graphs.Node | graphs.Link]] = selectionSignal.combineWith(graphSignal).map(
    (selection: Selection, graph: graphs.Graph) => {
      selection.flatMap((path, id) => path match
        case "nodes" => graph.nodes.find(id == _._id)
        case "links" => graph.links.find(id == _._id)
      )
    }
  )

  def nodeLabel(modelSignal: Signal[graphs.Node]): Signal[HtmlElement] = 
    modelSignal.map(node => node.label.map(
      span(_)
    ).getOrElse(
      span(cls := "italic", s"Node (${node._id})")
    ))

  def linkLabel(modelSignal: Signal[graphs.Link]): Signal[HtmlElement] = 
    modelSignal.map(link => link.label.map(
      span(_)
    ).getOrElse(
      span(cls := "italic", s"Link (${link._id})")
    ))
  
  //private def selectedValueUpdater = 

  object LinkItem:
    type Model = graphs.Link
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model])(mods: Modifier[HtmlElement]*): HtmlElement = 
      def isSelectedSignal = selectionSignal.combineWith(modelSignal).map(isLinkSelected)
      li(
        cls := "flex flex-col py-px",
        div(
          cls := "flex flex-row items-center gap-1 px-2 group hover:bg-slate-400", 
          cls.toggle("bg-slate-300") <-- isSelectedSignal,
          icons.StopIcon(svg.cls := "w-4 h-4"),
          h4(
            cls := "flex-grow",
            child <-- linkLabel(modelSignal)
          ),
          onClick.stopPropagation.compose(_.sample(modelSignal)) --> (link => 
            selectionUpdater((_) => Some(("links", link._id)))
          ),
          mods
        )
      )

  
  object LinkList: 
    type Model = Seq[graphs.Link]
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement =
      ul(
        children <-- modelSignal.split(_._id){(id, _, linkSignal) =>
          def linkUpdater = Resource.updater(modelUpdater, _._id == id)
          def linkDeleter = Resource.deleter(modelUpdater, _._id == id)
          LinkItem(linkSignal, linkUpdater)(
            button(
              cls := "w-8 h-8 btn btn-red btn-sm invisible group-hover:visible",
              onClick --> (_ => modelUpdater(
                _.delete(_._id == id, LinkFactory.delete))
              ),
              "-"
            )
          )
        }
      )
  
  object NodeItem:
    type Model = graphs.Node
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model])(mods: Modifier[HtmlElement]*): HtmlElement = 
      def linksSignal = graphSignal.combineWith(modelSignal).map((graph, node) => graph.links.filter(_.source == node._id).toSeq)
      def linkCreator = Resource.creator(linksUpdater)
      def isSelectedSignal = selectionSignal.combineWith(modelSignal).map(isNodeSelected)
      val isCollapsedVar = Var(false)
      li(
        cls := "flex flex-col gap-0.5 py-1",
        div(
          cls := "flex flex-row items-center gap-1 px-2 group hover:bg-slate-400",
          cls.toggle("bg-slate-300") <-- isSelectedSignal,
          icons.AngleDownIcon(
            svg.cls := "w-4 h-4 transition-transform",
            svg.cls.toggle("-rotate-90") <-- isCollapsedVar.signal,
            onClick.stopPropagation.map(_ => !isCollapsedVar.now()) --> isCollapsedVar.writer,
          ),
          h3(
            cls := "flex-grow",
            child <-- nodeLabel(modelSignal),
          ),
          button(
            cls := "w-8 h-8 btn btn-green btn-sm invisible group-hover:visible",
            onClick.stopPropagation.compose(_.sample(modelSignal)) --> (node => 
              linkCreator(LinkFactory.create(graphs.Link(_, source=node._id, target=node._id)))
            ),
            "+"
          ),
          onClick.stopPropagation.compose(_.sample(modelSignal)) --> (node => 
            selectionUpdater((_) => Some(("nodes", node._id)))
          ),
          mods
        ),
        LinkList(linksSignal, linksUpdater).amend(
          cls.toggle("hidden") <-- isCollapsedVar.signal,
        )
      )
      
      /*
      li(
        div(
          cls := "flex flex-col",
          div(
            cls := "flex flex-row items-center",
            h2(
              cls := "flex-grow",
              "Graph"
            ),
            button(
              cls := "w-8 h-8 btn btn-green btn-sm",
              //onClick --> (_ => modelUpdater(ResourceCollection.create(createNode))),
              "+"
            ),
          ),
        cls := "flex flex-row items-center",
        child <-- modelSignal.map(_.label.map(
          span(_)
        ).getOrElse(
          span(cls := "italic", "Unnamed Node")
        )),
      )
      )
      */
  
  object NodeList:
    type Model = Seq[graphs.Node]
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement =
      ul(
        children <-- modelSignal.split(_._id){(id, _, nodeSignal) =>
          def nodeUpdater = Resource.updater(modelUpdater, _._id == id)
          def nodeDeleter = Resource.deleter(modelUpdater, _._id == id)
          NodeItem(nodeSignal, nodeUpdater)(
            button(
              cls := "w-8 h-8 btn btn-red btn-sm invisible group-hover:visible",
              onClick --> { _ => 
                //nodeDeleter(NodeFactory.delete)
                graphUpdater(graph => {
                  graph.copy(
                    "nodes" -> graph.nodes.filterNot(node => node._id == id),
                    "links" -> graph.links.filterNot(link => link.source == id || link.target == id),
                  )
                })
              },
              "-"
            ),
          )
        }
      )

  object GraphItem:
    type Model = graphs.Graph
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement = 
      def nodesSignal = graphSignal.map(_.nodes.toSeq)
      def nodeCreator = Resource.creator(nodesUpdater)
      div(
        cls := "flex flex-col gap-0.5 py-1",
        div(
          cls := "flex flex-row items-center px-2 hover:bg-slate-400",
          h2(
            cls := "flex-grow",
            child <-- modelSignal.map(_.label.map(
              span(_)
            ).getOrElse(
              span(cls := "italic", "Unnamed Graph")
            )),
          ),
          button(
            cls := "w-8 h-8 btn btn-green btn-sm",
            onClick --> (_ => nodeCreator(NodeFactory.create(graphs.Node(_)))),
            //onClick --> (_ => nodeCreator(NodeBuilder().create)),
            "+"
          ),
        ),
        NodeList(nodesSignal, nodesUpdater)
      )
  
  object Sidebar:
    type Model = graphs.Graph
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement = 
      GraphItem(modelSignal, modelUpdater)

  object Content:
    type Model = graphs.Graph
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement = 
      val graph = forceDirectedGraph(js.Dynamic.literal(
        "width" -> 1920,
        "height" -> 1080,
        "layout" -> js.Dynamic.literal(
          "strength" -> 1000,
        )
      )).asInstanceOf[js.Dynamic]
      //dom.console.log(graph)
      div(
        cls := "flex flex-col",
        div(
          cls := "flex-grow",
          onMountUnmountCallback(
            mount = { nodeCtx =>
              graph.mount(nodeCtx.thisNode.ref)
            },
            unmount = { thisNode =>
              graph.unmount()
            }
          ),
          modelSignal --> { data => 
            graph.update(data.copy(
              "nodes" -> data.nodes.map(_.copy()),
              "links" -> data.links.map(_.copy()),
            ))
          },
        ),
        pre(
          cls := "hidden",
          child.text <-- modelSignal.map(JSON.stringify(_, space=2))
        )
      )
      
  
  object NodeDetail:
    type Model = graphs.Node
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement =
      def labelToString(label: js.UndefOr[String]): String = label.getOrElse("")
      def stringToLabel(value: String): js.UndefOr[String] = if value != "" then value else js.undefined
      val labelUpdater = (label: js.UndefOr[String]) => modelUpdater(_.copy(
        "label" -> label.asInstanceOf[js.Any]
      ))
      div(
        cls := "flex flex-col",
        h2("Node"),
        label("Label"),
        input(
          `type` := "text",
          controlled(
            value <-- modelSignal.map(node => labelToString(node.label)),
            onInput.mapToValue.map(value => stringToLabel(value)) --> labelUpdater
          )
        )
      )

  object LinkDetail:
    type Model = graphs.Link
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement =
      def labelToString(label: js.UndefOr[String]): String = label.getOrElse("")
      def stringToLabel(value: String): js.UndefOr[String] = if value != "" then value else js.undefined
      val labelUpdater = (label: js.UndefOr[String]) => modelUpdater(_.copy(
        "label" -> label.asInstanceOf[js.Any]
      ))
      div(
        cls := "flex flex-col gap-1",
        h2("Link"),
        label("Label"),
        input(
          `type` := "text",
          controlled(
            value <-- modelSignal.map(node => labelToString(node.label)),
            onInput.mapToValue.map(value => stringToLabel(value)) --> { label => modelUpdater(_.copy(
              "label" -> label.asInstanceOf[js.Any]
            ))
            }
          )
        ),
        label("Source"),
        select(
          controlled(
            value <-- modelSignal.map(node => labelToString(node.source)),
            onChange.mapToValue.map(value => stringToLabel(value)) --> { source => modelUpdater(_.copy(
              "source" -> source.asInstanceOf[js.Any]
            ))
            }
          ),
          children <-- nodesSignal.split(_._id)((id, _, nodeSignal) => 
            option(
              value := id,
              child <-- nodeLabel(nodeSignal),
              selected <-- nodeSignal.combineWith(modelSignal).map(_._id == _.source)
            )
          )
        ),
        label("Target"),
        select(
          controlled(
            value <-- modelSignal.map(node => labelToString(node.target)),
            onChange.mapToValue.map(value => stringToLabel(value)) --> { target => modelUpdater(_.copy(
              "target" -> target.asInstanceOf[js.Any]
            ))
            }
          ),
          children <-- nodesSignal.split(_._id)((id, _, nodeSignal) => 
            option(
              value := id,
              child <-- nodeLabel(nodeSignal),
              selected <-- nodeSignal.combineWith(modelSignal).map(_._id == _.target)
            )
          )
        )
      )

  object Detail:
    type Model = graphs.Graph
    def apply(modelSignal: Signal[Model], modelUpdater: Updater[Model]): HtmlElement = 
      div(
        child <-- selectedValueSignal.splitOption((_, valueSignal) =>
          span(
            child <-- valueSignal.splitByType/*({
              case node: graphs.Node => node._id
              case link: graphs.Link => link._id
            })*/(
              (_: graphs.Node, nodeSignal: Signal[graphs.Node]) => 
                NodeDetail(nodeSignal, (nodeUpdate) =>
                  val predicate = isNodeSelected(selectionVar.now() ,_)
                  modelUpdater(graph => graph.copy(
                    "nodes" -> graph.nodes.update(predicate, nodeUpdate)
                  ))
                ),
              (_: graphs.Link, linkSignal: Signal[graphs.Link]) => 
                LinkDetail(linkSignal, (linkUpdate) =>
                  val predicate = isLinkSelected(selectionVar.now() ,_)
                  modelUpdater(graph => graph.copy(
                    "links" -> graph.links.update(predicate, linkUpdate)
                  ))
                ),
            )
          )
        ,
        span(cls:="italic", "No Selection.")
      ))

  def apply: HtmlElement = 
    div(
      cls := "h-full flex flex-row",
      Sidebar(graphSignal, graphUpdater).amend(
        cls := "w-64 bg-slate-200",
      ),
      Content(graphSignal, graphUpdater).amend(
        cls := "flex-grow overflow-hidden",
      ),
      Detail(graphSignal, graphUpdater).amend(
        cls := "w-64 bg-slate-300 p-2",
      ),
    )
