package io.github.metarank.cfor

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CforScala3Test extends AnyFlatSpec with Matchers {
  // the scala 3 macro binds the loop value per iteration, so a closure captures
  // the value of the current iteration rather than the mutable loop variable
  it should "capture value in closure" in {
    val b1 = collection.mutable.ArrayBuffer.empty[() => Int]
    cfor(0)(_ < 3, _ + 1) { x =>
      b1 += (() => x)
    }
    b1.map(_.apply()).toList shouldBe List(0, 1, 2)
  }
}
