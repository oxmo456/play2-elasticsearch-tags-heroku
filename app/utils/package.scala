import play.api.libs.json._
import play.api.libs.json.JsSuccess

package object utils {

  import anorm._

  implicit object PkFormat extends Format[Pk[Long]] {
    def reads(json: JsValue): JsResult[Pk[Long]] = JsSuccess(
      json.asOpt[Long].map(id => Id(id)).getOrElse(NotAssigned)
    )

    def writes(id: Pk[Long]): JsValue = id.map(JsNumber(_)).getOrElse(JsNull)
  }


}
