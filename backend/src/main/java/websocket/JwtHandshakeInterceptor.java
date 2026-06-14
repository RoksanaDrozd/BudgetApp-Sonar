package pk.rd.pasir_drozd_roksana.websocket;

import pk.rd.pasir_drozd_roksana.security.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        URI uri = request.getURI();
        String query = uri.getQuery();

        if (query == null || !query.contains("token=")) {
            return false;
        }

        String token = query.substring(query.indexOf("token=") + 6);

        if (token.contains("&")) {
            token = token.substring(0, token.indexOf("&"));
        }

        if (!jwtUtil.validateToken(token)) {
            return false;
        }

        String email = jwtUtil.extractUsername(token);
        attributes.put("email", email);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // POPRAWIONE: Dodano komentarz wyjaśniający dla Sonara, dlaczego metoda pozostaje pusta
        // Logika po uścisku dłoni (handshake) nie jest wymagana w tej konfiguracji bezpieczeństwa.
    }
}