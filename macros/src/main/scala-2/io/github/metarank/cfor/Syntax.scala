package io.github.metarank.cfor

import Syntax._

case class SyntaxUtil[C <: Context with Singleton](val c: C) {

  import c.universe._

  def freshTermName[C <: Context](c: C)(s: String) =
    c.universe.TermName(c.freshName(s))

  def name(s: String) = freshTermName(c)(s + "$")

  def names(bs: String*) = bs.toList.map(name)

  def isClean(es: c.Expr[_]*): Boolean =
    es.forall {
      _.tree match {
        case t @ Ident(_: TermName) if t.symbol.asTerm.isStable => true
        case Function(_, _)                                     => true
        case _                                                  => false
      }
    }
}

class InlineUtil[C <: Context with Singleton](val c: C) {
  import c.universe._

  def resetLocalAttrs[C <: Context](c: C)(t: c.Tree) =
    c.untypecheck(t)

  def termName[C <: Context](c: C)(s: String) =
    c.universe.TermName(s)
  def setOrig[C <: Context](c: C)(tt: c.universe.TypeTree, t: c.Tree) =
    c.universe.internal.setOriginal(tt, t)

  def inlineAndReset[T](tree: Tree): c.Expr[T] = {
    val inlined = inlineApplyRecursive(tree)
    c.Expr[T](resetLocalAttrs(c)(inlined))
  }

  def inlineApplyRecursive(tree: Tree): Tree = {
    val ApplyName = termName(c)("apply")

    class InlineSymbol(name: TermName, symbol: Symbol, value: Tree) extends Transformer {
      override def transform(tree: Tree): Tree = tree match {
        case tree: Ident if tree.symbol == symbol =>
          if (tree.name == name) {
            value
          } else {
            super.transform(tree)
          }

        case tt: TypeTree if tt.original != null =>
          // super.transform(TypeTree().setOriginal(transform(tt.original)))
          super.transform(setOrig(c)(TypeTree(), transform(tt.original)))
        case _ =>
          super.transform(tree)
      }
    }

    object InlineApply extends Transformer {
      def inlineSymbol(name: TermName, symbol: Symbol, body: Tree, arg: Tree): Tree =
        new InlineSymbol(name, symbol, arg).transform(body)

      override def transform(tree: Tree): Tree = tree match {
        case Apply(Select(Function(params, body), ApplyName), args) =>
          params.zip(args).foldLeft(body) { case (b, (param, arg)) =>
            inlineSymbol(param.name, param.symbol, b, arg)
          }

        case Apply(Function(params, body), args) =>
          params.zip(args).foldLeft(body) { case (b, (param, arg)) =>
            inlineSymbol(param.name, param.symbol, b, arg)
          }

        case _ =>
          super.transform(tree)
      }
    }

    InlineApply.transform(tree)
  }
}

object Syntax {
  type Context = scala.reflect.macros.whitebox.Context

  def cforArrayMacro[A](c: Context)(array: c.Expr[Array[A]])(body: c.Expr[A => Unit]): c.Expr[Unit] = {
    import c.universe._
    val util  = SyntaxUtil[c.type](c)
    val index = util.name("index")

    val tree = if (util.isClean(body)) {
      q"""
      var $index: Int = 0
      while ($index < $array.length) {
        $body($array($index))
        $index += 1
      }
      """
    } else {
      val bodyName = util.name("body")
      q"""
      val $bodyName: Int => Unit = $body
      var $index: Int = 0
      while ($index < $array.length) {
        $bodyName($array($index))
        $index += 1
      }
      """
    }
    new InlineUtil[c.type](c).inlineAndReset[Unit](tree)
  }

  def cforMacro[A](
      c: Context
  )(init: c.Expr[A])(test: c.Expr[A => Boolean], next: c.Expr[A => A])(body: c.Expr[A => Unit]): c.Expr[Unit] = {

    import c.universe._
    val util  = SyntaxUtil[c.type](c)
    val index = util.name("index")

    val tree = if (util.isClean(test, next, body)) {
      q"""
      var $index = $init
      while ($test($index)) {
        $body($index)
        $index = $next($index)
      }
      """
    } else {
      val testName = util.name("test")
      val nextName = util.name("next")
      val bodyName = util.name("body")

      q"""
      val $testName: Int => Boolean = $test
      val $nextName: Int => Int = $next
      val $bodyName: Int => Unit = $body
      var $index: Int = $init
      while ($testName($index)) {
        $bodyName($index)
        $index = $nextName($index)
      }
      """
    }
    new InlineUtil[c.type](c).inlineAndReset[Unit](tree)
  }

  def cforRangeMacro(c: Context)(r: c.Expr[Range])(body: c.Expr[Int => Unit]): c.Expr[Unit] = {

    import c.universe._
    val util = SyntaxUtil[c.type](c)

    // names always contains 5 entries
    val names = util.names("range", "index", "end", "limit", "step")
    val index = names(1)
    val end   = names(2)
    val limit = names(3)

    def isLiteral(t: Tree): Option[Int] = t match {
      case Literal(Constant(a)) =>
        a match {
          case n: Int => Some(n)
          case _      => None
        }
      case _ => None
    }

    def strideUpTo(fromExpr: Tree, toExpr: Tree, stride: Int): Tree =
      q"""
      var $index: Int = $fromExpr
      val $end: Int = $toExpr
      while ($index <= $end) {
        $body($index)
        $index += $stride
      }"""

    def strideUpUntil(fromExpr: Tree, untilExpr: Tree, stride: Int): Tree =
      q"""
      var $index: Int = $fromExpr
      val $limit: Int = $untilExpr
      while ($index < $limit) {
        $body($index)
        $index += $stride
      }"""

    def strideDownTo(fromExpr: Tree, toExpr: Tree, stride: Int): Tree =
      q"""
      var $index: Int = $fromExpr
      val $end: Int = $toExpr
      while ($index >= $end) {
        $body($index)
        $index -= $stride
      }"""

    def strideDownUntil(fromExpr: Tree, untilExpr: Tree, stride: Int): Tree =
      q"""
      var $index: Int = $fromExpr
      val $limit: Int = $untilExpr
      while ($index > $limit) {
        $body($index)
        $index -= $stride
      }"""

    val tree: Tree = r.tree match {

      case q"$predef.intWrapper($i).until($j)" =>
        strideUpUntil(i, j, 1)

      case q"$predef.intWrapper($i).to($j)" =>
        strideUpTo(i, j, 1)

      case r @ q"$predef.intWrapper($i).until($j).by($step)" =>
        isLiteral(step) match {
          case Some(k) if k > 0 => strideUpUntil(i, j, k)
          case Some(k) if k < 0 => strideDownUntil(i, j, -k)
          case Some(k) =>
            c.error(c.enclosingPosition, "zero stride")
            q"()"
          case None =>
            c.info(c.enclosingPosition, "non-literal stride", true)
            q"$r.foreach($body)"
        }

      case r @ q"$predef.intWrapper($i).to($j).by($step)" =>
        isLiteral(step) match {
          case Some(k) if k > 0 => strideUpTo(i, j, k)
          case Some(k) if k < 0 => strideDownTo(i, j, -k)
          case Some(k) =>
            c.error(c.enclosingPosition, "zero stride")
            q"()"
          case None =>
            c.info(c.enclosingPosition, "non-literal stride", true)
            q"$r.foreach($body)"
        }

      case r =>
        c.info(c.enclosingPosition, "non-literal range", true)
        q"$r.foreach($body)"
    }

    new InlineUtil[c.type](c).inlineAndReset[Unit](tree)
  }

  def cforRange2Macro(
      c: Context
  )(r1: c.Expr[Range], r2: c.Expr[Range])(body: c.Expr[(Int, Int) => Unit]): c.Expr[Unit] = {

    import c.universe._
    c.Expr[Unit](q"cforRange($r1)(i => cforRange($r2)(j => $body(i, j)))")
  }

}
