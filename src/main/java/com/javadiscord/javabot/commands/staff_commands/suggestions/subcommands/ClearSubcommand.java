package com.javadiscord.javabot.commands.staff_commands.suggestions.subcommands;

import com.javadiscord.javabot.Bot;
import com.javadiscord.javabot.commands.Responses;
import com.javadiscord.javabot.commands.SlashCommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

/**
 * Subcommand that lets staff members clear suggestions.
 */
public class ClearSubcommand implements SlashCommandHandler {

    @Override
    public ReplyAction handle(SlashCommandEvent event) {

        OptionMapping messageIdOption = event.getOption("message-id");
        if (messageIdOption == null) {
            return Responses.error(event, "Missing required arguments.");
        }

        String messageId = messageIdOption.getAsString();
        Message message;
        try {
            message = event.getChannel().retrieveMessageById(messageId).complete();
        } catch (IllegalArgumentException iae) {
            return Responses.error(event, iae.getMessage());
        }

        MessageEmbed embed = message.getEmbeds().get(0);
        message.clearReactions().queue();

        var e = new EmbedBuilder()
                .setColor(Bot.config.get(event.getGuild()).getSlashCommand().getDefaultColor())
                .setAuthor(embed.getAuthor().getName(), embed.getAuthor().getUrl(), embed.getAuthor().getIconUrl())
                .setDescription(embed.getDescription())
                .setTimestamp(embed.getTimestamp());

        var config = Bot.config.get(event.getGuild()).getEmote();
        message.editMessageEmbeds(e.build()).queue(m -> {
            m.addReaction(config.getUpvoteEmote()).queue();
            m.addReaction(config.getDownvoteEmote()).queue();
        }, error -> Responses.error(event, error.getMessage()).queue());

        return Responses.success(event, "Suggestion cleared",
                String.format("Successfully cleared suggestion with id `%s`", message.getId()));
    }
}
