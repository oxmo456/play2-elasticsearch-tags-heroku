package services.elasticsearch

import akka.actor.{ActorRef, Actor}
import services.elasticsearch.ElasticSearchClient.{Agent, Search}
import org.elasticsearch.client.transport.TransportClient
import scala.util.{Failure, Success, Try}

class SearchAgent(transportClient: TransportClient, client: ActorRef) extends Agent(transportClient) {
  def receive: Actor.Receive = {
    case Search(query) => {
      Try {
        transportClient.prepareSearch(query.indices: _*)
          .setTypes(query.indicesTypes: _*)
          .setSearchType(query.searchType)
          .setQuery(query.content)
          .execute()
          .actionGet()
      } match {
        case Success(result) => {
          client ! result.toString
        }
        case Failure(e) => {
          client ! akka.actor.Status.Failure(e)
        }
      }

    }
  }
}
