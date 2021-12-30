package scommender
package utils.box

import exception.ScommenderException

import scala.util.Try
import scala.util.control.NonFatal


/**
 * Box is a safe wrapper over values (~Either[ScommenderException, T])
 * that is a domain specific standard, that has useful methods to deal with the wrapped value
 * also see [[utils.box.BoxF]] which wraps Future boxes.
 *
 * @tparam T type of the value inside the box
 */
sealed trait Box[+T] {

  /** map the value inside the box (if is successful) using the given function.
   *
   * @param f function to convert the value inside
   * @tparam V type of the return value in box
   * @return value returned by the function, wrapped inside a box
   */
  def map[V](f: T => V): Box[V]

  /** creates a new Box by applying the function to the value inside the box, if is successful.
   *
   * @param f the function to apply to successful box result
   * @tparam V type of the value inside the new box
   * @return the result of applying f to value inside the box
   */
  def flatMap[V](f: T => Box[V]): Box[V]

  /** turns the value inside the box if is successful and passes the predicate
   *
   * @param predicate the function to check for value
   * @return the same successful value if passes the predicate
   */
  def filter(predicate: T => Boolean): Box[T]

  /** performs a side effect for successful value box.
   *
   * @param f side effect function to apply to value inside
   * @tparam U return type of the function, ignored
   */
  def foreach[U](f: T => U): Unit

  /** returns a value of type U, given the functions for each case
   *
   * @param fa function to apply to failure cause
   * @param fb function to apply to successful value
   * @tparam V return type of the result
   * @return an instance of V, based on given functions
   */
  def fold[V](fa: ScommenderException => V, fb: T => V): V = this match {
    case Successful(value) => fb(value)
    case Failed(cause) => fa(cause)
  }

  /** applies the given side effect, but also returns this, can be used for seeing the value inside,
   * but also, holding the same value
   *
   * @param f function that applies side effect
   * @tparam U return type of the given function, is ignored
   * @return this
   */
  def peek[U](f: T => U): this.type = {
    foreach[U](f); this
  }

  /** recovers the result of box in case of failure.
   *
   * @param f the function to recover the failure mode.
   * @tparam U type of the value inside the new box
   * @return a box containing recovered value in case of failure
   */
  def recover[U >: T](f: ScommenderException => U): Box[U] = this match {
    case successCase@Successful(_) => successCase
    case Failed(cause) => Box[U](f(cause))
  }

  /** recovers failure mode with a prepared box value.
   *
   * @param f function to use in case of failure
   * @tparam U
   * @return
   */
  def recoverWith[U >: T](f: ScommenderException => Box[U]): Box[U] = this match {
    case successCase@Successful(_) => successCase
    case Failed(cause) => f(cause)
  }

  /** Does a side effect for failed box.
   *
   * @param f function to perform side effect
   * @tparam U return type of result of application of function to the failure cause, ignored
   */
  def ifFailed[U](f: ScommenderException => U): Unit

  /** Flattens the value inside the box, if is a subclass of Box.
   *
   * @param ev evidence to make sure value inside is a subclass of Box.
   * @tparam U type of the inside box.
   * @return flattened value inside
   */
  def flatten[U](implicit ev: T <:< Box[U]): Box[U]

}

/** Companion object for Box trait, which holds some useful functions in order to create box instances.
 *
 */
object Box {
  /** wraps a value inside the box
   *
   * @param code block of code to execute and convert to box
   * @tparam T result type of the code execution
   * @return Box containing code result
   */
  def apply[T](code: => T): Box[T] = fromTry(Try(code))

  /** creates a box instance from a Try value.
   *
   * @param t input Try value.
   * @tparam T type of the value inside the Box.
   * @return a Box containing given Try value.
   */
  def fromTry[T](t: Try[T]): Box[T] = t fold(
    fa = throwable => Failed[T](throwable),
    fb = value => Successful(value)
  )

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
    } catch {
      case NonFatal(e) => Failed[T](e)
    }

  override def ifFailed[U](f: ScommenderException => U): Unit = ()
}

case class Failed[+T](cause: ScommenderException) extends Box[T] {
  override def map[V](f: T => V): Box[V] = Failed[V](cause)

  override def flatMap[V](f: T => Box[V]): Box[V] = Failed[V](cause)

  override def foreach[U](f: T => U): Unit = ()

  override def flatten[U](implicit ev: T <:< Box[U]): Box[U] = this.asInstanceOf[Box[U]]

  override def filter(predicate: T => Boolean): Box[T] = this

  override def ifFailed[U](f: ScommenderException => U): Unit = f(cause)
}

object Failed {
  def apply[T](throwable: Throwable): Failed[T] =
    Failed[T](ScommenderException.adopt(throwable))
}