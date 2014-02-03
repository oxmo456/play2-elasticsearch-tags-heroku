package services.elasticsearch

import org.elasticsearch.common.xcontent.XContentBuilder

trait Document {

  def index: String

  def indexType: String

  def id: String

  def source: XContentBuilder
}
