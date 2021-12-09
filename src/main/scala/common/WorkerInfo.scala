package common

import message.common.{WorkerMessage, RangeMessage}


// State case objects
sealed trait MasterState
case object MASTERINIT extends MasterState    // Initial state
case object CONNECTED extends MasterState     // When all workers are connnected
case object PIVOTED extends MasterState       // When pivoting done
case object SHUFFLING extends MasterState        // When all workers are sorted(in a worker)
case object TERMINATE extends MasterState     // When all workers are terminated
case object FAILED extends MasterState        // Failed state

sealed trait WorkerState
case object WORKERINIT extends WorkerState    // Initial state
case object SAMPLED extends WorkerState       // When sample done
case object SORTED extends WorkerState   // When sort and partition done
case object SHUFFLED extends WorkerState      // When all partitioned files are sent
case object DONE extends WorkerState          // When merge done


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
