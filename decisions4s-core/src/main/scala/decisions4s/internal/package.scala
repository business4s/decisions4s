package decisions4s

package object internal {

  type Const[T] = [t] =>> T

  type ~>[A[_], B[_]] = [t] => A[t] => B[t]

  type Tuple2K[A[_], B[_]] = [t] =>> (A[t], B[t])

}
