package services.elasticsearch

import akka.actor.{ActorRef, Actor}
import services.elasticsearch.ElasticSearchClient.{Agent, Search}
import org.elasticsearch.client.transport.TransportClient
import scala.util.{Failure, Success, Try}
import play.api.Logger

class SearchAgent(transportClient: TransportClient, client: ActorRef) extends Agent(transportClient) {
  def receive: Actor.Receive = {
    case Search(query) => {
      Try {

        Logger.logger.debug("???" + query.content.toString)

        transportClient.prepareSearch(query.indices: _*)
          .setTypes(query.indicesTypes: _*)
          .setSearchType(query.searchType)
          .setQuery(query.content)
          .execute()
          .actionGet()
      } match {
        case Success(result) => {
          Logger.logger.debug(s"SEARCH SUCCESS $result")
          client ! result.toString
        }
        case Failure(e) => {
          Logger.logger.debug(s"SEARCH FAILURE $e")
          client ! akka.actor.Status.Failure(e)
        }
      }

    }
  }
}
