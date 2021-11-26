/*
  Make network request for connection to master.
  NetworkClient send request, and NetworkClient responed to the request.
  main.Worker set this client.
*/

package network

import com.google.protobuf.ByteString

import java.util.concurrent.TimeUnit
import java.util.logging.Logger

import io.grpc.{ManagedChannelBuilder, Status}
import io.grpc.stub.StreamObserver

import scala.io.Source

import message.connection.ConnectionGrpc
import message.connection._


class NetworkClient(host: String, port: Int) {
  val logger: Logger = Logger.getLogger(classOf[NetworkClient].getName)

  val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext.build
  val blockingStub = ConnectionGrpc.blockingStub(channel)
  val asyncStub = ConnectionGrpc.stub(channel)

  var id: Int = -1

  def shutdown: Unit = {
    if (id > 0) {
      val response = blockingStub.terminate(new TerminateRequest(id))
    } else {}
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  def connect(ip: String, port: Int): Boolean = {
    val response = blockingStub.connect(new ConnectRequest(ip, port))
    id = response.id
    logger.info("Connection result: " + response.success)
    response.success
  }
}
