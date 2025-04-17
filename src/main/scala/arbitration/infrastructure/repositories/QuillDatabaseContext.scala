package arbitration.infrastructure.repositories

import io.getquill.context.qzio.ZioJdbcContext
import io.getquill.{PostgresDialect, PostgresJdbcContext, SnakeCase}
import io.getquill.jdbczio.Quill
import zio.ZIO

import javax.sql.DataSource

type QuillDatabaseContext = ZioJdbcContext[PostgresDialect, SnakeCase]