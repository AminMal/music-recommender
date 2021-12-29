package ir.ac.usc
package utils.box

import akka.actor.ActorRef
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}
import akka.pattern.ask
import exception.ScommenderException
import models.responses.{ScommenderResponse, SuccessResponse}

trait BoxSupport {

  implicit class BoxFOps[+T](value: BoxF[T])(implicit ec: ExecutionContext) {
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

  implicit class ActorBoxOps(ref: ActorRef)(implicit ec: ExecutionContext) {
    def ??[T : ClassTag](msg: Any)(implicit to: Timeout): BoxF[T] = {
      toBoxF((ref ? msg).mapTo[Box[T]])
    }
  }

  trait BaseMagnet[C[+_], +T] {
    def invoke(): C[T]
  }

  trait BoxFMagnet[+T] extends BaseMagnet[BoxF, T]

  trait BoxMagnet[+T] extends BaseMagnet[Box, T]

  def toBoxF[T](magnet: BoxFMagnet[T])(implicit ec: ExecutionContext): BoxF[T] = magnet.invoke()

  def toBox[T](magnet: BoxMagnet[T]): Box[T] = magnet.invoke()

  type EC = ExecutionContext
  implicit def futureToBoxMagnet[T](future: => Future[T])(implicit ec: EC): BoxFMagnet[T] = () => {
    new BoxF[T](Box(future))
  }

  implicit def futureBoxToBoxMagnet[T](box: Box[Future[T]])(implicit ec: EC): BoxFMagnet[T] = () => {
    new BoxF[T](box)
  }

  implicit def boxFutureToBoxMagnet[T](future: => Future[Box[T]])(implicit ec: EC): BoxFMagnet[T] = () => {
    val simpleFuture: Future[T] = future.map {
      case Successful(value) => value
      case Failed(cause) => throw cause
    }

    new BoxF[T](Box(simpleFuture))
  }

  implicit def tryToBoxMagnet[T](tryObj: Try[T]): BoxMagnet[T] = () =>
    tryObj.fold(
      fa = throwable => Failed[T](ScommenderException.adopt(throwable)),
      fb = value => Successful[T](value)
    )

  implicit def eitherToBoxMagnet[T](either: Either[Throwable, T]): BoxMagnet[T] = tryToBoxMagnet(either.toTry)

  implicit def codeBlockToBoxMagnet[T](block: => T): BoxMagnet[T] = tryToBoxMagnet(Try(block))
}

object BoxSupport extends BoxSupport