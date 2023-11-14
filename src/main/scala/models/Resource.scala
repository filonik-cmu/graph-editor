package editor.models

type Predicate[A] = A => Boolean

type Getter[S,A] = S => A
type Setter[S,A] = (S, A) => S

type Create[A] = () => A
type Update[A] = A => A
type Delete[A] = A => Unit

type Creator[A] = Create[A] => Unit
type Updater[A] = Update[A] => Unit
type Deleter[A] = Delete[A] => Unit

trait Collection[T[_]]:
  extension [A](self: T[A])
    def create(create: Create[A]): T[A]
    def update(predicate: Predicate[A], update: Update[A]): T[A]
    def delete(predicate: Predicate[A], delete: Delete[A]): T[A]

given Collection[Seq] with
 extension [A](self: Seq[A]) 
    def create(create: Create[A]): Seq[A] =
      self :+ create()
    
    def update(predicate: Predicate[A], update: Update[A]): Seq[A] = 
      self.map(value => 
        if predicate(value) then
          update(value)
        else 
          value
      )
  
    def delete(predicate: Predicate[A], delete: Delete[A]): Seq[A] = 
      self.filterNot(value => 
        if predicate(value) then 
          delete(value) 
          true
        else
          false
      )

object Resource:
  def creator[V](updater: Updater[Seq[V]])(create: Create[V]): Unit = updater(_.create(create))
  def updater[V](updater: Updater[Seq[V]], predicate: Predicate[V])(update: Update[V]): Unit = updater(_.update(predicate, update))
  def deleter[V](updater: Updater[Seq[V]], predicate: Predicate[V])(delete: Delete[V]): Unit = updater(_.delete(predicate, delete))


import com.raquo.laminar.api.L.{*, given}

object Update:
  def zoom[S,A](signal: Signal[S], update: Observer[Update[S]])(getter: Getter[S,A])(setter: Setter[S,A]): (Signal[A], Observer[Update[A]]) =
    (signal.map(getter), update.contramap((focusUpdate) => (value) => setter(value, focusUpdate(getter(value)))))

object Updater:
  def zoom[S,A](signal: Signal[S], updater: Updater[S])(getter: Getter[S,A])(setter: Setter[S,A]): (Signal[A], Updater[A]) = 
    (signal.map(getter), (focusUpdater) => { 
      updater((value) => setter(value, focusUpdater(getter(value))))
    }) 

def defaultUpdate[A]: Update[A] = (a) => a
def defaultDelete[A]: Delete[A] = (a) => ()

sealed trait Command[A]
case class CreateCommand[A](create: Create[A]) extends Command[A]
case class UpdateCommand[A](predicate: Predicate[A], update: Update[A] = defaultUpdate) extends Command[A]
case class DeleteCommand[A](predicate: Predicate[A], delete: Delete[A] = defaultDelete) extends Command[A]

def collectionObserver[A,T[A]](updater: Updater[T[A]])(using Collection[T]): Observer[Command[A]] = Observer[Command[A]] {
  case CreateCommand(create) => {
    updater(_.create(create))
  }
  case UpdateCommand(predicate, update) => {
    updater(_.update(predicate, update))
  }
  case DeleteCommand(predicate, delete) => {
    updater(_.delete(predicate, delete))
  }    
}

import scala.scalajs.js

given Collection[js.Array] with
 extension [A](self: js.Array[A]) 
    def create(create: Create[A]): js.Array[A] =
      self :+ create()
    
    def update(predicate: Predicate[A], update: Update[A]): js.Array[A] = 
      self.map(value => 
        if predicate(value) then
          update(value)
        else 
          value
      )
  
    def delete(predicate: Predicate[A], delete: Delete[A]): js.Array[A] = 
      self.filterNot(value => 
        if predicate(value) then 
          delete(value) 
          true
        else
          false
      )