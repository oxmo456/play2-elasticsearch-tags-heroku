package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._
import play.api.libs.json._
import anorm.~
import anorm.Id
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber
import utils.PkFormat
import scala.util.Random

case class Blob(id: Pk[Long], name: String, tags: Set[Tag] = Set.empty) {

  def setTags(tags: Set[Tag]): Blob = Blob(id, name, tags)

}

object Blob {

  implicit object BlobFormat extends Format[Blob] {

    private def extractId(blob: Blob): JsValue = {
      blob.id match {
        case Id(id) => JsNumber(id)
        case NotAssigned => JsNull
      }
    }

    def reads(json: JsValue): JsResult[Blob] = JsSuccess(Blob(
      (json \ "id").as[Pk[Long]],
      (json \ "name").as[String],
      (json \ "tags").as[Set[Tag]]
    ))

    def writes(blob: Blob): JsValue = JsObject(Seq(
      "id" -> extractId(blob),
      "name" -> JsString(blob.name),
      "tags" -> JsArray()
    ))

  }

  private def randomName(): String = {
    java.lang.Long.toString(Random.nextLong(), 26)
  }


  private val blobRowParser = {
    get[Pk[Long]]("id") ~
      get[String]("name") map {
      case (id@Id(idValue)) ~ name => {
        Blob(id, name, Tag.findByBlobId(idValue))
      }
    }
  }

  private val blobIdRowParser = {
    get[Pk[Long]]("id") map {
      case Id(id) => id
    }
  }

  def save(blob: Blob): Int = {
    //no need to check if blob exists
    DB.withTransaction {
      implicit connection =>
        SQL( """
              DELETE FROM blobs_tags
              WHERE blob_id = {blobId}
             """).on('blobId -> blob.id).executeUpdate()

        blob.tags.foreach(tag => {
          SQL( """
                 WITH a AS (SELECT id FROM tags WHERE name = {name}),
                 b AS (INSERT INTO tags (name) SELECT {name} WHERE NOT EXISTS (SELECT 1 FROM a) RETURNING *)
                 INSERT INTO blobs_tags (blob_id,tag_id) VALUES ({blobId}, (SELECT id FROM a UNION ALL SELECT id FROM b) );
               """).on('name -> tag.name, 'blobId -> blob.id).executeUpdate()
        })

        SQL( """
              UPDATE blobs SET
              name = {name}
              WHERE id = {id}
             """).on('name -> blob.name, 'id -> blob.id).executeUpdate()
    }
  }

  def deleteById(id: Long): Int = {
    DB.withConnection {
      implicit connection =>
        SQL( """
              DELETE FROM blobs
              WHERE id = {id}
             """).on('id -> id).executeUpdate()
    }
  }

  def create(): Option[Long] = {
    DB.withConnection {
      implicit connection =>
        SQL("INSERT INTO blobs (name) VALUES ({name})").on('name -> randomName()).executeInsert()
    }
  }

  def findAll(): Seq[Blob] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT * from blobs").as(blobRowParser *)
    }
  }


  def findById(id: Long): Option[Blob] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT * from blobs WHERE id = {id}")
          .on('id -> id)
          .as(blobRowParser.singleOpt)
    }
  }

}
