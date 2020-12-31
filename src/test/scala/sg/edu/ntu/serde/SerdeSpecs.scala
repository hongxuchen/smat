package sg.edu.ntu.serde

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sg.edu.ntu.ModuleMD
import sg.edu.ntu.sems.SMItem

import scala.collection.mutable.ListBuffer

class SerdeSpecs extends AnyFlatSpec with Matchers {

  "loaded SMItem" should "be the same as serialized one" in {

    val smItem = new SMItem(ModuleMD("hw-1.0"), ListBuffer.empty)

    SmDBSerde.write(smItem)
    val loadedItem = SmDBSerde.load(smItem.moduleMD)

    loadedItem shouldBe smItem

  }

}
