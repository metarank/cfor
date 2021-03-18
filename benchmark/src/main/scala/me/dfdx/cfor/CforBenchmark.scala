package me.dfdx.cfor

import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit
import scala.util.Random
import me.dfdx.cfor._

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(value = 1)
class CforBenchmark {
  @Param(Array("10", "1000", "1000000"))
  var length: Int = _

  var array: Array[Int] = _

  @Setup
  def setup = {
    array = new Array[Int](length)
    var i = 0
    while (i < length) {
      array(i) = if (Random.nextBoolean()) 1 else 0
      i += 1
    }
  }

  @Benchmark
  def measureArraySum() = {
    var sum = 0
    var i   = 0
    while (i < array.length) {
      sum += array(i)
      i += 1
    }
    sum
  }

  @Benchmark
  def measureCforIndexSum() = {
    var sum = 0
    cfor(0)(_ < array.length, _ + 1) { i => sum += array(i) }
    sum
  }

  @Benchmark
  def measureCforArraySum() = {
    var sum = 0
    cfor(array) { i => sum += i }
    sum
  }
}
