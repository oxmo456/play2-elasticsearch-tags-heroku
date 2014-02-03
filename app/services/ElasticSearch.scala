package services

import play.api.Play
import services.elasticsearch.{Query, Document, ElasticSearchClient}
import play.api.libs.concurrent.Akka
import akka.actor.Props
import akka.pattern.ask
import models.Blob
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory._
import services.elasticsearch.ElasticSearchClient.{Search, Index}
import play.api.Play.current
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.{QueryBuilders, QueryBuilder}
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Future

object ElasticSearch {
  lazy val host = Play.application.configuration.getString("elasticsearch.host").get
  lazy val port = Play.application.configuration.getInt("elasticsearch.port").get

  private val elasticSearchClient = Akka.system.actorOf(Props(classOf[ElasticSearchClient], host, port))


  case class BlobDocument(blob: Blob) extends Document {
    def index: String = "blobs"

    def indexType: String = "blob"

    def id: String = blob.id.get.toString

    def source: XContentBuilder = jsonBuilder()
      .startObject()
      .field("name", blob.name)
      .array("tags", blob.tags.map(_.name): _*)
      .endObject()
  }

  case class BlobSearch(value: String) extends Query {
    def indices: List[String] = List("blobs")

    def indicesTypes: List[String] = Nil

    def searchType: SearchType = SearchType.DEFAULT

    def content: QueryBuilder = QueryBuilders.fuzzyQuery("_all", value)
  }


  def indexBlob(blob: Blob) {
    elasticSearchClient ! Index(BlobDocument(blob))
  }

  implicit val timeout = Timeout(5 seconds)

  def searchBlob(value: String): Future[Any] = {
    elasticSearchClient ? Search(BlobSearch(value))
  }

}
