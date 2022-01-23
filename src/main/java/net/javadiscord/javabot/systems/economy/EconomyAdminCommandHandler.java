package net.javadiscord.javabot.systems.economy;

import net.javadiscord.javabot.command.DelegatingCommandHandler;
import net.javadiscord.javabot.systems.economy.subcommands.admin.GiveSubcommand;

import java.util.Map;

/**
 * Handler class for all economy admin commands.
 */
public class EconomyAdminCommandHandler extends DelegatingCommandHandler {
	/**
	 * Adds all subcommands {@link DelegatingCommandHandler#addSubcommand}.
	 */
	public EconomyAdminCommandHandler() {
		super(Map.of(
				"give", new GiveSubcommand()
		));
	}
}
