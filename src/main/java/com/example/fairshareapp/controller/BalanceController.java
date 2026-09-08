package com.example.fairshareapp.controller;

import com.example.fairshareapp.model.dto.BalanceDTO;
import com.example.fairshareapp.model.dto.DeudaDetalleDTO;
import com.example.fairshareapp.model.dto.RegistrarPagoDTO;
import com.example.fairshareapp.service.BalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST del motor de balances, deudas y liquidacion cruzada.
 * Expone la consulta de la matriz simplificada de deudas de un espacio y el registro de pagos.
 */
@RestController
@RequestMapping("/api/v1")
public class BalanceController {

    private final BalanceService balanceService;

    /**
     * Constructor con inyeccion de dependencias del servicio de balances.
     *
     * @param balanceService Instancia del servicio de logica de negocio de balances.
     */
    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    /**
     * Consulta la matriz simplificada de deudas pendientes de un espacio.
     *
     * @param id Identificador unico del espacio.
     * @return Balance del espacio con codigo HTTP 200 OK.
     */
    @GetMapping("/espacios/{id}/balance")
    public ResponseEntity<BalanceDTO> obtenerBalance(@PathVariable Long id) {
        BalanceDTO balance = balanceService.obtenerBalance(id);
        return ResponseEntity.ok(balance);
    }

    /**
     * Registra el pago de una deuda, total o parcial.
     *
     * @param id Identificador unico de la deuda a saldar.
     * @param dto Datos del pago a registrar (opcional; sin monto se interpreta como pago total).
     * @return Detalle actualizado de la deuda con codigo HTTP 200 OK.
     */
    @PostMapping("/deudas/{id}/saldar")
    public ResponseEntity<DeudaDetalleDTO> saldarDeuda(@PathVariable Long id,
                                                        @RequestBody(required = false) RegistrarPagoDTO dto) {
        DeudaDetalleDTO deuda = balanceService.registrarPago(id, dto);
        return ResponseEntity.ok(deuda);
    }
}
