package services.elasticsearch

import akka.actor.{Props, Actor}
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import services.elasticsearch.ElasticSearchClient.{Search, Index}
import akka.pattern.ask
import play.api.Logger

object ElasticSearchClient {

  abstract class Agent(transportClient: TransportClient) extends Actor

  case class Index(document: Document)

  case class Search(query: Query)

  case class SearchResult(result: String)

}

class ElasticSearchClient(host: String, port: Int) extends Actor {

  private val transportAddress = new InetSocketTransportAddress(host, port)
  private val transportClient = new TransportClient().addTransportAddress(transportAddress)

  def receive: Actor.Receive = {

    case index: Index => {
      val indexAgent = context.actorOf(Props(classOf[IndexAgent], transportClient))
      indexAgent ! index
    }
    case search: Search => {
      val searchAgent = context.actorOf(Props(classOf[SearchAgent], transportClient, sender))
      searchAgent ! search
    }
    case m => {
      Logger.logger.debug(s"CLIENT $m")
    }

  }

}

