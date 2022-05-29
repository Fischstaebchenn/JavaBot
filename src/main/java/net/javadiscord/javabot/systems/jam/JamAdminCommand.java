package net.javadiscord.javabot.systems.jam;

import com.dynxsty.dih4jda.interactions.commands.SlashCommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.javadiscord.javabot.systems.jam.subcommands.admin.*;

/**
 * Handler class for all jam-admin commands.
 */
public class JamAdminCommand extends SlashCommand {

	public JamAdminCommand() {
		setCommandData(Commands.slash("jam-admin", "Administrator actions for configuring the Java Jam.")
				// TODO: Implement App Permissions V2 once JDA releases them
				.setDefaultEnabled(false));
		setSubcommands(new PlanNewJamSubcommand(), new EditJamSubcommand(), new NextPhaseSubcommand(),
				new AddThemeSubcommand(), new ListThemesSubcommand(), new RemoveThemeSubcommand(),
				new ListSubmissionsSubcommand(), new RemoveSubmissionsSubcommand(), new CancelSubcommand());
	}
}
