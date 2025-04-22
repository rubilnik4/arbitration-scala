package arbitration.infrastructure.repositories

import io.getquill.MappedEncoding

import java.sql.Timestamp
import java.time.Instant

object QuillMappings {
  implicit val encodeInstant: MappedEncoding[Instant, Timestamp] =
    MappedEncoding(Timestamp.from)

  implicit val decodeInstant: MappedEncoding[Timestamp, Instant] =
    MappedEncoding(_.toInstant())
}
