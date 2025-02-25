package decisions4s.internal

case class Meta[T](index: Int, name: String, value: Option[T])

object Meta {
  given Extract[Meta] with {
    extension [T](ft: Meta[T]) {
      // unsafe but works for the current usages (probably by accident)
      def extract: T = ft.value.get
    }
  }
  given Functor[Meta] with {
    extension [T](ft: Meta[T]) {
      def map[T1](f: T => T1): Meta[T1] = ft.copy(value = ft.value.map(f))
    }
  }
}
