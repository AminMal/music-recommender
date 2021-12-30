package ir.ac.usc
package utils.box

import exception.ScommenderException

import scala.util.Try
import scala.util.control.NonFatal

sealed trait Box[+T] {

  def map[V](f: T => V): Box[V]

  def flatMap[V](f: T => Box[V]): Box[V]

  def filter(predicate: T => Boolean): Box[T]

  def foreach[U](f: T => U): Unit

  def fold[V](fa: ScommenderException => V, fb: T => V): V = this match {
    case Successful(value) => fb(value)
    case Failed(cause) => fa(cause)
  }

  def peek[U](f: T => U): this.type = {foreach[U](f); this}

  def recover[U >: T](f: ScommenderException => U): Box[U] = this match {
    case Successful(value) => Successful[U](value)
    case Failed(cause) => Box[U](f(cause))
  }

  def recoverWith[U >: T](f: ScommenderException => Box[U]): Box[U] = this match {
    case Successful(value) => Successful[U](value)
    case Failed(cause) => f(cause)
  }

  def flatten[U](implicit ev: T <:< Box[U]): Box[U]

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
  override def map[V](f: T => V): Box[V] = Box(f(value))

  override def flatMap[V](f: T => Box[V]): Box[V] = f(value)

  override def foreach[U](f: T => U): Unit = f(value)

  override def flatten[U](implicit ev: T <:< Box[U]): Box[U] = value

  override def filter(predicate: T => Boolean): Box[T] =
    try {
      if (predicate(value)) this else Failed[T](
        new NoSuchElementException(s"Predicate does not hold for value: $value")
      )
    } catch { case NonFatal(e) => Failed[T](e) }
}

case class Failed[+T](cause: ScommenderException) extends Box[T] {
  override def map[V](f: T => V): Box[V] = Failed[V](cause)

  override def flatMap[V](f: T => Box[V]): Box[V] = Failed[V](cause)

  override def foreach[U](f: T => U): Unit = ()

  override def flatten[U](implicit ev: T <:< Box[U]): Box[U] = this.asInstanceOf[Box[U]]

  override def filter(predicate: T => Boolean): Box[T] = this
}

object Failed {
  def apply[T](throwable: Throwable): Failed[T] =
    Failed[T](ScommenderException.adopt(throwable))
}