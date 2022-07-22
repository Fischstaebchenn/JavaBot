package net.javadiscord.javabot.data.h2db.commands;

import com.dynxsty.dih4jda.interactions.commands.AutoCompletable;
import com.dynxsty.dih4jda.interactions.commands.SlashCommand;
import com.dynxsty.dih4jda.util.AutoCompleteUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.AutoCompleteQuery;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.javadiscord.javabot.Bot;
import net.javadiscord.javabot.data.h2db.MigrationUtils;
import net.javadiscord.javabot.util.ExceptionLogger;
import net.javadiscord.javabot.util.Responses;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * <h3>This class represents the /db-admin migrate command.</h3>
 * This subcommand is responsible for executing SQL migrations on the bot's
 * schema.
 * <p>
 * It uses the given name (adding .sql if it's not already there) to look
 * for a matching file in the /migrations/ resource directory. Once it's
 * found the file, it will split it up into a list of statements by the ';'
 * character, and then proceed to execute each statement.
 * </p>
 */
public class MigrateSubcommand extends SlashCommand.Subcommand implements AutoCompletable {
	/**
	 * The constructor of this class, which sets the corresponding {@link SubcommandData}.
	 */
	public MigrateSubcommand() {
		setSubcommandData(new SubcommandData("migrate", "(ADMIN ONLY) Run a single database migration")
				.addOption(OptionType.STRING, "name", "The migration's filename", true, true));
		requireUsers(Bot.config.getSystems().getAdminConfig().getAdminUsers());
		requirePermissions(Permission.MANAGE_SERVER);
	}

	/**
	 * Replies with all available migrations to run.
	 *
	 * @param event The {@link CommandAutoCompleteInteractionEvent} that was fired.
	 * @return A {@link List} with all Option Choices.
	 */
	public static @NotNull List<Command.Choice> replyMigrations(CommandAutoCompleteInteractionEvent event) {
		List<Command.Choice> choices = new ArrayList<>(25);
		try (Stream<Path> s = Files.list(MigrationUtils.getMigrationsDirectory())) {
			List<Path> paths = s.filter(path -> path.getFileName().toString().endsWith(".sql")).toList();
			paths.forEach(path -> choices.add(new Command.Choice(path.getFileName().toString(), path.getFileName().toString())));
		} catch (IOException | URISyntaxException e) {
			ExceptionLogger.capture(e, MigrateSubcommand.class.getSimpleName());
		}
		return choices;
	}

	@Override
	public void execute(@NotNull SlashCommandInteractionEvent event) {
		String migrationName = Objects.requireNonNull(event.getOption("name")).getAsString();
		if (!migrationName.endsWith(".sql")) {
			migrationName = migrationName + ".sql";
		}
		try {
			Path migrationsDir = MigrationUtils.getMigrationsDirectory();
			Path migrationFile = migrationsDir.resolve(migrationName);
			if (Files.notExists(migrationFile)) {
				Responses.error(event, "The specified migration `" + migrationName + "` does not exist.").queue();
				return;
			}
			String sql = Files.readString(migrationFile);
			String[] statements = sql.split("\\s*;\\s*");
			if (statements.length == 0) {
				Responses.error(event, "The migration `" + migrationName + "` does not contain any statements. Please remove or edit it before running again.").queue();
				return;
			}
			Bot.asyncPool.submit(() -> {
				try (Connection con = Bot.dataSource.getConnection()) {
					for (int i = 0; i < statements.length; i++) {
						if (statements[i].isBlank()) {
							event.getChannel().sendMessage("Skipping statement " + (i + 1) + "; it is blank.").queue();
							continue;
						}
						try (Statement stmt = con.createStatement()) {
							int rowsUpdated = stmt.executeUpdate(statements[i]);
							event.getChannel().sendMessageFormat(
									"Executed statement %d of %d:\n```sql\n%s\n```\nRows Updated: `%d`", i + 1, statements.length, statements[i], rowsUpdated
							).queue();
						} catch (SQLException e) {
							ExceptionLogger.capture(e, getClass().getSimpleName());
							event.getChannel().sendMessage("Error while executing statement " + (i + 1) + ": " + e.getMessage()).queue();
							return;
						}
					}
				} catch (SQLException e) {
					ExceptionLogger.capture(e, getClass().getSimpleName());
					event.getChannel().sendMessage("Could not obtain a connection to the database.").queue();
				}
			});
			Responses.info(event, "Migration Started",
					"Execution of the migration `" + migrationName + "` has been started. " + statements.length + " statements will be executed.").queue();
		} catch (IOException | URISyntaxException e) {
			ExceptionLogger.capture(e, getClass().getSimpleName());
			Responses.error(event, e.getMessage()).queue();
		}
	}

	@Override
	public void handleAutoComplete(@NotNull CommandAutoCompleteInteractionEvent event, @NotNull AutoCompleteQuery target) {
		event.replyChoices(AutoCompleteUtils.handleChoices(event, MigrateSubcommand::replyMigrations)).queue();
	}
}
