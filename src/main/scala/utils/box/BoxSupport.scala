package scommender
package utils.box

import exception.ScommenderException
import models.responses.{ScommenderResponse, SuccessResponse}

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/** Provides some useful features to work with Boxes and BoxFs
 *
 * {{{
 *   class SomeService extends SomeClass with BoxSupport {
 *     lazy val unsafeFuture: Future[Int] = throw new RuntimeException("test")
 *
 *     val safeBox: BoxF[Int] = toBoxF(unsafeFuture) // BoxF(Failed(ScommenderException))
 *   }
 * }}}
 */
trait BoxSupport {

  /** Wraps a BoxF value to provide actor pipe support, and response conversion
   *
   * @param value BoxF value to provide features for
   * @param ec    an instance of ExecutionContext to perform map transformations on future value inside
   * @tparam T type of the Future value this BoxF contains
   */
  implicit class BoxFOps[+T](value: BoxF[T])(implicit ec: ExecutionContext) {
    /** Pipe the result of BoxF computation to receiver as an instance of Box[T]
     *
     * @param ref message recipient
     * @return same value this box contains
     */
    def pipeTo(ref: ActorRef): BoxF[T] = {
      value.underlying match {
        case Successful(res) => res.andThen {
          case Failure(exception) => ref ! Failed[T](exception)
          case Success(value) => ref ! Successful[T](value)
        }
        case failed@Failed(_) => ref ! failed
      }

      value
    }

    /** converts the BoxF value to ScommenderResponse in Future
     *
     * @return scommender response equivalent in Future.
     */
    def toScommenderResponse: Future[ScommenderResponse[T]] =
      value.fold(
        fa = throwable => Future.successful(ScommenderException.adopt(throwable).toResponseBody),
        fb = value => value
          .map(data => SuccessResponse(data = data))
          .recover {
            case throwable => ScommenderException.adopt(throwable).toResponseBody
          }
      )
  }

  /** Provides ask pattern that returns a Box as result.
   *
   * @param ref actor to ask message from
   * @param ec  an instance of ExecutionContext to perform ask on actor
   */
  implicit class ActorBoxOps(ref: ActorRef)(implicit ec: ExecutionContext) {
    /** provides ask pattern that returns BoxF as result
     *
     * {{{
     *   class YourService extends BoxSupport {
     *     val boxActor: ActorRef = ???
     *
     *     val safeAskResult: BoxF[Int] = boxActor ??[Int] "give me the int"
     *   }
     * }}}
     *
     * @param msg message to ask from actor
     * @param to  timeout for ask
     * @tparam T expected type for box value result
     * @return an instance of BoxF
     */
    def ??[T: ClassTag](msg: Any)(implicit to: Timeout): BoxF[T] = {
      toBoxF((ref ? msg).mapTo[Box[T]])
    }
  }

  type EC = ExecutionContext

  /** Provides an instance of BoxF using given magnet
   *
   * @param magnet magnet that can create instance of BoxF of specific type T on invocation.
   * @tparam T the type this BoxF contains
   * @return an instance of BoxF on magnet invocation
   */
  def toBoxF[T](magnet: BoxFMagnet[T]): BoxF[T] = magnet.invoke()

  /** Provides an instance of Box using given magnet
   *
   * @param magnet magnet that can create instance
   * @tparam T magnet that can create instance of Box of specific type T on invocation.
   * @return an instance of Box on magnet invocation
   */
  def toBox[T](magnet: BoxMagnet[T]): Box[T] = magnet.invoke()

  /** Provides Container value in invocation
   *
   * @tparam C container type
   * @tparam T value inside container type
   */
  trait BaseMagnet[C[+_], +T] {
    /** Provides an instance of container of t
     *
     * @return instance of C[T]
     */
    def invoke(): C[T]
  }

  /** Creates box values
   *
   * @tparam T value inside box
   */
  trait BoxFMagnet[+T] extends BaseMagnet[BoxF, T]

  /** Creates BoxF values
   *
   * @tparam T value inside BoxF
   */
  trait BoxMagnet[+T] extends BaseMagnet[Box, T]

  /** magnet to convert a future instance to BoxF
   *
   * @param future future instance to convert to BoxF
   * @param ec     execution context instance to convert future instance to BoxF instance
   * @tparam T type of the value inside future
   * @return an instance of BoxFMagnet
   */
  implicit def futureToBoxMagnet[T](future: => Future[T])(implicit ec: EC): BoxFMagnet[T] = () => {
    new BoxF[T](Box(future))
  }

  /** magnet to convert a box holding a future instance to BoxF
   *
   * @param box box instance to convert to BoxF
   * @param ec  execution context instance to convert future instance to BoxF instance
   * @tparam T type of the value inside box of future
   * @return an instance of BoxFMagnet
   */
  implicit def futureBoxToBoxMagnet[T](box: Box[Future[T]])(implicit ec: EC): BoxFMagnet[T] = () => {
    new BoxF[T](box)
  }

  /** magnet to convert a future holding a box instance to BoxF
   *
   * @param future future of box instance to convert to BoxF
   * @param ec     execution context instance to convert future instance to BoxF instance
   * @tparam T type of the value inside future
   * @return an instance of BoxFMagnet
   */
  implicit def boxFutureToBoxMagnet[T](future: => Future[Box[T]])(implicit ec: EC): BoxFMagnet[T] = () => {
    val simpleFuture: Future[T] = future.map {
      case Successful(value) => value
      case Failed(cause) => throw cause
    }

    new BoxF[T](Box(simpleFuture))
  }

  /** magnet to convert a try object to an instance of Box
   *
   * @param tryObj Try object to convert to box
   * @tparam T type of the value inside try object
   * @return an instance of box magnet
   */
  implicit def tryToBoxMagnet[T](tryObj: Try[T]): BoxMagnet[T] = () =>
    Box.fromTry(tryObj)

  /** magnet to convert an either object to an instance of Box
   *
   * @param either Either object to convert to box
   * @tparam T type of value inside right side of either
   * @return an instance of box magnet
   */
  implicit def eitherToBoxMagnet[T](either: Either[Throwable, T]): BoxMagnet[T] = tryToBoxMagnet(either.toTry)

  /** magnet to convert any code block to an instance of Box
   *
   * @param block code block to convert the result of execution to box
   * @tparam T type of the code execution result
   * @return an instance of box magnet
   */
  implicit def codeBlockToBoxMagnet[T](block: => T): BoxMagnet[T] = tryToBoxMagnet(Try(block))
}

/** Companion object for BoxSupport to use to import instead of extension
 *
 */
object BoxSupport extends BoxSupport