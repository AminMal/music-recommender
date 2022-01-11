package scommender
package utils.box

import exception.ScommenderException

import scala.concurrent.{ExecutionContext, Future}

/** Holds a value ot type Box of Future of T, and provides useful functions to use instances of
 * this class easily in for comprehensions.
 *
 * @param underlying actual value inside
 * @param ec         an instance of ExecutionContext to provide transformations on Future
 * @tparam T type of value inside Box of Future
 */
class BoxF[+T](val underlying: Box[Future[T]])(implicit ec: ExecutionContext) extends BoxSupport {

  /** Maps the value inside the Box of Future using given function
   *
   * @see [[utils.box.Box.map]]
   * @param f function to be applied to value
   * @tparam V return type of the function on application
   * @return an instance of BoxF holding the new value
   */
  def map[V](f: T => V): BoxF[V] =
    new BoxF[V](underlying.map(_.map(f)))

  /** Maps and flattens the value inside the box of Future using given function
   *
   * @see [[utils.box.Box.flatMap]]
   * @param f function to be applied to the value
   * @tparam V value inside the box on application of function to the underlying value
   * @return an instance of BoxF holding the new value
   */
  def flatMap[V](f: T => BoxF[V]): BoxF[V] = {
    val futureBox = underlying.flatMap { future =>
      val unflatFutureInBox = future.map(f)
      Box(unflatFutureInBox.map(_.underlying).map {
        case Successful(value) => value
        case Failed(cause) => throw cause
      }.flatten)
    }
    toBoxF(futureBox)
  }


  /** checks if the given predicate holds for the value inside the Box of Future, returns Failed if not so.
   *
   * @see [[utils.box.Box.filter]]
   * @param predicate predicate to pass for value inside
   * @return the same instance if is successful and predicate holds, otherwise, Failed instance of Box
   */
  def filter(predicate: T => Boolean): BoxF[T] =
    new BoxF[T](underlying.map(_.filter(predicate)))

  def withFilter(predicate: T => Boolean): BoxF[T] = filter(predicate)

  /** performs given side effect function on the value
   *
   * @see [[utils.box.Box.foreach]]
   * @param f side effect function
   * @tparam U return type of the function, is ignored
   */
  def foreach[U](f: Future[T] => U): Unit = underlying.foreach(f)

  /** returns a value of type V on each case
   *
   * @see [[utils.box.Box.fold]]
   * @param fa function to be applied to failed case
   * @param fb function to be applied to success case
   * @tparam V return type of the value
   * @return instance of type V
   */
  def fold[V](fa: ScommenderException => V, fb: Future[T] => V): V =
    underlying.fold(fa, fb)

  /** recovers the value inside, in case of fault
   *
   * @param fn function to recover future value inside
   * @tparam U return type of partial functino on application to the underlying value
   * @return a new Instance of BoxF
   */
  def recover[U >: T](fn: PartialFunction[Throwable, U]): BoxF[U] =
    toBoxF(underlying.map(_.recover(fn)))
}
