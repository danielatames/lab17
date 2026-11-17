package cr.ac.ucr.if0009.insecurepay.util;

public record SesionUsuario(String username, String rol, Long usuarioId) {
    public boolean esPrivilegiado() {
        return "ADMIN".equals(rol) || "AUDITOR".equals(rol);
    }
}