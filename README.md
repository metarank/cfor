# cfor: java-style for and foreach loop in scala

[![CI Status](https://github.com/metarank/cfor/workflows/CI/badge.svg)](https://github.com/metarank/cfor/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.metarank/cfor/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/io.github.metarank/cfor)
[![License: Apache 2](https://img.shields.io/badge/License-Apache2-green.svg)](https://opensource.org/licenses/Apache-2.0)

This tiny project is a cfor macro inspired by the one 
originally implemented in [Typelevel spire](https://github.com/typelevel/spire/blob/master/macros/src/main/scala/spire/macros/Syntax.scala) 
library. Supports Scala 2.12 and 2.13, has zero dependencies and does not require spire.

## Example

An index loop:
```scala
import io.github.metarank.cfor._
def sum(values: Array[Int]) = {
  var result = 0
  cfor(0)(_ < values.length, _ + 1) { i => result += values(i) }
  result
}
```

A foreach loop:
```scala
import io.github.metarank.cfor._
def sum(values: Array[Int]) = {
  var result = 0
  cfor(values) { result += _ }
  result
}
```

Both of this `cfor` calls are expanded into a while loop with no boxing, no extra allocations and 
with the same performance as a pure java for loop.

## Installation

Available on maven central for scala 2.12 and 2.13
```scala
libraryDependencies += "io.github.metarank" %% "cfor" % "0.1"
```

## Purpose

In Java, there is a well-known for-loop:
```java
public int sum(int[] values) {
    int result = 0;
    for (int i=0; i<values.length; i++) {
        result += values[i];
    }
    return result;
}
```

It can be even simplified with a foreach loop:
```java
public int sum(int[] values) {
        int result = 0;
        for (int value: values) {
            result += value;
        }
        return result;
}
```

But the `for` construct in Scala is not the same thing as in Java and can do much more.
The most straigthforward solution will be to have something like this:
```scala
// foreach style
def sumForeach(values: Array[Int]) = {
  var sum = 0
  for { value <- values } { sum += value }
  sum
}

// scala collections style
def sum(values: Array[Int]) = values.sum

// old good while loop
def sumWhile(values: Array[Int]) = {
  var sum = 0
  var i = 0
  while(i < values.length) {
    sum += values(i)
    i += 1
  }
  sum
}
```

But nice syntax comes with a price: performance and memory allocations:
```
[info] Benchmark       length)  Mode  Cnt     Score     Error  Units
[info] scalaForSum        1000  avgt   10  1783.558 ±  47.969  ns/op
[info] scalaSum           1000  avgt   10  5143.344 ± 242.712  ns/op
[info] scalaWhileSum      1000  avgt   10   253.662 ±   2.407  ns/op
```

```
[info] Benchmark                               (length)  Mode  Cnt      Score      Error   Units
[info] scalaForSum:·gc.alloc.rate.norm            1000  avgt   10     32.001 ±    0.001    B/op
[info] scalaSum:·gc.alloc.rate.norm               1000  avgt   10  12352.002 ±    0.001    B/op
[info] scalaWhileSum:·gc.alloc.rate.norm          1000  avgt   10     ≈ 10⁻⁴               B/op

```

So if you write a performance-critical code, then you only can use while loops, which are much more verbose that Java for loops.

Here comes a `cfor`: a yet another syntax sugar macro which expands to a while loop:

```scala
def sum(values: Array[Int]) = {
  var result = 0
  cfor(values) { result += _ }
  result
}
```

It has zero extra allocations and exactly the same performance as a while/for loop.

## Performance

The [benchmark](notfound) is done on Scala 2.13.5, AdoptOpenJDK 11.0.10 x64. 
```
[info] Benchmark                                         (length)  Mode  Cnt      Score     Error   Units
[info] cforForeachSum                                        1000  avgt   30    246.620 ±   0.836   ns/op
[info] cforSum                                               1000  avgt   30    248.275 ±   1.001   ns/op
[info] scalaCollectionsSum                                   1000  avgt   30   5058.811 ± 139.946   ns/op
[info] scalaForeachSum                                       1000  avgt   30   1769.533 ±  42.385   ns/op
[info] scalaWhileSum                                         1000  avgt   30    247.857 ±   0.358   ns/op

[info] cforForeachSum:·gc.alloc.rate.norm                    1000  avgt   30     ≈ 10⁻³              B/op
[info] cforSum:·gc.alloc.rate.norm                           1000  avgt   30     ≈ 10⁻³              B/op
[info] scalaCollectionsSum:·gc.alloc.rate.norm               1000  avgt   30  11813.341 ±  35.878    B/op
[info] scalaForeachSum:·gc.alloc.rate.norm                   1000  avgt   30     32.003 ±   0.004    B/op
[info] scalaWhileSum:·gc.alloc.rate.norm                     1000  avgt   30     ≈ 10⁻³              B/op
```

## License

This project is released under the Apache 2.0 license, as specified in the LICENSE file.
