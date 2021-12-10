package network

import scala.concurrent.{ExecutionContext, Future}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import org.scalamock.scalatest.MockFactory
import io.grpc.{Server, ServerBuilder, ManagedChannelBuilder, StatusRuntimeException}

import network.{NetworkServer, NetworkClient}
import message.connection.ConnectionGrpc
import message.connection._
import common._

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

  def shutdown = client.shutdown
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
        .returning(Future.successful(ConnectResponse(id = id)))
      (mockServer.mockConnectionImpl.terminate _).expects(TerminateRequest(id = id))
        .returning(Future.successful(TerminateResponse()))

      testClient.client.requestConnect(ip, port)
      assert(testClient.client.id == id)
    } finally {
      testClient.shutdown
      mockServer.shutdown
    }
  }

  test("Client's request after requiredWorkerNum is full is rejected") {
    val mockServer = new MockNetworkServer(1)
    val testClient1 = new TestNetworkClient
    val testClient2 = new TestNetworkClient

    val id1 = 1
    val ip1 = "localhost"
    val port1 = 9002
    val ip2 = "localhost"
    val port2 = 9003

    try {
      (mockServer.mockConnectionImpl.connect _).expects(ConnectRequest(ip = ip1, port = port1))
        .returning(Future.successful(ConnectResponse(id = id1)))
      (mockServer.mockConnectionImpl.connect _).expects(ConnectRequest(ip = ip1, port = port2))
        .returning(Future.failed(new InvalidStateException))
      (mockServer.mockConnectionImpl.terminate _).expects(TerminateRequest(id = id1))
        .returning(Future.successful(TerminateResponse()))

      testClient1.client.requestConnect(ip1, port1)
      assert(testClient1.client.id == id1)

      assertThrows[StatusRuntimeException] {
        testClient2.client.requestConnect(ip2, port2)
      }
    } finally {
      testClient1.shutdown
      testClient2.shutdown
      mockServer.shutdown
    }
  }
}
