package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import core.Main;
import listeners.JoinVoice;
import net.dv8tion.jda.api.entities.Activity;
import utilities.Setup;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public static void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        JoinVoice jv = new JoinVoice();
        if (JoinVoice.queueURLs.size() < (JoinVoice.hashIndex - 1)) {
            JoinVoice.hashIndex++;
            jv.loadAndPlay(Main.jda.getTextChannelById(Setup.TEXTCHANNELID), JoinVoice.queueURLs.get(JoinVoice.hashIndex));
        }else {
            JoinVoice.hashIndex = 0;
            JoinVoice.queueURLs.clear();
            JoinVoice.queueInfo.clear();
            jv.triggerPlayer(JoinVoice.emitter);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            JoinVoice jv = new JoinVoice();
            if (JoinVoice.queueURLs.size() < (JoinVoice.hashIndex - 1)) {
                JoinVoice.hashIndex++;
                jv.loadAndPlay(Main.jda.getTextChannelById(Setup.TEXTCHANNELID), JoinVoice.queueURLs.get(JoinVoice.hashIndex));
            }else {
                JoinVoice.hashIndex = 0;
                JoinVoice.queueURLs.clear();
                JoinVoice.queueInfo.clear();
                jv.triggerPlayer(JoinVoice.emitter);
            }
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        Main.jda.getPresence().setActivity(Activity.listening(JoinVoice.queueInfo.get(JoinVoice.hashIndex)));
        Main.jda.getTextChannelById(Setup.TEXTCHANNELID).sendMessage("Now Playing: ```\n" + track.getInfo().title + "\n```").queue(message -> message.delete().queueAfter(10, TimeUnit.SECONDS));
    }

    public void clear() {
        queue.clear();
    }
}
