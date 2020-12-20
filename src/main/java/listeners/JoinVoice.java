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
    private AudioPlayerManager playerManager;
    private Map<Long, GuildMusicManager> musicManagers;
    public static int y = 0;
    public static int hashIndex;
    public static HashMap<Integer, String> queueURLs = new HashMap<>();
    public static HashMap<Integer, String> queueInfo = new HashMap<>();

    public static void getRandom() {
        Random r = new Random();
        y = r.nextInt(Setup.playlists.size());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        emitter = event;
        Main.jda = event.getJDA();
        triggerPlayer(event);
    }

    public void triggerPlayer(@NotNull ReadyEvent event) {
        getRandom();
        playMusic(event);
        loadAndPlay(event.getJDA().getTextChannelById(Setup.TEXTCHANNELID), queueURLs.get(hashIndex));
    }

    public void triggerSelectedPlayer(@NotNull ReadyEvent event, int playlist) {
        playSelectedMusic(event, playlist);
        loadAndPlay(event.getJDA().getTextChannelById(Setup.TEXTCHANNELID), queueURLs.get(hashIndex));
    }

    public void playMusic(@NotNull ReadyEvent event) {
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

        Main.jda.getGuildById(Setup.GUILDID).getSelfMember().modifyNickname(fuckinghell.getName()).queue();
        Paging<PlaylistTrack> pTracks = fuckinghell.getTracks();
        PlaylistTrack[] allTracks = pTracks.getItems();

        Collections.shuffle(Arrays.asList(allTracks));

        for (int i = 0; i < allTracks.length; i++) {
            GetTrackRequest trackRequest =  spotifyApi.getTrack(allTracks[i].getTrack().getId()).build();
            try {
                Track track = trackRequest.execute();
                String url = "ytsearch: " + track.getArtists()[0].getName() + " " + track.getName();

                queueURLs.put(i, url);
                queueInfo.put(i , track.getArtists()[0].getName() + " - " + track.getName());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (SpotifyWebApiException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        };
    }

    public void playSelectedMusic(@NotNull ReadyEvent event, int playlist) {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(ClientAccess.clientCredentials.getAccessToken())
                .build();

        GetPlaylistRequest tracks = spotifyApi.getPlaylist(Setup.playlists.get(playlist)).build();
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

        Main.jda.getGuildById(Setup.GUILDID).getSelfMember().modifyNickname(fuckinghell.getName()).queue();
        Paging<PlaylistTrack> pTracks = fuckinghell.getTracks();
        PlaylistTrack[] allTracks = pTracks.getItems();

        Collections.shuffle(Arrays.asList(allTracks));

        for (int i = 0; i < allTracks.length; i++) {
            GetTrackRequest trackRequest =  spotifyApi.getTrack(allTracks[i].getTrack().getId()).build();
            try {
                Track track = trackRequest.execute();
                String url = "ytsearch: " + track.getArtists()[0].getName() + " " + track.getName();

                queueURLs.put(i, url);
                queueInfo.put(i , track.getArtists()[0].getName() + " - " + track.getName());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (SpotifyWebApiException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        };
    }

    public JoinVoice() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }


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

    public void loadAndPlay(final TextChannel channel, final String trackUrl) {

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
        if (!guild.getSelfMember().getVoiceState().inVoiceChannel())
            connectToFirstVoiceChannel(guild.getAudioManager());
        musicManager.scheduler.queue(track);
    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager) {
                audioManager.openAudioConnection(audioManager.getGuild().getVoiceChannelById(Setup.VOICECHANNELID));
    }

     public void close(Guild guild) {
         guild.getAudioManager();
    }
}
