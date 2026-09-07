package io.github.metarank.cfor

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class CforScala2Test extends AnyFlatSpec with Matchers {
  it should "iterate over array with anonymous function" in {
    val l   = mutable.ListBuffer[Int]()
    val arr = Array(0, 1, 2, 3, 4)
    cfor(arr)(l.append)
    l.toList shouldBe List(0, 1, 2, 3, 4)
  }

  // the scala 2 macro inlines the loop body, so a closure captures the mutable
  // loop variable itself, exactly like a hand-written while loop does
  it should "capture value in closure" in {
    val b1 = collection.mutable.ArrayBuffer.empty[() => Int]
    cfor(0)(_ < 3, _ + 1) { x =>
      b1 += (() => x)
    }
    val b2 = collection.mutable.ArrayBuffer[() => Int]()
    var i  = 0
    while (i < 3) {
      b2 += (() => i)
      i += 1
    }
    b1.map(_.apply()).toList shouldBe b2.map(_.apply()).toList
  }
}
