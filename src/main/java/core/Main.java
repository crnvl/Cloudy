package core;

import listeners.Commands;
import listeners.JoinVoice;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.apache.hc.core5.http.ParseException;
import spotify.ClientAccess;
import utilities.Setup;

import javax.security.auth.login.LoginException;

public class Main extends ListenerAdapter {

    public static JDA jda;

    public static void main(String[] args) throws LoginException, ParseException {
        ClientAccess.clientCredentials_Sync();

        JDABuilder builder = JDABuilder.createDefault(Setup.TOKEN);
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setCompression(Compression.NONE);

        builder.addEventListeners(new JoinVoice(), new Commands());
        configureMemoryUsage(builder);

        builder.build();
    }

    public static void configureMemoryUsage(JDABuilder builder) {
        builder.disableCache(CacheFlag.ACTIVITY);
        builder.setMemberCachePolicy(MemberCachePolicy.VOICE.or(MemberCachePolicy.OWNER));
        builder.setChunkingFilter(ChunkingFilter.NONE);
        builder.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING);
        builder.setLargeThreshold(50);
    }
}
