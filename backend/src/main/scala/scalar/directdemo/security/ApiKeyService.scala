package scalar.directdemo.security

import scalar.directdemo.infrastructure.Magnum.DbTx
import scalar.directdemo.logging.Logging
import scalar.directdemo.user.User
import scalar.directdemo.util.Strings.Id
import scalar.directdemo.util.{Clock, IdGenerator}

import java.time.temporal.ChronoUnit
import scala.concurrent.duration.Duration

class ApiKeyService(apiKeyModel: ApiKeyModel, idGenerator: IdGenerator, clock: Clock)
    extends Logging:
  def create(userId: Id[User], valid: Duration)(using DbTx): ApiKey =
    val id = idGenerator.nextId[ApiKey]()
    val now = clock.now()
    val validUntil = now.plus(valid.toMillis, ChronoUnit.MILLIS)
    val apiKey = ApiKey(id, userId, now, validUntil)
    logger.debug(s"Creating a new api key for user $userId, valid until: $validUntil")
    apiKeyModel.insert(apiKey)
    apiKey

  def invalidate(id: Id[ApiKey])(using DbTx): Unit =
    logger.debug(s"Invalidating api key $id")
    apiKeyModel.delete(id)

  def invalidateAllForUser(userId: Id[User])(using DbTx): Unit =
    logger.debug(s"Invalidating all api keys for user $userId")
    apiKeyModel.deleteAllForUser(userId)
