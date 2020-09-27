package sg.edu.ntu.serde

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sg.edu.ntu.ProjectMD
import sg.edu.ntu.sems.SMItem

import scala.collection.mutable.ListBuffer

class SerdeSpecs extends AnyFlatSpec with Matchers {

  "loaded SMItem" should "be the same as serialized one" in {

    val smItem = new SMItem(ProjectMD("hw-1.0"), ListBuffer.empty)

    SmDBSerde.write(smItem)
    val loadedItem = SmDBSerde.load(smItem.projectMD)

    loadedItem shouldBe smItem

  }

}
