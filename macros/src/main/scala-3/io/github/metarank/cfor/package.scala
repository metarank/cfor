package io.github.metarank.cfor

inline def cfor[A](inline init: A)(inline test: A => Boolean, inline next: A => A)(inline body: A => Any): Unit =
  var a = init
  while test(a) do
    body(a)
    a = next(a)

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
