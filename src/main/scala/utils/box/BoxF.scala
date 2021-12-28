package ir.ac.usc
package utils.box

import exception.ScommenderException

import scala.concurrent.{ExecutionContext, Future}

class BoxF[+T](val underlying: Box[Future[T]])(implicit ec: ExecutionContext) extends BoxSupport {
  def map[V](f: T => V): BoxF[V] =
    new BoxF[V](underlying.map(_.map(f)))

  def flatMap[V](f: T => Future[V]): BoxF[V] =
    new BoxF[V](underlying.map(_.flatMap(f)))

  def foreach[U](f: Future[T] => U): Unit = underlying.foreach(f)

  def fold[V](fa: ScommenderException => V, fb: Future[T] => V): V =
    underlying.fold(fa, fb)

  def recover[U >: T](fn: PartialFunction[Throwable, U]): BoxF[U] =
    toBoxF(underlying.map(_.recover(fn)))
}
