package listeners;

import audio.GuildMusicManager;
import audio.TrackScheduler;
import core.Main;
import net.dv8tion.jda.api.Permission;
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
            TrackScheduler trackScheduler;
            JoinVoice joinVoice;
            switch (args[0]) {
                case "next":
                    trackScheduler = new TrackScheduler(GuildMusicManager.player);
                    trackScheduler.clear();
                    joinVoice = new JoinVoice();
                    joinVoice.playMusic(JoinVoice.emitter);

                    message.getTextChannel().sendMessage(":white_check_mark: **Skipped to the next playlist!**").queue();
                    break;
                case "skip":
                        trackScheduler = new TrackScheduler(GuildMusicManager.player);
                        trackScheduler.nextTrack();
                        message.getTextChannel().sendMessage(":white_check_mark: **Playing the next track!**").queue();
                    break;
                case "shutdown":
                    if(event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
                        joinVoice = new JoinVoice();
                        joinVoice.close(event.getGuild());
                        System.exit(0);
                    }
                    break;
            }
        }
    }
}
