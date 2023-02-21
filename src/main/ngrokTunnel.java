package main;

import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Proto;
import com.github.alexdlaird.ngrok.protocol.Tunnel;

public class ngrokTunnel {

    public static void startNgrok(String authToken){
        try {
            final NgrokClient ngrokClient = new NgrokClient.Builder().build();
            //Don't leak this auth token.
            ngrokClient.setAuthToken(authToken);

            final CreateTunnel sshCreateTunnel = new CreateTunnel.Builder()
                    .withProto(Proto.HTTP)
                    .withAddr(4567)
                    .build();
            final Tunnel httpTunnel = ngrokClient.connect(sshCreateTunnel);

            String url = httpTunnel.getPublicUrl();
            System.out.println("Url for API: " + url);

        } catch (Exception e) {
            System.out.println("ERROR, ngrok failed");
        }
    }
}
