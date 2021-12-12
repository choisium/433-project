package common
import java.util.logging.Level

case class InvalidStateException() extends Exception
case class WorkerFullException() extends Exception
case class WorkerFailedException() extends Exception

object loggerLevel {
  val level = Level.INFO
}