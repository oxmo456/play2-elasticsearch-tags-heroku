package controllers.api.rest

import play.api.mvc.{Action, Controller}
import models.Blob
import play.api.Logger._
import utils._

import play.api.libs.json.{JsBoolean, JsNumber, Json}
import play.api.Logger

object Blobs extends Controller {

  implicit val blobWriter = Json.format[Blob]

  def create = Action {
    Blob.create() match {
      case Some(id) => Ok(Json.toJson(Json.obj(
        "id" -> JsNumber(id)
      )))
      case _ => InternalServerError("")
    }

  }

  def findAll = Action {
    Ok(Json.toJson(Blob.findAll()))
  }

  def findById(id: Long) = Action {
    Blob.findById(id).map(blob => {
      Ok(Json.toJson(blob))
    }).getOrElse(NotFound(""))
  }

  def update() = Action(parse.json) {
    request =>
      request.body.validate[Blob].map {
        Blob.save(_) match {
          case 1 => Ok("")
          case _ => NotFound("")
        }
      }.recoverTotal {
        e => BadRequest(e.toString)
      }
  }

  def deleteById(id: Long) = Action {
    Blob.deleteById(id) match {
      case 1 => Ok(Json.toJson("ok"))
      case _ => NotFound("")
    }
  }

}
