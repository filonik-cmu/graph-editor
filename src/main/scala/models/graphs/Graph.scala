package editor.models.graphs

import scala.scalajs.js
import scala.scalajs.js.annotation.JSName

import com.raquo.laminar.api.L.{*, given}

import editor.models.*

type Id = String
type Path = String

class Node(
  val _id: Id,
  var label: js.UndefOr[String] = js.undefined,
) extends Copyable[Node]

class Link(
  val _id: Id,
  var label: js.UndefOr[String] = js.undefined,
  var source: js.UndefOr[Id] = js.undefined,
  var target: js.UndefOr[Id] = js.undefined,
) extends Copyable[Link]

class Graph(
  var label: js.UndefOr[String] = js.undefined,
  var nodes: js.Array[Node] = js.Array(),
  var links: js.Array[Link] = js.Array(),
) extends Copyable[Graph]
