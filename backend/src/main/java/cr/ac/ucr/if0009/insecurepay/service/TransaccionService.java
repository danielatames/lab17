package cr.ac.ucr.if0009.insecurepay.service;

import cr.ac.ucr.if0009.insecurepay.dto.TransaccionDto;
import cr.ac.ucr.if0009.insecurepay.dto.TransferenciaDto;
import cr.ac.ucr.if0009.insecurepay.exception.AccesoDenegadoException;
import cr.ac.ucr.if0009.insecurepay.exception.NegocioException;
import cr.ac.ucr.if0009.insecurepay.exception.RecursoNoEncontradoException;
import cr.ac.ucr.if0009.insecurepay.model.CuentaBancaria;
import cr.ac.ucr.if0009.insecurepay.model.Transaccion;
import cr.ac.ucr.if0009.insecurepay.repository.CuentaRepository;
import cr.ac.ucr.if0009.insecurepay.repository.TransaccionRepository;
import cr.ac.ucr.if0009.insecurepay.util.SesionUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;

    public TransaccionService(TransaccionRepository transaccionRepository, CuentaRepository cuentaRepository) {
        this.transaccionRepository = transaccionRepository;
        this.cuentaRepository = cuentaRepository;
    }

    public List<TransaccionDto> listarTodas(SesionUsuario sesion) {
        List<Transaccion> lista = sesion.esPrivilegiado()
                ? transaccionRepository.findAllByOrderByFechaDesc()
                : transaccionRepository.findDelUsuario(sesion.usuarioId());
        return lista.stream().map(this::convertirADto).collect(Collectors.toList());
    }

    public TransaccionDto obtenerPorId(Long id, SesionUsuario sesion) {
        Transaccion t = transaccionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Transacción no encontrada."));
        boolean participa = Objects.equals(t.getCuentaOrigen().getUsuario().getId(), sesion.usuarioId())
                || Objects.equals(t.getCuentaDestino().getUsuario().getId(), sesion.usuarioId());
        if (!participa && !sesion.esPrivilegiado()) {
            throw new AccesoDenegadoException("Acceso no autorizado a transacción ajena.");
        }
        return convertirADto(t);
    }

    public List<TransaccionDto> obtenerPorCuenta(Long cuentaId, SesionUsuario sesion) {
        CuentaBancaria cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta bancaria no encontrada."));
        if (!Objects.equals(cuenta.getUsuario().getId(), sesion.usuarioId()) && !sesion.esPrivilegiado()) {
            throw new AccesoDenegadoException("Acceso no autorizado a cuenta ajena.");
        }
        return transaccionRepository.findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(cuentaId, cuentaId)
                .stream().map(this::convertirADto).collect(Collectors.toList());
    }

    public List<TransaccionDto> buscarPorConcepto(String termino, SesionUsuario sesion) {
        if (termino == null || termino.trim().isEmpty()) {
            return List.of();
        }
        String t = termino.trim();
        List<Transaccion> res = sesion.esPrivilegiado()
                ? transaccionRepository.buscarPorTerminoSeguro(t)
                : transaccionRepository.buscarPorTerminoSeguroDelUsuario(t, sesion.usuarioId());
        return res.stream().map(this::convertirADto).collect(Collectors.toList());
    }

    @Transactional
    public TransaccionDto transferir(TransferenciaDto dto, SesionUsuario sesion) {
        if (dto.getMonto() == null || dto.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El monto a transferir debe ser mayor a cero.");
        }

        CuentaBancaria cuentaOrigen = cuentaRepository.findById(dto.getCuentaOrigenId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta de origen no encontrada."));

        if (!Objects.equals(cuentaOrigen.getUsuario().getId(), sesion.usuarioId())) {
            throw new AccesoDenegadoException("Fraude detectado: no puede debitar fondos de cuentas ajenas.");
        }

        CuentaBancaria cuentaDestino = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuentaDestino())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta de destino no encontrada."));

        if (Objects.equals(cuentaOrigen.getId(), cuentaDestino.getId())) {
            throw new NegocioException("La cuenta de origen y destino no pueden ser la misma.");
        }
        if (cuentaOrigen.getSaldo().compareTo(dto.getMonto()) < 0) {
            throw new NegocioException("Fondos insuficientes en la cuenta de origen.");
        }

        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(dto.getMonto()));
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(dto.getMonto()));
        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);

        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(cuentaOrigen);
        transaccion.setCuentaDestino(cuentaDestino);
        transaccion.setMonto(dto.getMonto());
        transaccion.setConceptoHtml(sanitizarTexto(dto.getConceptoHtml())); // XSS: defensa en profundidad
        transaccion.setEstado("COMPLETADA");

        return convertirADto(transaccionRepository.save(transaccion));
    }

    private String sanitizarTexto(String entrada) {
        if (entrada == null)
            return "";
        String recortado = entrada.length() > 500 ? entrada.substring(0, 500) : entrada;
        return recortado
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    private TransaccionDto convertirADto(Transaccion t) {
        TransaccionDto dto = new TransaccionDto();
        dto.setId(t.getId());
        dto.setCuentaOrigenId(t.getCuentaOrigen().getId());
        dto.setNumeroCuentaOrigen(t.getCuentaOrigen().getNumeroCuenta());
        dto.setTitularOrigen(t.getCuentaOrigen().getUsuario().getNombreCompleto());
        dto.setCuentaDestinoId(t.getCuentaDestino().getId());
        dto.setNumeroCuentaDestino(t.getCuentaDestino().getNumeroCuenta());
        dto.setTitularDestino(t.getCuentaDestino().getUsuario().getNombreCompleto());
        dto.setMonto(t.getMonto());
        dto.setConceptoHtml(t.getConceptoHtml());
        dto.setFecha(t.getFecha());
        dto.setEstado(t.getEstado());
        return dto;
    }
}