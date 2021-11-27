package io.grpc.examples.helloworld;

import scala.concurrent.ExecutionContext
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll}
import io.grpc.{Server, ServerBuilder, ManagedChannelBuilder}

import sorting.Pivoting

class PivotingTest extends AnyFunSuite {
  trait TestEnv {
    val filepath = "/Users/choisium/Development/433-project/src/test/resources/sample"
    val keyMap = Map("s" -> 1, "*" -> 2, "}" -> 2, "9" -> 1, "J" -> 1, "u" -> 2,
                     "&" -> 1, "A" -> 1, "5" -> 1, "G" -> 1, "Q" -> 1, "0" -> 1,
                     "K" -> 1, ">" -> 1, "o" -> 2, "m" -> 2, "`" -> 1, "," -> 1,
                     "v" -> 2, "g" -> 1, "B" -> 1, "k" -> 1, "~" -> 2)
    val rangeNum = 3
    val keyLength = 1
    val pivot = new Pivoting(filepath, rangeNum, keyLength)
  }

  test("pivot getKeyMap returns right map") {
    new TestEnv {
      val result = pivot.getKeyMap
      assert(result == keyMap)
    }
  }

  test("pivot findPivot returns correct pivots") {
    new TestEnv {
      val result = pivot.findPivot(keyMap)
      assert(result.length == rangeNum - 1)
      assert(result == Seq("B", "o"))
    }
  }

  test("pivot getRangeFromPivot returns correct range") {
    new TestEnv {
      val test = Seq("A", "k")
      val expected = Seq((" ", "A"), ("B", "k"), ("l", "~"))
      val result = pivot.getRangeFromPivot(test)
      assert(result == expected)
    }
  }

  test("pivot works fine for 30 lines of input") {
    new TestEnv {
      val result = pivot.run
      assert(result == Seq((" ", "B"), ("C", "o"), ("p", "~")))
      
      val keyNum = keyMap.values.sum
      val limit = keyNum / rangeNum
      
      for {
        (first, second) <- result
        if (second != "~")
      } {
        assert(keyMap.filter(key => key._1 >= first && key._1 <= second).values.sum >= limit)
      }
    }
  }

  trait TestEnv2 {
    val filepath = System.getProperty("user.dir") + "/src/test/resources/sample"
    val keyMap = Map("9S" -> 1, "Q)" -> 1, ">Y" -> 1, ",2" -> 1, "5H" -> 1, "my" -> 1,
                     "B]" -> 1, "`P" -> 1, "}[" -> 1, "K~" -> 1, "GL" -> 1, "~s" -> 1,
                     "*i" -> 1, "vi" -> 1, "v%" -> 1, "uI" -> 1, "&~" -> 1, "}d" -> 1,
                     "uF" -> 1, "o4" -> 1, "*}" -> 1, "~%" -> 1, "sw" -> 1, "0f" -> 1,
                     "mz" -> 1, "As" -> 1, "JP" -> 1, "o7" -> 1, "gj" -> 1, "kX" -> 1)
    val rangeNum = 4
    val keyLength = 2
    val pivot = new Pivoting(filepath, rangeNum, keyLength)
  }

  test("pivot for length 2 getKeyMap returns right map") {
    new TestEnv2 {
      val result = pivot.getKeyMap
      assert(result == keyMap)
    }
  }

  test("pivot for length 2 findPivot returns correct pivots") {
    new TestEnv2 {
      val result = pivot.findPivot(keyMap)
      assert(result.length == rangeNum - 1)
      assert(result == Seq("9S", "Q)", "o7"))
    }
  }

  test("pivot for length 2 getRangeFromPivot returns correct range") {
    new TestEnv2 {
      val test = Seq("9S", "Q)", "o7")
      val expected = Seq(("  ", "9S"), ("9T", "Q)"), ("Q*", "o7"), ("o8", "~~"))
      val result = pivot.getRangeFromPivot(test)
      assert(result == expected)
    }
  }

  test("pivot for length 2 works fine for 30 lines of input") {
    new TestEnv2 {
      val result = pivot.run
      assert(result == Seq(("  ", "9S"), ("9T", "Q)"), ("Q*", "o7"), ("o8", "~~")))
      
      val keyNum = keyMap.values.sum
      val limit = keyNum / rangeNum
      
      for {
        (first, second) <- result
        if (second != "~~")
      } {
        println(keyMap.filter(key => key._1 >= first && key._1 <= second).values.sum)
        assert(keyMap.filter(key => key._1 >= first && key._1 <= second).values.sum >= limit)
      }
    }
  }
}