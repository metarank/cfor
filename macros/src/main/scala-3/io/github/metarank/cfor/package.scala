package io.github.metarank.cfor

import scala.PartialFunction.cond

//inline def cfor[A](inline init: A)(inline test: A => Boolean, inline next: A => A)(inline body: A => Any): Unit =
//  var a = init
//  while test(a) do
//    body(a)
//    a = next(a)

inline def cfor[A](inline init: A)(inline test: A => Boolean, inline next: A => A)(inline body: A => Unit): Unit =
  ${ cforImpl('init, 'test, 'next, 'body) }

inline def cfor[A](inline array: Array[A])(inline body: A => Unit): Unit =
  var i = 0
  while i < array.length do
    body(array(i))
    i += 1

inline def cfor(inline r: Range)(inline body: Int => Unit): Unit =
  val end = r match
    case _: Range.Inclusive => if r.step > 0 then r.end + 1 else r.end - 1
    case _: Range.Exclusive => r.end

  var i = r.start
  if r.step > 0 then
    while i < end do
      body(i)
      i += r.step
  else
    while i > end do
      body(i)
      i += r.step


//--------------------------------------------------------------------------
//
// Code from below are fragments of file
// `core/src/main/scala-3/spire/syntax/macros/cforMacros.scala`
// Original copyright and license noted
// OUt out 133 lines 37 are uised here.
//

/*
 * **********************************************************************\
 * * Project                                                              **
 * *       ______  ______   __    ______    ____                          **
 * *      / ____/ / __  /  / /   / __  /   / __/     (c) 2011-2021        **
 * *     / /__   / /_/ /  / /   / /_/ /   / /_                            **
 * *    /___  / / ____/  / /   / __  /   / __/   Erik Osheim, Tom Switzer **
 * *   ____/ / / /      / /   / / | |   / /__                             **
 * *  /_____/ /_/      /_/   /_/  |_|  /____/     All rights reserved.    **
 * *                                                                      **
 * *      Redistribution and use permitted under the MIT license.         **
 * *                                                                      **
 * \***********************************************************************
 */
import scala.quoted.*
def cforImpl[R: Type](init: Expr[R], test: Expr[R => Boolean], next: Expr[R => R], body: Expr[R => Unit])(using
                                                                                                          Quotes
): Expr[Unit] =
  import quotes.reflect.*

  def code(testRef: Expr[R => Boolean], nextRef: Expr[R => R], bodyRef: Expr[R => Unit]): Expr[Unit] = '{
    var index = $init
    while $testRef(index) do
      $bodyRef(index)
      index = $nextRef(index)
  }

  letFunc("test", test)(t => letFunc("next", next)(n => letFunc("body", body)(b => code(t, n, b))))
end cforImpl

/**
 * Equivalent to `'{ val name: A => B = $rhs; ${in('name)} }`, except when `rhs` is a function literal, then equivalent
 * to `in(rhs)`.
 *
 * This allows inlined function arguments to perform side-effects only once before their first evaluation, while still
 * avoiding the creation of closures for function literal arguments.
 */
private def letFunc[A, B, C](using Quotes)(name: String, rhs: Expr[A => B])(in: Expr[A => B] => Expr[C]): Expr[C] =
  import quotes.reflect.*

  extension (t: Term) def unsafeAsExpr[A] = t.asExpr.asInstanceOf[Expr[A]] // cast without `quoted.Type[A]`

  def isFunctionLiteral[A, B](f: Expr[A => B]): Boolean = cond(f.asTerm.underlyingArgument) { case Lambda(_, _) =>
    true
  }

  def let[A, B](name: String, rhs: Expr[A])(in: Expr[A] => Expr[B])(using Quotes): Expr[B] =
  // Equivalent to `'{ val name = $rhs; ${in('name)} }`
    ValDef.let(Symbol.spliceOwner, name, rhs.asTerm)(ref => in(ref.unsafeAsExpr[A]).asTerm).unsafeAsExpr[B]

  if isFunctionLiteral(rhs) then in(Expr.betaReduce(rhs))
  else let(name, rhs)(in)