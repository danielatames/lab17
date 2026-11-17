package cr.ac.ucr.if0009.insecurepay.util;

import cr.ac.ucr.if0009.insecurepay.exception.NoAutenticadoException;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

@Component
public class AutenticacionUtil {

    private final JwtUtil jwtUtil;

    public AutenticacionUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public SesionUsuario requerirSesion(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new NoAutenticadoException("Se requiere autenticación.");
        }
        try {
            Claims c = jwtUtil.getClaims(authHeader.substring(7));
            return new SesionUsuario(
                    c.getSubject(),
                    c.get("rol", String.class),
                    ((Number) c.get("usuarioId")).longValue());
        } catch (Exception e) {
            throw new NoAutenticadoException("Token inválido o expirado.");
        }
    }
}