package io.grpc.examples.helloworld;

import scala.concurrent.{ExecutionContext, Future}
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import org.scalamock.scalatest.MockFactory
import io.grpc.{Server, ServerBuilder, ManagedChannelBuilder}
import io.grpc.examples.helloworld.HelloWorldServer
import io.grpc.examples.helloworld.HelloWorldClient
import io.grpc.examples.helloworld.helloworld.{GreeterGrpc, HelloRequest, HelloReply}


trait BaseClientTest extends AnyFunSuite with BeforeAndAfterEach with MockFactory {
  val greeterImpl = stub[GreeterGrpc.Greeter]
	var helloClient: HelloWorldClient = null
  var helloMockServer: Server = null

	override def beforeEach(): Unit = {
    helloMockServer = ServerBuilder.forPort(8080)
        .addService(GreeterGrpc.bindService(greeterImpl, ExecutionContext.global))
        .build
        .start

    val channel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext.build
    val blockingStub = GreeterGrpc.blockingStub(channel)
    helloClient = new HelloWorldClient(channel, blockingStub)

		super.beforeEach
	}

	override def afterEach(): Unit = {
		helloClient.shutdown
    helloClient = null

    helloMockServer.shutdown

		super.afterEach
	}
}

class HelloWorldClientTest extends AnyFunSuite with BaseClientTest {
    test("client send greet") {
        assert (1 == 1)
        val name = "test name"

        (greeterImpl.sayHello _).when(HelloRequest(name = name)).returns(Future.successful(HelloReply(message = "Hello " + name)))
        (greeterImpl.sayHelloAgain _).when(HelloRequest(name = name)).returns(Future.successful(HelloReply(message = "Hello Again " + name)))

        helloClient.greet(name)

        (greeterImpl.sayHello _).verify(HelloRequest(name = name))
        (greeterImpl.sayHelloAgain _).verify(HelloRequest(name = name))
    }
}