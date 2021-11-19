package io.grpc.examples.helloworld;

import scala.concurrent.ExecutionContext
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import io.grpc.{Server, ServerBuilder, ManagedChannelBuilder}
import io.grpc.examples.helloworld.HelloWorldServer
import io.grpc.examples.helloworld.helloworld.{GreeterGrpc, HelloRequest, HelloReply}


trait BaseServerTest extends AnyFunSuite with BeforeAndAfterEach {
	var helloServer: HelloWorldServer = null

	override def beforeEach(): Unit = {
		helloServer = new HelloWorldServer(ExecutionContext.global, 8088)
    helloServer.start
    assert (helloServer.server != null)
		super.beforeEach
	}

	override def afterEach(): Unit = {
		helloServer.stop
    helloServer = null
		super.afterEach
	}
}

class HelloWorldServerTest extends AnyFunSuite with BaseServerTest {
	test("server replies to sayHello with HelloReply") {
    val name = "test";
    val expected = s"Hello $name"

    val channel = ManagedChannelBuilder.forAddress("localhost", 8088).usePlaintext.build
    val blockingStub = GreeterGrpc.blockingStub(channel)
    val response = blockingStub.sayHello(new HelloRequest(name))

    assert (response.message == expected)

    channel.shutdown
	}

  test("server replies again to sayHelloAgain with HelloReply") {
    val name = "test";
    val expected = s"Hello Again $name"

    val channel = ManagedChannelBuilder.forAddress("localhost", 8088).usePlaintext.build
    val blockingStub = GreeterGrpc.blockingStub(channel)
    val response = blockingStub.sayHelloAgain(new HelloRequest(name))

    assert (response.message == expected)

    channel.shutdown
	}
}
