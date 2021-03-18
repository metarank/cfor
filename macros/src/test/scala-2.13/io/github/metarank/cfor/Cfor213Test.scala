package io.github.metarank.cfor

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class Cfor213Test extends AnyFlatSpec with Matchers {
  it should "iterate over array with anonymous function" in {
    val l   = mutable.ListBuffer[Int]()
    val arr = Array(0, 1, 2, 3, 4)
    cfor(arr)(l.append)
    l.toList shouldBe List(0, 1, 2, 3, 4)
  }
}
