package cr.ac.ucr.if0009.insecurepay.controller;

import cr.ac.ucr.if0009.insecurepay.dto.TransaccionDto;
import cr.ac.ucr.if0009.insecurepay.dto.TransferenciaDto;
import cr.ac.ucr.if0009.insecurepay.service.TransaccionService;
import cr.ac.ucr.if0009.insecurepay.util.AutenticacionUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final AutenticacionUtil auth;

    public TransaccionController(TransaccionService transaccionService, AutenticacionUtil auth) {
        this.transaccionService = transaccionService;
        this.auth = auth;
    }

    @GetMapping
    public ResponseEntity<List<TransaccionDto>> listarTodas(
            @RequestHeader(value = "Authorization", required = false) String h) {
        return ResponseEntity.ok(transaccionService.listarTodas(auth.requerirSesion(h)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransaccionDto> obtenerPorId(@PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String h) {
        return ResponseEntity.ok(transaccionService.obtenerPorId(id, auth.requerirSesion(h)));
    }

    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<List<TransaccionDto>> obtenerPorCuenta(@PathVariable Long cuentaId,
            @RequestHeader(value = "Authorization", required = false) String h) {
        return ResponseEntity.ok(transaccionService.obtenerPorCuenta(cuentaId, auth.requerirSesion(h)));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<TransaccionDto>> buscarPorConcepto(@RequestParam("termino") String termino,
            @RequestHeader(value = "Authorization", required = false) String h) {
        return ResponseEntity.ok(transaccionService.buscarPorConcepto(termino, auth.requerirSesion(h)));
    }

    @PostMapping("/transferir")
    public ResponseEntity<TransaccionDto> transferir(@RequestBody TransferenciaDto dto,
            @RequestHeader(value = "Authorization", required = false) String h) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionService.transferir(dto, auth.requerirSesion(h)));
    }
}