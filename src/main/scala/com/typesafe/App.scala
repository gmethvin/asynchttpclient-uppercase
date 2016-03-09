package com.typesafe

import java.lang.management.ManagementFactory
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorSystem, Props}
import com.ning.http.client._
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider
import org.jboss.netty.logging.{InternalLoggerFactory, Slf4JLoggerFactory}
import org.slf4j.Logger

import scala.concurrent.duration._

object App {

  val numberOfRequests = 1
  val buildWithHanging: Boolean = true

  private val logger: Logger = org.slf4j.LoggerFactory.getLogger(classOf[App])

  @throws(classOf[InterruptedException])
  def main(args: Array[String]) {
    InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory())

    logger.info("id = {}", ManagementFactory.getRuntimeMXBean.getName)

    val actorSystem = ActorSystem()

    val scheduler = actorSystem.scheduler

    // Give some time to connect Yourkit and see the open threads...
    scheduler.scheduleOnce(10.seconds) {
      val app = actorSystem.actorOf(AsyncHttpClientActor.props(numberOfRequests, buildWithHanging))

      app ! Get("HTTP://EXAMPLE.COM")
      app ! Post("HTTP://EXAMPLE.COM")
      app ! Get("HTTP://example.net/")
      app ! Get("http://EXAMPLE.ORG")
      app ! Get("http://example.org")

    }(actorSystem.dispatchers.defaultGlobalDispatcher)
  }
}

sealed trait Request
case class Get(url: String) extends Request
case class Post(url: String) extends Request

object AsyncHttpClientActor {
  def props(numberOfRequests: Int, buildWithHanging: Boolean): Props = Props(new AsyncHttpClientActor(numberOfRequests, buildWithHanging))
}

class AsyncHttpClientActor(val numberOfRequests: Int, val buildWithHanging: Boolean) extends Actor {
  private val logger = org.slf4j.LoggerFactory.getLogger(this.getClass.getName)
  private var client: AsyncHttpClient = _
  private val requestsOpen: AtomicInteger = new AtomicInteger()

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    val builder = new AsyncHttpClientConfig.Builder()

    val config: AsyncHttpClientConfig = builder.build

    val provider: AsyncHttpProvider = new NettyAsyncHttpProvider(config)
    client = new AsyncHttpClient(provider, config)
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    client.close()
  }

  def receive = {
    case Get(url: String) => makeRequest(client.prepareGet(url))
    case Post(url: String) => makeRequest(client.preparePost(url))
  }

  def makeRequest(request: AsyncHttpClient#BoundRequestBuilder): Unit = {
    val future = request.execute
    requestsOpen.incrementAndGet()
    future.addListener(new Runnable {
      override def run(): Unit = {
        val count = requestsOpen.decrementAndGet()
        val response = future.get()
        logger.info(s"Listening for response $response, requestsOpen = $count")
      }
    }, context.dispatcher)
  }
}
