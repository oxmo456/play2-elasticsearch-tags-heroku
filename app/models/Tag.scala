package models

import utils.PkFormat

import play.api.db._
import play.api.Play.current
import play.api.libs.json._

import anorm._
import anorm.SqlParser._

case class Tag(id: Pk[Long], name: String)

object Tag {

  implicit object TagFormat extends Format[Tag] {

    private def extractId(tag: Tag): JsValue = {
      tag.id match {
        case Id(id) => JsNumber(id)
        case NotAssigned => JsNull
      }
    }

    def reads(json: JsValue): JsResult[Tag] = JsSuccess(Tag(
      (json \ "id").as[Pk[Long]],
      (json \ "name").as[String]
    ))

    def writes(tag: Tag): JsValue = JsObject(Seq(
      "id" -> extractId(tag),
      "name" -> JsString(tag.name)
    ))

  }

  val tagRowParser = {
    get[Pk[Long]]("id") ~
      get[String]("name") map {
      case id ~ name => Tag(id, name)
    }
  }

  def findAll(): Seq[Tag] = {
    DB.withConnection {
      implicit connection =>
        SQL("SELECT * from tags").as(tagRowParser *)
    }
  }


}
