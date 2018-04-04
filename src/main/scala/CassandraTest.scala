import CassandraTest.keyspace
import com.datastax.driver.core.{ResultSet, _}
import com.google.common.base.CaseFormat
import com.outworkers.phantom.dsl._

import scala.collection.JavaConverters._
import scala.util.Try


object CassandraTest extends App {
  val connector = new Connector("localhost")
  val keyspace = "data_storage"


  private val tables = connector.connection map { s => Mapper.processTables(s)
  }


  tables
  connector.close()
}


object Mapper {

  import treehugger.forest._
  import definitions._
  import treehuggerDSL._

  def hug(entry: (String, Seq[Column])): String = {

    val name = RootClass.newClass(entry._1)
    val params = entry._2.map(i => PARAM(i.name, getType(i.columnType)): ValDef)
    val tree = (CASECLASSDEF(name).withParams(params): Tree)
    treeToString(tree)
  }

  def getType(rawType: String) = rawType match {
    case "text" => "java.lang.String"
    case "timestamp" => "java.sql.Timestamp"
    case s => CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, s)
  }

   def processTables(s: Session) = {
    val columns: ResultSet = s
      .execute(s"select table_name, column_name, type from system_schema.columns where keyspace_name = '$keyspace'")
    columns.all()
      .asScala
      .map(row => Column(row.getString(0), row.getString(1), row.getString(2)))
      .groupBy(_.table)
      .map(hug).foreach(println)
  }

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

