import com.datastax.driver.core.{ResultSet, _}
import com.outworkers.phantom.dsl._
import treehugger.forest._
import definitions._
import treehugger.forest
import treehuggerDSL._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.Try


object CassandraTest extends App {
  val connector = new Connector("localhost")

  def hug(entry: (String, mutable.Buffer[Column])) : String = {
    val name = RootClass.newClass(entry._1)
    val params = entry._2.map(i => PARAM(i.name, IntClass): ValDef)
    val tree = (CASECLASSDEF(name).withParams(params): Tree)
    treeToString(tree)
  }


  private val tables = connector.connection map { successful =>
    val rs: ResultSet = successful
      .execute("select table_name, column_name, type from system_schema.columns where keyspace_name = 'data_storage'")
    rs.all()
  } map {
    _.asScala
      .map(row => Column(row.getString(0), row.getString(1), row.getString(2)))
      .groupBy(_.table)
        .map{entry => hug(entry)}.foreach(println)
  }
  tables
  connector.close()
}

case class Column(table: String, name: String, columnType: String)

class Connector(url: String) {
  private lazy val cluster = Cluster.builder()
    .addContactPoint(url)
    .build()

  private def establishConnection(cluster: Cluster): Try[Session] = Try(cluster.connect())

  def close(): Unit = cluster.close()

  lazy val connection: Try[Session] = establishConnection(cluster)
}

