package assignmentsender

import org.apache.log4j.Logger

trait Logging {

  private lazy val _logger = Logger.getLogger(getClass)

  protected def logger: Logger = _logger

}
