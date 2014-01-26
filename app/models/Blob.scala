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

case class Blob(id: Pk[Long], name: String)

object Blob {

  implicit object SearchFormat extends Format[Blob] {

    private def extractId(blob: Blob): JsValue = {
      blob.id match {
        case Id(id) => JsNumber(id)
        case NotAssigned => JsNull
      }
    }

    def reads(json: JsValue): JsResult[Blob] = JsSuccess(Blob(
      (json \ "id").as[Pk[Long]],
      (json \ "name").as[String]
    ))

    def writes(blob: Blob): JsValue = JsObject(Seq(
      "id" -> extractId(blob),
      "name" -> JsString(blob.name)
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
    DB.withConnection {
      implicit connection =>
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
