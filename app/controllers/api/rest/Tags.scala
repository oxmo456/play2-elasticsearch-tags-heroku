package controllers.api.rest

import play.api.mvc.{Action, Controller}
import models.Tag
import play.api.libs.json.Json


object Tags extends Controller {

  def findAll = Action {
    Ok(Json.toJson(Tag.findAll()))
  }

}
