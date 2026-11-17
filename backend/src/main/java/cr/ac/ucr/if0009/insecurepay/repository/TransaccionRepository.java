package cr.ac.ucr.if0009.insecurepay.repository;

import cr.ac.ucr.if0009.insecurepay.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByCuentaOrigenIdOrCuentaDestinoIdOrderByFechaDesc(Long cuentaOrigenId, Long cuentaDestinoId);

    List<Transaccion> findAllByOrderByFechaDesc();

    @Query("SELECT t FROM Transaccion t WHERE t.cuentaOrigen.usuario.id = :usuarioId " +
            "OR t.cuentaDestino.usuario.id = :usuarioId ORDER BY t.fecha DESC")
    List<Transaccion> findDelUsuario(@Param("usuarioId") Long usuarioId);

    @Query("SELECT t FROM Transaccion t WHERE t.conceptoHtml LIKE CONCAT('%', :termino, '%') " +
            "OR t.estado LIKE CONCAT('%', :termino, '%') ORDER BY t.fecha DESC")
    List<Transaccion> buscarPorTerminoSeguro(@Param("termino") String termino);

    @Query("SELECT t FROM Transaccion t WHERE (t.cuentaOrigen.usuario.id = :usuarioId " +
            "OR t.cuentaDestino.usuario.id = :usuarioId) " +
            "AND (t.conceptoHtml LIKE CONCAT('%', :termino, '%') OR t.estado LIKE CONCAT('%', :termino, '%')) " +
            "ORDER BY t.fecha DESC")
    List<Transaccion> buscarPorTerminoSeguroDelUsuario(@Param("termino") String termino,
            @Param("usuarioId") Long usuarioId);
}