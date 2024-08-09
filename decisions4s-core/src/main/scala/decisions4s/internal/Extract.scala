package decisions4s.internal

trait Extract[F[_]] {
  extension [T](ft: F[T]) {
    def extract: T
  }
}

object Extract {
  given id: Extract[[T] =>> T] with {
    extension [T](ft: T) {
      def extract: T = ft
    }
  }

}
