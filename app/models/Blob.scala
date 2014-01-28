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
import play.api.Logger

case class Blob(id: Pk[Long], name: String, tags: Set[Tag] = Set.empty)

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
      case id ~ name => Blob(id, name)
    }
  }

  def save(blob: Blob): Int = {
    //TODO check if blob exists...
    DB.withConnection {
      implicit connection =>
        SQL( """
              DELETE FROM blobs_tags
              WHERE blob_id = {blobId}
             """).on('blobId -> blob.id).execute()

        val tags = blob.tags.map(tag => {
          SQL( """
              WITH a AS (SELECT * FROM tags WHERE name = {name}),
              b AS (INSERT INTO tags (name) SELECT {name} WHERE NOT EXISTS (SELECT 1 FROM a) RETURNING *)
              SELECT * FROM a UNION ALL SELECT * FROM b
               """).on('name -> tag.name).as(Tag.tagRowParser.singleOpt)
        }).flatten


        (SQL( """
              INSERT INTO blobs_tags (blob_id,tag_id) VALUES ({blobId},{tagId})
              """).asBatch /: tags) {
          (sql, tag) => {
            sql.addBatch("blobId" -> blob.id, "tagId" -> tag.id)
          }
        }.execute()

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

  val rowParser = get[Long]("id") ~
    get[String]("name") ~
    get[Option[Long]]("tagId") ~
    get[Option[String]]("tagName") map {
    case id ~ name ~ Some(tagId) ~ Some(tagName) => ((id, name), Some(tagId, tagName))
    case id ~ name ~ None ~ None => ((id, name), None)

  }

  def findById(id: Long): Option[Blob] = {
    DB.withConnection {
      implicit connection =>

        SQL( """
              SELECT
              blobs.id as id,
              blobs.name as name,
              tags.id as tagId,
              tags.name as tagName
              FROM blobs
              LEFT JOIN blobs_tags ON blobs_tags.blob_id = blobs.id
              LEFT JOIN tags ON tags.id = blobs_tags.tag_id
              WHERE blobs.id = {blobId}
             """).on("blobId" -> id).as(rowParser *).groupBy(_._1).map(a => {
          (a._1, a._2.map(_._2))
        }).map(a => {
          Blob(Id(a._1._1), a._1._2, a._2.flatten.map(a => {
            Tag(Id(a._1), a._2)
          }).toSet)
        }).headOption

      /*
      SQL("SELECT * from blobs WHERE id = {id}")
        .on('id -> id)
        .as(blobRowParser.singleOpt)
      */
    }
  }

}
