package decisions4s.sql

import decisions4s._
import decisions4s.exprs._
import doobie._
import doobie.implicits._

object SqlEvaluator {

  def toSql(expr: Expr[?]): Fragment = expr match {
    case Literal(v) => 
      v match {
        case s: String  => val str: String = s; fr0"$str"
        case i: Int     => val intVal: Int = i; fr0"$intVal"
        case l: Long    => val longVal: Long = l; fr0"$longVal"
        case d: Double  => val doubleVal: Double = d; fr0"$doubleVal"
        case f: Float   => val floatVal: Float = f; fr0"$floatVal"
        case b: Boolean => val boolVal: Boolean = b; fr0"$boolVal"
        case other      => throw new IllegalArgumentException(s"Unsupported literal value: $other")
      }

    case Variable(name, _) => Fragment.const0(name)

    case Plus(lhs, rhs)     => fr0"(${toSql(lhs)} + ${toSql(rhs)})"
    case Minus(lhs, rhs)    => fr0"(${toSql(lhs)} - ${toSql(rhs)})"
    case Multiply(lhs, rhs) => fr0"(${toSql(lhs)} * ${toSql(rhs)})"

    case Equal(lhs, rhs)            => fr0"(${toSql(lhs)} = ${toSql(rhs)})"
    case NotEqual(lhs, rhs)         => fr0"(${toSql(lhs)} <> ${toSql(rhs)})"
    case LessThan(lhs, rhs)         => fr0"(${toSql(lhs)} < ${toSql(rhs)})"
    case LessThanEqual(lhs, rhs)    => fr0"(${toSql(lhs)} <= ${toSql(rhs)})"
    case GreaterThan(lhs, rhs)      => fr0"(${toSql(lhs)} > ${toSql(rhs)})"
    case GreaterThanEqual(lhs, rhs) => fr0"(${toSql(lhs)} >= ${toSql(rhs)})"

    case And(lhs, rhs) => fr0"(${toSql(lhs)} AND ${toSql(rhs)})"
    case Or(lhs, rhs)  => fr0"(${toSql(lhs)} OR ${toSql(rhs)})"

    case In(lhs, test) => compileUnaryTest(test, toSql(lhs))

    case Projection(base, _, label) => fr0"${toSql(base)}.${Fragment.const0(label)}"
    
    case IsEmpty(base) => fr0"${toSql(base)} IS NULL" 

    case f: Function1[_, _] =>
      fr0"${Fragment.const0(f.name)}(${toSql(f.arg1)})"
    
    case f: Function2[_, _, _] =>
       fr0"${Fragment.const0(f.name)}(${toSql(f.arg1)}, ${toSql(f.arg2)})"

    case _ => throw new UnsupportedOperationException(s"Cannot compile expression to SQL: $expr")
  }

  def compileUnaryTest(test: UnaryTest[?], input: Fragment): Fragment = test match {
    case UnaryTest.Compare(sign, rhs) =>
      val op = sign match {
        case UnaryTest.Compare.Sign.<  => "<"
        case UnaryTest.Compare.Sign.<= => "<="
        case UnaryTest.Compare.Sign.>  => ">"
        case UnaryTest.Compare.Sign.>= => ">="
      }
      fr0"($input ${Fragment.const0(op)} ${toSql(rhs)})"

    case UnaryTest.EqualTo(expr) => fr0"($input = ${toSql(expr)})"
    
    case UnaryTest.OneOf(expr) =>
       expr match {
         case Literal(iter: Iterable[_]) => 
            if (iter.isEmpty) fr0"1=0" // false
            else {
               val list = iter.toList
               list.headOption match {
                   case Some(s: String) => 
                      import cats.data.NonEmptyList
                      Fragments.in(input, NonEmptyList.fromListUnsafe(list.asInstanceOf[List[String]]))
                   case Some(i: Int) => 
                      import cats.data.NonEmptyList
                      Fragments.in(input, NonEmptyList.fromListUnsafe(list.asInstanceOf[List[Int]]))
                   case _ =>  throw new IllegalArgumentException(s"Unsupported OneOf list type: $list")
               }
            }
         case _ => throw new UnsupportedOperationException("OneOf requires a Literal Iterable")
       }

    case UnaryTest.Not(inner) => fr0"NOT (${compileUnaryTest(inner, input)})"
    
    case UnaryTest.Or(parts) => 
       if (parts.isEmpty) fr0"1=0"
       else parts.map(compileUnaryTest(_, input)).reduceLeft { (a, b) => fr0"($a OR $b)" }

    case UnaryTest.CatchAll => fr0"1=1"
    
    case UnaryTest.Bool(expr) => toSql(expr) // Ignore input for pure boolean test?
    
    case _ => throw new UnsupportedOperationException(s"Cannot compile UnaryTest: $test")
  }
}
