package net.javadiscord.javabot.data.config.guild;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.javadiscord.javabot.data.config.GuildConfigItem;

import java.util.List;

/**
 * Configuration for the guild's qotw system.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QOTWConfig extends GuildConfigItem {
	private long questionChannelId;
	private long submissionChannelId;
	private long submissionsForumChannelId;
	private long questionRoleId;
	private long qotwReviewRoleId;
	private long qotwChampionRoleId;
	private String submissionForumOngoingReviewTagName = "";

	public NewsChannel getQuestionChannel() {
		return this.getGuild().getNewsChannelById(this.questionChannelId);
	}

	public TextChannel getSubmissionChannel() {
		return this.getGuild().getTextChannelById(this.submissionChannelId);
	}

	public ForumChannel getSubmissionsForumChannel() {
		return this.getGuild().getForumChannelById(this.submissionsForumChannelId);
	}

	public Role getQOTWRole() {
		return this.getGuild().getRoleById(this.questionRoleId);
	}

	public Role getQOTWReviewRole() {
		return this.getGuild().getRoleById(this.qotwReviewRoleId);
	}

	public Role getQOTWChampionRole() {
		return this.getGuild().getRoleById(this.qotwChampionRoleId);
	}

	/**
	 * Gets a {@link ForumTag} based on the specified name.
	 *
	 * @return The specified {@link ForumTag} or null if there is no tag matching the specified name.
	 */
	public ForumTag getSubmissionsForumOngoingReviewTag() {
		ForumChannel forumChannel = getSubmissionsForumChannel();
		if (forumChannel == null) return null;
		List<ForumTag> tags = forumChannel.getAvailableTagsByName(this.submissionForumOngoingReviewTagName, true);
		return tags.stream().findFirst().orElse(null);
	}
}
