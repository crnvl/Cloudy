package listeners;



import audio.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Playlist;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.model_objects.specification.Track;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import core.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;
import spotify.ClientAccess;
import utilities.Setup;

import java.io.IOException;
import java.util.*;

public class JoinVoice extends ListenerAdapter {
    public static ReadyEvent emitter;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        emitter = event;

        Main.jda = event.getJDA();
        Setup.playlistInit();

        playMusic(event);
    }

    public void playMusic(@NotNull ReadyEvent event) {

        Random r = new Random();
        int y = r.nextInt(Setup.playlists.size());

        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(ClientAccess.clientCredentials.getAccessToken())
                .build();

        GetPlaylistRequest tracks = spotifyApi.getPlaylist(Setup.playlists.get(y)).build();
        Playlist fuckinghell = null;
        try {
            fuckinghell = tracks.execute();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SpotifyWebApiException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Paging<PlaylistTrack> pTracks = fuckinghell.getTracks();
        PlaylistTrack[] allTracks = pTracks.getItems();

        Collections.shuffle(Arrays.asList(allTracks));

        for (int i = 0; i < allTracks.length; i++) {
            GetTrackRequest trackRequest =  spotifyApi.getTrack(allTracks[i].getTrack().getId()).build();
            try {
                Track track = trackRequest.execute();

                loadAndPlay(
                        event.getJDA().getTextChannelById(Setup.TEXTCHANNELID),
                        "ytsearch: " + track.getArtists()[0].getName() + " " + track.getName()
                );
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SpotifyWebApiException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        };
    }

    private AudioPlayerManager playerManager;
    private Map<Long, GuildMusicManager> musicManagers;

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl) {

        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(channel.getGuild(), musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                play(channel.getGuild(), musicManager, firstTrack);
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });

    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track) {
        connectToFirstVoiceChannel(guild.getAudioManager());

        musicManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
                audioManager.openAudioConnection(audioManager.getGuild().getVoiceChannelById(Setup.VOICECHANNELID));
    }
}
