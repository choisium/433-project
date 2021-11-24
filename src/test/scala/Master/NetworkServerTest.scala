package io.grpc.examples.helloworld;

import scala.concurrent.ExecutionContext
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import io.grpc.{Server, ServerBuilder, ManagedChannelBuilder}

import network.{NetworkServer}
import message.connection.{ConnectionGrpc, ConnectRequest, ConnectResponse}


trait ServerTest extends AnyFunSuite with BeforeAndAfterAll {
	var helloServer: NetworkServer = null
  val testPort = 8089

	override def beforeAll(): Unit = {
		helloServer = new NetworkServer(ExecutionContext.global, testPort, 1)
    helloServer.start
    assert (helloServer.server != null)
		super.beforeAll
	}

	override def afterAll(): Unit = {
		helloServer.stop
    helloServer = null
		super.afterAll
	}
}

class NetworkServerTest extends AnyFunSuite with ServerTest {
  test("Connect to server replies success message") {
    val channel = ManagedChannelBuilder.forAddress("localhost", testPort).usePlaintext.build
    val blockingStub = ConnectionGrpc.blockingStub(channel)
    val response = blockingStub.connect(new ConnectRequest("localhost", 8090))

    assert (response.id == 1)
    assert (response.success == true)

    channel.shutdown
  }
}