package controllers.api.rest

import play.api.mvc.{Action, Controller}
import models.Blob
import utils._

import play.api.libs.json.{JsNumber, Json}

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
        blob => {
          Blob.save(blob)
          Ok("")
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
