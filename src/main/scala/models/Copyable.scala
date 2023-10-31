package editor.models

import scala.scalajs.js

import org.scalajs.dom

type Field = (String, js.Any)

private def jsClone(obj: js.Object): js.Object = 
  js.Object.assign(js.Object.create(js.Object.getPrototypeOf(obj)), obj)

private def jsCopy(obj: js.Object, fields: Field*): js.Object = 
  js.Object.assign(jsClone(obj), js.Dynamic.literal(fields*))

object CopyableExtension:
  extension (self: js.Object)
    def copy(fields: Field*): js.Object = 
      jsCopy(self, fields*)

abstract class Copyable[A] extends js.Object:
  def copy(fields: Field*): A = 
    jsCopy(this, fields*).asInstanceOf[A]

/*
import scala.language.dynamics

// Nicer syntax, but unfortunately broken...
abstract class Copyable[A] extends js.Object:
  self =>
  
  object copy extends scala.Dynamic:
    def applyDynamicNamed(name: String)(fields: (String, js.Any)*): A = 
      js.Object.assign(jsClone(self), js.Dynamic.literal(fields*)).asInstanceOf[A]
    
    def applyDynamic(name: String)(fields: (String, js.Any)*): A = 
      js.Object.assign(jsClone(self), js.Dynamic.literal(fields*)).asInstanceOf[A]
*/
