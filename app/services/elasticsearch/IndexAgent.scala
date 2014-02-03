package services.elasticsearch

import akka.actor.Actor
import org.elasticsearch.client.transport.TransportClient
import services.elasticsearch.ElasticSearchClient.{Agent, Index}
import play.api.Logger
import scala.util.{Success, Failure, Try}


class IndexAgent(client: TransportClient) extends Agent(client) {


  def receive: Actor.Receive = {

    case Index(document) => {
      Try {
        client.prepareIndex(document.index, document.indexType, document.id)
          .setSource(document.source)
          .execute()
          .actionGet()
      } match {
        case Success(result) => ()
        case Failure(e) => ()
      }
    }

  }
}
