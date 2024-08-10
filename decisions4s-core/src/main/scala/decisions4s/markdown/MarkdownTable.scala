package decisions4s.markdown

case class MarkdownTable(headers: IndexedSeq[String], values: IndexedSeq[IndexedSeq[String]]) {
  def render: String = {
    val allRows                   = Seq(headers) ++ values
    val numOfColumns              = headers.size.max(values.map(_.size).maxOption.getOrElse(0))
    val columns                   = 0.until(numOfColumns)
    val maxWidth: IndexedSeq[Int] = columns.map(idx => allRows.map(_.lift(idx).map(_.length).getOrElse(0)).max)

    val headersRow   = columns.map(idx => headers.lift(idx).getOrElse("").center(maxWidth(idx)))
    val separatorRow = columns.map(idx => "-" * maxWidth(idx))
    val valueRows    = values.map(row => columns.map(idx => row.lift(idx).getOrElse("").center(maxWidth(idx))))

    (List(headersRow, separatorRow) ++ valueRows)
      .map(_.mkString("|", "|", "|"))
      .mkString("\n")
  }

  extension (s: String) {
    // centering is ignored when Markdown is rendered but can be considered useful if viewed as raw text
    private def center(width: Int): String = {
      if (s.length < width) {
        val remaining = width - s.length
        (s.reverse + (" " * (remaining / 2))).reverse.padTo(width, ' ')
      } else s
    }
  }
}
