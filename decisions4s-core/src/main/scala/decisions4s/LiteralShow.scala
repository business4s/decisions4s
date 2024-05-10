package decisions4s

/** Typeclass controlling how the given type should render when using as a literal in rules definition
  */
trait LiteralShow[-T] {
  def show(v: T): String
}
