package arbitration.application.commands.handlers

import arbitration.application.commands.commands.SpreadCommand
import arbitration.domain.models.SpreadResult

trait SpreadCommandHandler extends CommandHandler[SpreadCommand, SpreadResult]
