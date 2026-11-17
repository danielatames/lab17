package cr.ac.ucr.if0009.insecurepay.service;

import cr.ac.ucr.if0009.insecurepay.dto.CuentaDto;
import cr.ac.ucr.if0009.insecurepay.exception.AccesoDenegadoException;
import cr.ac.ucr.if0009.insecurepay.exception.RecursoNoEncontradoException;
import cr.ac.ucr.if0009.insecurepay.model.CuentaBancaria;
import cr.ac.ucr.if0009.insecurepay.repository.CuentaRepository;
import cr.ac.ucr.if0009.insecurepay.util.SesionUsuario;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaService(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    public List<CuentaDto> listarTodas(SesionUsuario sesion) {
        if (!sesion.esPrivilegiado()) {
            throw new AccesoDenegadoException("Requiere privilegios de administración o auditoría.");
        }
        return cuentaRepository.findAll().stream().map(this::convertirADto).collect(Collectors.toList());
    }

    public CuentaDto obtenerPorId(Long id, SesionUsuario sesion) {
        CuentaBancaria cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta bancaria no encontrada."));
        boolean esPropietario = Objects.equals(cuenta.getUsuario().getId(), sesion.usuarioId());
        if (!esPropietario && !sesion.esPrivilegiado()) {
            throw new AccesoDenegadoException("Acceso no autorizado a cuenta ajena.");
        }
        return convertirADto(cuenta);
    }

    public List<CuentaDto> obtenerPorUsuario(Long usuarioId, SesionUsuario sesion) {
        if (!Objects.equals(usuarioId, sesion.usuarioId()) && !sesion.esPrivilegiado()) {
            throw new AccesoDenegadoException("Acceso no autorizado a cuentas ajenas.");
        }
        return cuentaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::convertirADto).collect(Collectors.toList());
    }

    private CuentaDto convertirADto(CuentaBancaria cuenta) {
        CuentaDto dto = new CuentaDto();
        dto.setId(cuenta.getId());
        dto.setUsuarioId(cuenta.getUsuario().getId());
        dto.setTitular(cuenta.getUsuario().getNombreCompleto());
        dto.setNumeroCuenta(cuenta.getNumeroCuenta());
        dto.setTipoCuenta(cuenta.getTipoCuenta().name());
        dto.setSaldo(cuenta.getSaldo());
        dto.setMoneda(cuenta.getMoneda());
        dto.setActiva(cuenta.getActiva());
        return dto;
    }
}