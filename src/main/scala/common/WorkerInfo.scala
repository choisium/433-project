package common

import message.connection.WorkerMessage

sealed trait WorkerState
case object WORKERINIT extends WorkerState
case object SAMPLED extends WorkerState
case object PARTITIONED extends WorkerState
case object SHUFFLED extends WorkerState
case object DONE extends WorkerState

class WorkerInfo(val id: Int, val ip: String, val port: Int) {
  var keyRange: (String, String) = null
  var state: WorkerState = WORKERINIT
}

object WorkerInfo {
  def convertToWorkerMessage(workerInfo: WorkerInfo): WorkerMessage = {
    new WorkerMessage(
      workerInfo.id,
      workerInfo.ip,
      workerInfo.port,
      workerInfo.keyRange._1,
      workerInfo.keyRange._2
    )
  }

  def convertMessageToInfo(workerMessage: WorkerMessage): WorkerInfo = {
    val worker = new WorkerInfo(
      workerMessage.id,
      workerMessage.ip,
      workerMessage.port
    )
    worker.keyRange = (workerMessage.lowerbound, workerMessage.upperbound)
    worker.state = SAMPLED
    worker
  }
}
