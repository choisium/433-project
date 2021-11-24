package io.grpc.examples.helloworld;

import scala.concurrent.{ExecutionContext, Future}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import org.scalamock.scalatest.MockFactory
import io.grpc.{Server, ServerBuilder, ManagedChannelBuilder}

import network.{NetworkServer, NetworkClient}
import message.connection.{ConnectionGrpc, ConnectRequest, ConnectResponse}

trait BaseClientTest extends AnyFunSuite with BeforeAndAfterAll with MockFactory {
  val connectionImpl = stub[ConnectionGrpc.Connection]
  var client: NetworkClient = null
  var mockServer: Server = null
  val testPort = 8088

  override def beforeAll(): Unit = {
    mockServer = ServerBuilder.forPort(testPort)
      .addService(ConnectionGrpc.bindService(connectionImpl, ExecutionContext.global))
      .build
      .start

    client = new NetworkClient("localhost", testPort)

    super.beforeAll
  }

  override def afterAll(): Unit = {
    client.shutdown
    client = null

    mockServer.shutdown

    super.afterAll
  }
}

class NetworkClientTest extends AnyFunSuite with BaseClientTest {
    test("client tries to connect") {
      val ip = "localhost"
      val port = 8081
      (connectionImpl.connect _).when(ConnectRequest(ip = ip, port = port)).returns(Future.successful(ConnectResponse(id = 1, success = true)))

      client.connect(ip, port)

      (connectionImpl.connect _).verify(ConnectRequest(ip = ip, port = port))
    }
}