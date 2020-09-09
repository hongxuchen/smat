package sg.edu.ntu

import org.slf4j.{Logger, LoggerFactory}

package object smsem {

  val logger: Logger = LoggerFactory.getLogger(this.getClass)

  def optStr[A](optV: Option[A], defaultStr: String = "NA"): String = {
    optV match {
      case Some(v) => v.toString
      case None => defaultStr
    }
  }

  /**
    * abstract class to provide semantics for semantic matching
    */
  abstract class SMSem() {
    def dumpAll(): Unit
  }

}
