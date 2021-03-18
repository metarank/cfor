package io.github.metarank

import scala.language.experimental.macros

package object cfor {
  def cfor[A](init: A)(test: A => Boolean, next: A => A)(body: A => Unit): Unit = macro Syntax.cforMacro[A]

  def cfor[A](array: Array[A])(body: A => Unit): Unit = macro Syntax.cforArrayMacro[A]

  def cfor(r: Range)(body: Int => Unit): Unit = macro Syntax.cforRangeMacro
}
