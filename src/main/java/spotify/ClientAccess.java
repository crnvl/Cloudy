package spotify;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import utilities.Setup;

import javax.swing.*;
import java.io.IOException;

public class ClientAccess {

    private static final String clientId = Setup.SPOTIFY_CLIENTID;
    private static final String clientSecret = Setup.SPOTIFY_CLIENTSECRET;
    private static final SpotifyApi spotifyApi = new SpotifyApi.Builder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .build();
    private static final ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials()
            .build();
    public static ClientCredentials clientCredentials;

    public static void clientCredentials_Sync() throws org.apache.hc.core5.http.ParseException {
        try {
            clientCredentials = clientCredentialsRequest.execute();

            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            Timer stonks = new Timer(clientCredentials.getExpiresIn() * 1000, e -> {
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                System.out.println(clientCredentials.getExpiresIn());
            }
            );
            stonks.start();

        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
