package common

case class InvalidStateException() extends Exception
case class WorkerFullException() extends Exception
case class WorkerFailedException() extends Exception