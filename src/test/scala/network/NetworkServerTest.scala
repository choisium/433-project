package network;

import scala.concurrent.ExecutionContext
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import io.grpc.{Server, ServerBuilder, ManagedChannelBuilder, ManagedChannel, StatusRuntimeException}

import network.{NetworkServer}
import message.connection.{ConnectionGrpc, ConnectRequest, ConnectResponse}
import common._

class MockNetworkClient() {
  // build mock client(channel)
  lazy val channel = ManagedChannelBuilder.forAddress("localhost", 8001).usePlaintext.build
  lazy val blockingStub = ConnectionGrpc.blockingStub(channel)
  lazy val asyncStub = ConnectionGrpc.stub(channel)

  def shutdown() = channel.shutdown
}

class TestNetworkServer(requiredWorkerNum: Int) {
  // build test server
  val server = new NetworkServer(ExecutionContext.global, 8001, requiredWorkerNum)

  def start() = server.start
  def shutdown() = server.stop
}

class NetworkServerTest extends AnyFunSuite {
  test("Server send ConnectResponse to a client sent ConnectRequest") {
    val testServer = new TestNetworkServer(1)
    val mockClient = new MockNetworkClient()
    testServer.start

    try {
      val response = mockClient.blockingStub.connect(new ConnectRequest("localhost", 9001))
      assert (response.id == 1)
      assert (testServer.server.state == CONNECTED)
    } finally {
      mockClient.shutdown
      testServer.shutdown
    }
  }


  test("Server rejects second ConectRequest when requiredWorkerNum is 1") {
    val testServer = new TestNetworkServer(1)
    val mockClient1 = new MockNetworkClient()
    val mockClient2 = new MockNetworkClient()

    testServer.start

    try {
      val response1 = mockClient1.blockingStub.connect(new ConnectRequest("localhost", 9001))
      assert (response1.id == 1)
      assert (testServer.server.state == CONNECTED)
      assertThrows[StatusRuntimeException] {
        mockClient2.blockingStub.connect(new ConnectRequest("localhost", 9002))
      }
    } finally {
      mockClient1.shutdown
      mockClient2.shutdown
      testServer.shutdown
    }
  }
}
