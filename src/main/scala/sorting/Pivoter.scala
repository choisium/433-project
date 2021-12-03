package sorting

import scala.io.Source
import scala.collection.immutable.SortedMap
import scala.annotation.tailrec


/*  This class computes pivots from the file in filepath.
    - filepath: The absolute path of file used to compute pivots.
                Assumed that the file in "$filepath$id" exists for id in 1~rangeNum
                and has same format as gensort result.
    - rangeNum: Expected range count. Usualy same as requiredWorkerNum in NetworkServer.
    - subRangeNum: Expected subrange count per one range.
                e.g. For a range ("A", "F"), subrange can be (("A", "C"), ("D", "F"))
    - keyLength: The prefix length of key used to create KeyMap. */
class Pivoter(filepath: String, rangeNum: Int, subRangeNum: Int, keyLength: Int) {
    require(rangeNum > 0)
    require(keyLength > 0)

    type Range = (String, String)

    val printableChars = (' ' to '~').toSeq

    def getKeyMap(): Map[String, Int] = {
        val sources = for {
            filename <- (1 to rangeNum).map(r => filepath+r)
        } yield Source.fromFile(filename)

        val keys = for {
            filename <- (1 to rangeNum).map(r => filepath+r)
            source <- sources
            line <- source.getLines
            if (!line.isEmpty)
        } yield line.take(10)

        sources.foreach(source => source.close)

        for {
            (key, keyList) <- keys.toSeq.groupBy(key => key.take(keyLength))
        } yield key -> keyList.length
    }

    def findPivot(keyMap: Map[String, Int]): Seq[String] = {        
        val keyNum = keyMap.values.sum
        val totalRangeNum = rangeNum * subRangeNum
        val limit = keyNum / totalRangeNum

        @tailrec
        def findPivotByAccumulateKeyCount(keyCounts: Seq[(String, Int)], lastKey: String, accum: Int, pivots: Seq[String]): Seq[String] = {
            if (accum >= limit) {
                findPivotByAccumulateKeyCount(keyCounts, lastKey, 0, pivots :+ lastKey)
            } else if (keyCounts.isEmpty || pivots.length == totalRangeNum - 1) {
                pivots
            } else {
                val (key, count) = keyCounts.head
                findPivotByAccumulateKeyCount(keyCounts.tail, key, accum + count, pivots)
            }
        }

        findPivotByAccumulateKeyCount(keyMap.toSeq.sorted, null, 0, Seq())
    }

    def getNextKey(str: String): String = {
        @tailrec
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

    def getRangeFromPivot(pivots: Seq[String]): Seq[Range] = {
        val startString = printableChars.head.toString * keyLength
        val endString = printableChars.last.toString * keyLength
        val extendedPivots = startString +: pivots :+ endString

        for (keyPair <- extendedPivots.sliding(2).toSeq) yield {
            if (keyPair(0) == startString) (startString, keyPair(1))
            else (getNextKey(keyPair(0)), keyPair(1))
        }
    }

    def run(): Seq[(Range, Seq[Range])] = {
        val keyMap = getKeyMap
        val pivots = findPivot(keyMap)
        val ranges = getRangeFromPivot(pivots)
        val subRanges = ranges.grouped(subRangeNum)

        (for (subRange <- subRanges) yield {
            /* (keyRange: Range, subRange: Seq[Range]) */
            ((subRange.head._1, subRange.last._2), subRange)
        }).toSeq
    }
}