package com.example.fairshareapp.security;
import com.example.fairshareapp.exception.*;
import com.example.fairshareapp.repository.*;
import com.example.fairshareapp.model.enums.RolMiembro;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service("acceso") @RequiredArgsConstructor @Transactional(readOnly=true)
public class AccesoService {
    private final EspacioRepository espacios;
    private final MiembroEspacioRepository miembros;
    private final GastoRepository gastos;
    private final SueldoRepository sueldos;
    private final PlantillaGastoRepository plantillas;
    private final SaldoDeudaRepository deudas;
    public boolean propio(Long id) { return id != null && UsuarioActual.requerido().getId().equals(id); }
    public boolean miembro(Long espacioId) {
        if (!espacios.existsById(espacioId)) throw new RecursoNoEncontradoException("Espacio inexistente");
        return miembros.existsByEspacioIdAndUsuarioId(espacioId, UsuarioActual.requerido().getId());
    }
    public boolean admin(Long espacioId) {
        return miembro(espacioId) && miembros.existsByEspacioIdAndUsuarioIdAndRol(espacioId, UsuarioActual.requerido().getId(), RolMiembro.ADMIN);
    }
    public boolean gasto(Long id) {
        var g = gastos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Gasto inexistente"));
        return miembro(g.getEspacio().getId());
    }
    public boolean eliminarGasto(Long id) {
        var g = gastos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Gasto inexistente"));
        return miembro(g.getEspacio().getId()) && (propio(g.getPagador().getId()) || admin(g.getEspacio().getId()));
    }
    public boolean sueldo(Long id) {
        return propio(sueldos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Sueldo inexistente")).getUsuario().getId());
    }
    public boolean plantilla(Long id) {
        return miembro(plantillas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Plantilla inexistente")).getEspacioId().getId());
    }
    public boolean pago(Long id) {
        var d = deudas.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Deuda inexistente"));
        return miembro(d.getEspacio().getId()) && (propio(d.getDeudor().getId()) || admin(d.getEspacio().getId()));
    }
}
