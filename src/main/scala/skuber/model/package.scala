package skuber.model

import java.net.URL
import java.util.Date
import scala.collection.immutable.HashMap

/*
 * Core representation of the Kubernetes V1 API model
 * @author David O'Riordan
 */
package object Model {
  
  // define standard empty values - defaults and json formatting use them
  val emptyS=""
  val emptyB=false
  def emptyL[T]=List[T]()
  
  sealed abstract class TypeMeta {
    def apiVersion: String = "v1"
    def kind: String 
  }
  
  type Timestamp=java.time.ZonedDateTime
  
  case class ObjectMeta(
    name: String = emptyS,
    generateName: String = emptyS,
    namespace: String = "default",
    uid: String = emptyS,
    selfLink: String = emptyS,
    resourceVersion: String = emptyS,
    creationTimestamp: Option[Timestamp] = None,
    deletionTimestamp: Option[Timestamp] = None,
    labels: Option[Map[String, String]] = None,
    annotations: Option[Map[String, String]] = None)
          
  abstract class ObjectResource extends TypeMeta {
    def metadata: ObjectMeta
    def name = metadata.name
  }

  case class ListMeta( 
      selfLink: String = "",
      resourceVersion: String = "")
      
  // marker trait for any Kubernetes object type that are also items of some Kubernetes list type 
  // e.g. a Pod can be an item in a PodList
  trait KListItem 
  
  trait KList[I <: KListItem] extends TypeMeta {
    def metadata : ListMeta
    def items : List[I]
  }
 
  implicit def toList[I <: KListItem](resource: KList[I]) : List[I] = resource.items  
   
  case class Pods(metadata: ListMeta = ListMeta(), items: List[Pod] = List()) extends KList[Pod] { def kind = "PodList" }
  case class Nodes(metadata: ListMeta=ListMeta(), items: List[Node] = List()) extends KList[Node] { def kind = "NodeList" }
  case class ReplicationControllers(metadata: ListMeta=ListMeta(), items: List[ReplicationController] = List()) 
    extends KList[ReplicationController] { def kind = "ReplicationControllerList" }
  
  type Finalizer=String
  type Phase=String
  
  case class LocalObjectReference(name: String)
  
  case class ObjectReference(
      kind: Option[String] = None,
      apiVersion: Option[String] = None,
      namespace: Option[String] = None,
      name: String = "",
      uid: Option[String] = None,
      resourceVersion: Option[String] = None,
      fieldPath: Option[String] = None)
      
  type NameablePort = Either[Int, String] // is either an int or an IANA name       

  sealed trait Handler
  case class ExecAction(command: List[String]) extends Handler
  case class HTTPGetAction(
      port: NameablePort, 
      host: Option[String] = None, 
      path: Option[String] = None, 
      schema: Option[String] = None) extends Handler
  case class TCPSocketAction(port: NameablePort) extends Handler
  
  case class Probe(action: Handler, initialDelaySeconds: Option[Int], timeoutSeconds: Option[Int])
  case class Lifecycle(postStart: Option[Handler], preStop: Option[Handler]) 

}