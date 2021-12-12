package sorting

import scala.concurrent.ExecutionContext
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import io.grpc.{Server, ServerBuilder, ManagedChannelBuilder}

import sorting.Pivoter

class PivotingTest extends AnyFunSuite {
  trait TestEnv {
    val filepath = System.getProperty("user.dir") + "/src/test/resources/pivoting/sample"
    val rangeNum = 3
    val subRangeNum = 1
    val keyLength = 1
    val pivot = new Pivoter(filepath, rangeNum, subRangeNum, keyLength)

    val expectedKeyMap = Map("*" -> 1, "N" -> 1, "j" -> 1, "u" -> 2, "!" -> 1, "A" -> 1,
                    "5" -> 1, "," -> 1, "G" -> 1, "l" -> 1, "0" -> 1, "7" -> 1,
                    "D" -> 1, "Y" -> 1, "J" -> 1, "m" -> 3, "|" -> 1, "`" -> 1,
                    "-" -> 1, "@" -> 1, "Q" -> 1, "'" -> 1, "~" -> 1, "O" -> 1,
                    "^" -> 1, "Z" -> 1, "o" -> 1)
    val expectedPivot = Seq("A", "`")
    val expectedRange = Seq((" ", "A"), ("B", "`"), ("a", "~"))
    val expectedResult = Seq(((" ", "A"), Seq((" ", "A"))),
                            (("B", "`"), Seq(("B", "`"))),
                            (("a", "~"), Seq(("a", "~"))))
  }

  test("pivot getKeyMap returns right map") {
    new TestEnv {
      val result = pivot.getKeyMap
      assert(result == expectedKeyMap)
    }
  }

  test("pivot findPivot returns correct pivots") {
    new TestEnv {
      val result = pivot.findPivot(expectedKeyMap)
      assert(result.length == rangeNum * subRangeNum - 1)
      assert(result == expectedPivot)
    }
  }

  test("pivot getRangeFromPivot returns correct range") {
    new TestEnv {
      val result = pivot.getRangeFromPivot(expectedPivot)
      assert(result.length == rangeNum * subRangeNum)
      assert(result == expectedRange)
    }
  }

  test("pivot works fine for 30 lines of input") {
    new TestEnv {
      val result = pivot.run

      assert(result == expectedResult)
      assert(result.length == rangeNum)

      for {
        (range, subRange) <- result
      } {
        assert(subRange.length == subRangeNum)
        assert(range._1 == subRange.head._1)
        assert(range._2 == subRange.last._2)
      }
    }
  }

  trait TestEnv2 {
    val filepath = System.getProperty("user.dir") + "/src/test/resources/pivoting/sample"
    val rangeNum = 4
    val subRangeNum = 2
    val keyLength = 2
    val pivot = new Pivoter(filepath, rangeNum, subRangeNum, keyLength)

    val expectedKeyMap = Map("`q" -> 1, "Q)" -> 1, "Ie" -> 1, "~s" -> 1, "GC" -> 1, "8;" -> 1,
                             "\"," -> 1, "l@" -> 1, "N(" -> 1, "mv" -> 1, "!}" -> 1, "\\." -> 1,
                             "YJ" -> 1, "+d" -> 1, "^3" -> 1, "$^" -> 1, "}8" -> 1, "jz" -> 1,
                             "D@" -> 1, "Of" -> 1, "5H" -> 1, "76" -> 1, "|x" -> 1, "uI" -> 1,
                             "Lv" -> 1, "u3" -> 1, "my" -> 1, ",n" -> 1, "Z-" -> 1, "o4" -> 1,
                             "JU" -> 1, "*}" -> 1, "0f" -> 1, "mz" -> 1, "p," -> 1, "As" -> 1,
                             "`E" -> 1, "@O" -> 1, "'z" -> 1, "-_" -> 1)
    val expectedPivot = Seq("*}", "5H", "D@", "N(", "\\.", "l@", "p,")
    val expectedRange = Seq(("  ", "*}"), ("*~", "5H"), ("5I","D@"), ("DA", "N("), ("N)", "\\."),
                            ("\\/", "l@"), ("lA", "p,"), ("p-", "~~"))
    val expectedResult = Seq((("  ", "5H"), Seq(("  ", "*}"), ("*~", "5H"))),
                            (("5I", "N("), Seq(("5I", "D@"), ("DA", "N("))),
                            (("N)", "l@"), Seq(("N)", "\\."), ("\\/", "l@"))),
                            (("lA", "~~"), Seq(("lA", "p,"), ("p-", "~~"))))
  }

  test("pivot for length 2 getKeyMap returns right map") {
    new TestEnv2 {
      val result = pivot.getKeyMap
      assert(result == expectedKeyMap)
    }
  }

  test("pivot for length 2 findPivot returns correct pivots") {
    new TestEnv2 {
      val result = pivot.findPivot(expectedKeyMap)
      assert(result.length == rangeNum * subRangeNum - 1)
      assert(result == expectedPivot)
    }
  }

  test("pivot for length 2 getRangeFromPivot returns correct range") {
    new TestEnv2 {
      val result = pivot.getRangeFromPivot(expectedPivot)
      assert(result == expectedRange)
      assert(result.length == rangeNum * subRangeNum)
    }
  }

  test("pivot for length 2 works fine for 30 lines of input") {
    new TestEnv2 {
      val result = pivot.run

      assert(result == expectedResult)
      assert(result.length == rangeNum)

      for {
        (range, subRange) <- result
      } {
        assert(subRange.length == subRangeNum)
        assert(range._1 == subRange.head._1)
        assert(range._2 == subRange.last._2)
      }
    }
  }
}