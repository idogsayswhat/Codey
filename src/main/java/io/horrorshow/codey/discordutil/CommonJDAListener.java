package io.horrorshow.codey.discordutil;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
@Slf4j
public class CommonJDAListener extends ListenerAdapter {

    public static final String BASKET = "\uD83D\uDDD1️";

    private final ApplicationState applicationState;


    public CommonJDAListener(JDA jda, ApplicationState applicationState) {
        jda.addEventListener(this);

        this.applicationState = applicationState;
    }


    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            CompletableFuture.runAsync(() -> onReactionAdd(event));
        }
    }


    public void onReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        var message = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
        if (DiscordUtils.hasEmoji(BASKET, event)) {
            if (canDeleteMessage(event.getJDA().getSelfUser(), message)) {
                try {
                    message.delete().complete();
                } catch (ErrorResponseException e) {
                    log.warn("Unable to remove message");
                }
            }
        }
    }


    private boolean canDeleteMessage(@NotNull User user, Message message) {
        try {
            return user.getId().equals(message.getAuthor().getId())
                    && !applicationState.getGithubEventState().contains(message.getChannel().getId()).get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }
}
