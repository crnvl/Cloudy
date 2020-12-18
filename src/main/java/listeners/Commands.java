package listeners;

import audio.GuildMusicManager;
import audio.TrackScheduler;
import core.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utilities.Setup;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class Commands extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        if(message.getContentRaw().startsWith(Setup.PREFIX)) {
            String[] args = message.getContentRaw().replace(Setup.PREFIX, "").split(" ");
            TrackScheduler trackScheduler;
            JoinVoice joinVoice;
            switch (args[0]) {
                case "next":
                    joinVoice = new JoinVoice();
                    joinVoice.close(event.getGuild());

                    trackScheduler = new TrackScheduler(GuildMusicManager.player);
                    trackScheduler.clear();
                    JoinVoice.hashIndex = JoinVoice.queueURLs.size();
                    JoinVoice.getRandom();
                    TrackScheduler.nextTrack();

                    message.getTextChannel().sendMessage(":white_check_mark: **Skipped to the next playlist!**").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
                    event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
                    break;
                case "skip":
                        trackScheduler = new TrackScheduler(GuildMusicManager.player);
                        trackScheduler.nextTrack();
                        message.getTextChannel().sendMessage(":white_check_mark: **Playing the next track!**").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
                    event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
                    break;
                case "restart":
                        joinVoice = new JoinVoice();
                        joinVoice.close(event.getGuild());
                        System.exit(0);
                    break;
                case "join":
                    if(event.getMember().getVoiceState().inVoiceChannel()) {
                        event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
                        message.getTextChannel().sendMessage(":white_check_mark: **Joined your voicechat!**").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
                        event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
                    }
                    break;
                case "help":
                        event.getTextChannel().sendMessage(
                                new EmbedBuilder()
                                .setColor(Color.WHITE)
                                .setTitle("My Commands")
                                .addField("next", "Selects a new playlist", false)
                                        .addField("skip", "Plays the next track in the queue", false)
                                        .addField("restart", "Restarts all Audiosystems", false)
                                        .addField("join", "Makes me join your current voicechannel", false)
                                .build()
                        ).queue();
                    break;
            }
        }
    }
}
