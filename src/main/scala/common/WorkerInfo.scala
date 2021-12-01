package common

import message.common.{WorkerMessage, RangeMessage}

sealed trait WorkerState
case object WORKERINIT extends WorkerState
case object SAMPLED extends WorkerState
case object PARTITIONED extends WorkerState
case object SHUFFLED extends WorkerState
case object DONE extends WorkerState

class WorkerInfo(val id: Int, val ip: String, val port: Int) {
  type Range = (String, String)
  var keyRange: Range = null
  var subKeyRange: Seq[Range] = null
  var state: WorkerState = WORKERINIT
}

object WorkerInfo {
  def convertToWorkerMessage(workerInfo: WorkerInfo): WorkerMessage = {
    WorkerMessage(
      id = workerInfo.id,
      ip = workerInfo.ip,
      port = workerInfo.port,
      keyRange = Option(RangeMessage(workerInfo.keyRange._1, workerInfo.keyRange._2)),
      subKeyRange = workerInfo.subKeyRange.map{case (lb, ub) => RangeMessage(lb, ub)}
    )
  }

  def convertMessageToInfo(workerMessage: WorkerMessage): WorkerInfo = {
    val worker = new WorkerInfo(
      workerMessage.id,
      workerMessage.ip,
      workerMessage.port
    )
    val keyRange = workerMessage.keyRange.get
    worker.keyRange = (keyRange.lowerbound, keyRange.upperbound)
    worker.subKeyRange = workerMessage.subKeyRange.map{s => (s.lowerbound, s.upperbound)}
    worker.state = SAMPLED
    worker
  }
}
