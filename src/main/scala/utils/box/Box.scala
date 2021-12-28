package ir.ac.usc
package utils.box

import exception.ScommenderException

import scala.concurrent.Future
import scala.util.Try

sealed trait Box[+T] {

  def map[V](f: T => V): Box[V]
  def flatMap[V](f: T => Box[V]): Box[V]
  def foreach[U](f: T => U): Unit
  def fold[V](fa: ScommenderException => V, fb: T => V): V = this match {
    case Successful(value) => fb(value)
    case Failed(cause) => fa(cause)
  }

}

object Box {
  def fromTry[T](t: Try[T]): Box[T] = t fold(
    fa = throwable => Failed[T](throwable),
    fb = value => Successful(value)
  )

  def fromEither[T](either: Either[Throwable, T]): Box[T] = either match {
    case Left(ex) => Failed[T](ex)
    case Right(value) => Successful(value)
  }

  def apply[T](code: => T): Box[T] = fromTry(Try(code))

}

case class Successful[+T](value: T) extends Box[T] {
  override def map[V](f: T => V): Box[V] = Successful(f(value))

  override def flatMap[V](f: T => Box[V]): Box[V] = f(value)

  override def foreach[U](f: T => U): Unit = f(value)
}

case class Failed[+T](cause: ScommenderException) extends Box[T] {
  override def map[V](f: T => V): Box[V] = Failed[V](cause)

  override def flatMap[V](f: T => Box[V]): Box[V] = Failed[V](cause)

  override def foreach[U](f: T => U): Unit = ()
}

object Failed {
  def apply[T](throwable: Throwable): Failed[T] =
    Failed[T](ScommenderException.adopt(throwable))
}