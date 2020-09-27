package sg.edu.ntu.serde

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import org.slf4j.{Logger, LoggerFactory}
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.sems.SMItem

object SmDBSerde {

  val logger: Logger = LoggerFactory.getLogger(getClass)

  def load(projectMD: ProjectMD): SMItem = {
    val smdbFile = Utils.getSMDBPath(projectMD)
    val smdbfName = smdbFile.toString()
    load(smdbfName)
  }

  def load(fpath: String): SMItem = {
    val ois = new ObjectInputStream(new FileInputStream(fpath))
    val smItem = ois.readObject().asInstanceOf[SMItem]
    ois.close()
    smItem
  }

  def write(smItem: SMItem): Unit = {
    val smdbFile = Utils.getSMDBPath(smItem.projectMD)
    val smdbfName = smdbFile.toString()
    write(smdbfName, smItem)
  }

  def write(fpath: String, smItem: SMItem): Unit = {
    val oos = new ObjectOutputStream(new FileOutputStream(fpath))
    oos.writeObject(smItem)
    oos.close()
  }

}
