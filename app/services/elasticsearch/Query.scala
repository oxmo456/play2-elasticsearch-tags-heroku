package services.elasticsearch

import org.elasticsearch.action.search.SearchType
import org.elasticsearch.index.query.QueryBuilder

trait Query {

  def indices: List[String]

  def indicesTypes: List[String]

  def searchType: SearchType

  def content: QueryBuilder
}
