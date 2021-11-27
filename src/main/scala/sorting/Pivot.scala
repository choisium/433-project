package sorting

import scala.io.Source
import scala.collection.immutable.SortedMap

class Pivoting(filepath: String, rangeNum: Int, keyLength: Int) {
    require(rangeNum > 0)
    require(keyLength > 0)

    val printableChars = (' ' to '~').toSeq

    def getKeyMap(): Map[String, Int] = {
        val keys = for {
            line <- Source.fromFile(filepath).getLines
            if (!line.isEmpty)
        } yield line.take(10)

        for {
            (key, keyList) <- keys.toSeq.groupBy(key => key.take(keyLength))
        } yield key -> keyList.length
    }

    def findPivot(keyMap: Map[String, Int]): Seq[String] = {        
        val keyNum = keyMap.values.sum
        val limit = keyNum / rangeNum

        def findPivotByAccumulateKeyCount(keyCounts: Seq[(String, Int)], lastKey: String, accum: Int, pivots: Seq[String]): Seq[String] = {
            if (accum >= limit) {
                findPivotByAccumulateKeyCount(keyCounts, lastKey, 0, pivots :+ lastKey)
            } else if (keyCounts.isEmpty || pivots.length == rangeNum - 1) {
                pivots
            } else {
                val (key, count) = keyCounts.head
                findPivotByAccumulateKeyCount(keyCounts.tail, key, accum + count, pivots)
            }
        }

        findPivotByAccumulateKeyCount(keyMap.toSeq.sorted, null, 0, Seq())
    }

    def getNextKey(str: String): String = {
        def increaseKey(front: String, curChar: Char, back: String): String = {
            assert(front.length > 0 || (curChar != printableChars.last))
            if (curChar == printableChars.last) {
                increaseKey(front.init, front.last, printableChars.head +: back)
            } else {
                val charIndex = printableChars.indexOf(curChar)
                front + printableChars(charIndex + 1) + back
            }
        }
        increaseKey(str.init, str.last, "")
    }

    def getRangeFromPivot(pivots: Seq[String]): Seq[(String, String)] = {
        val startString = printableChars.head.toString * keyLength
        val endString = printableChars.last.toString * keyLength
        val extendedPivots = startString +: pivots :+ endString

        for (keyPair <- extendedPivots.sliding(2).toSeq) yield {
            if (keyPair(0) == startString) (startString, keyPair(1))
            else (getNextKey(keyPair(0)), keyPair(1))
        }
    }

    def run(): Seq[(String, String)] = {
        val keyMap = getKeyMap
        val pivots = findPivot(keyMap)
        
        getRangeFromPivot(pivots)
    }
}