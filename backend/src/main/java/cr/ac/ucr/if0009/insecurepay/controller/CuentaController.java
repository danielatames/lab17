package cr.ac.ucr.if0009.insecurepay.controller;

import cr.ac.ucr.if0009.insecurepay.dto.CuentaDto;
import cr.ac.ucr.if0009.insecurepay.service.CuentaService;
import cr.ac.ucr.if0009.insecurepay.util.AutenticacionUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;
    private final AutenticacionUtil auth;

    public CuentaController(CuentaService cuentaService, AutenticacionUtil auth) {
        this.cuentaService = cuentaService;
        this.auth = auth;
    }

    @GetMapping
    public ResponseEntity<List<CuentaDto>> listarTodas(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(cuentaService.listarTodas(auth.requerirSesion(authHeader)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaDto> obtenerPorId(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(cuentaService.obtenerPorId(id, auth.requerirSesion(authHeader)));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CuentaDto>> obtenerPorUsuario(@PathVariable Long usuarioId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return ResponseEntity.ok(cuentaService.obtenerPorUsuario(usuarioId, auth.requerirSesion(authHeader)));
    }
}