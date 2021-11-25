package io.grpc.examples.helloworld;

import scala.concurrent.{ExecutionContext, Future}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import org.scalamock.scalatest.MockFactory
import io.grpc.{Server, ServerBuilder, ManagedChannelBuilder}

import network.{NetworkServer, NetworkClient}
import message.connection.ConnectionGrpc
import message.connection._

class MockNetworkServer(requiredWorkerNum: Int) extends MockFactory {
  // build mock server
  val mockConnectionImpl = mock[ConnectionGrpc.Connection]
  val mockServer = ServerBuilder.forPort(8002)
    .addService(ConnectionGrpc.bindService(mockConnectionImpl, ExecutionContext.global))
    .build
    .start

  def shutdown = mockServer.shutdown
}

class TestNetworkClient() {
  // build test client
  val client = new NetworkClient("localhost", 8002)

  def shutdown(id: Int) = client.shutdown(id)
}

class NetworkClientTest extends AnyFunSuite with MockFactory {
  test("Client send ConnectRequest to server and then terminate") {
    val mockServer = new MockNetworkServer(1)
    val testClient = new TestNetworkClient

    val id = 1
    val ip = "localhost"
    val port = 9002

    try {
      (mockServer.mockConnectionImpl.connect _).expects(ConnectRequest(ip = ip, port = port))
        .returning(Future.successful(ConnectResponse(success = true, id = id)))
      (mockServer.mockConnectionImpl.terminate _).expects(TerminateRequest(id = id))
        .returning(Future.successful(TerminateResponse()))

      testClient.client.connect(ip, port)
    } finally {
      testClient.shutdown(id)
      mockServer.shutdown
    }
  }
}
