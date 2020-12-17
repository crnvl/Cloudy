package listeners;

import audio.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utilities.Setup;

public class Commands extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        if(message.getContentRaw().startsWith(Setup.PREFIX)) {
            String[] args = message.getContentRaw().replace(Setup.PREFIX, "").split(" ");
            switch (args[0]) {
                case "next":
                    AudioPlayer player = null;
                    TrackScheduler trackScheduler = new TrackScheduler(player);
                    trackScheduler.clear();
                    JoinVoice joinVoice = new JoinVoice();
                    joinVoice.playMusic(JoinVoice.emitter);

                    message.getTextChannel().sendMessage(":white_check_mark: **Skipped to the next Playlist!**").queue();
                    break;
            }
        }
    }
}
