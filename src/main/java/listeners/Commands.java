package listeners;

import audio.GuildMusicManager;
import audio.TrackScheduler;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import spotify.ClientAccess;
import utilities.Setup;

import java.awt.*;
import java.io.IOException;
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
                    System.out.println(JoinVoice.hashIndex);
                    TrackScheduler.nextTrack();

                    message.getTextChannel().sendMessage(":white_check_mark: **Skipping to next playlist...**").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
                    event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
                    break;
                case "skip":
                        TrackScheduler.nextTrack();
                        message.getTextChannel().sendMessage(":white_check_mark: **Skipping...**").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
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
                case "playlist":
                    if(args.length >= 2) {
                        joinVoice = new JoinVoice();
                        joinVoice.triggerSelectedPlayer(JoinVoice.emitter, Integer.parseInt(args[1]));
                        joinVoice.close(event.getGuild());

                        message.getTextChannel().sendMessage(":white_check_mark: **Skipping to playlist \"" + Setup.getPlaylist(Integer.parseInt(args[1])) + "\"...**").queue(message1 -> message1.delete().queueAfter(10, TimeUnit.SECONDS));
                        event.getMessage().delete().queueAfter(10, TimeUnit.SECONDS);
                    }else {
                        message.getTextChannel().sendMessage(":white_check_mark: **There are " + Setup.playlists.size() + " Playlists to choose from!**```\n" +
                                Setup.PLAYLISTS +
                                "\n```").queue();
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
                                        .addField("playlist", "Select a playlist", false)
                                .build()
                        ).queue();
                    break;
            }
        }
    }
}
