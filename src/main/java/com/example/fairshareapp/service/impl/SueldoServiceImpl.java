package com.example.fairshareapp.service.impl;

import com.example.fairshareapp.model.entity.Sueldo;
import com.example.fairshareapp.model.entity.Usuario;
import com.example.fairshareapp.model.response.SueldoResponse;
import com.example.fairshareapp.repository.SueldoRepository;
import com.example.fairshareapp.repository.UsuarioRepository;
import com.example.fairshareapp.service.SueldoService;
import com.example.fairshareapp.model.mapper.SueldoMapper;
import com.example.fairshareapp.model.request.SueldoRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SueldoServiceImpl implements SueldoService {

    private final SueldoRepository sueldoRepository;
    private final SueldoMapper sueldoMapper;

    public SueldoServiceImpl(SueldoRepository SR, SueldoMapper SM) {
        this.sueldoRepository = SR;
        this.sueldoMapper = SM;
    }

    @Override
    public SueldoResponse crearSueldo(Long usuarioId, SueldoRequest sueldoRequest) {

        Sueldo sueldo = Sueldo.builder()
                .monto(sueldoRequest.getMonto())
                .tipo(sueldoRequest.getTipo())
                .frecuencia(sueldoRequest.getFrecuencia())
                .usuario(new Usuario()) // Estatico de Momento, faltan metodos en UsuarioRepository
                .fechaInicio(LocalDate.now())
                .build();
        sueldoRepository.save(sueldo);
        return sueldoMapper.toResponse(sueldo);
    }

    @Override
    public SueldoResponse crearSueldoCopia(Sueldo copia) {
        sueldoRepository.save(copia);
        return sueldoMapper.toResponse(copia);
    }

    @Override
    public SueldoResponse obtenerSueldo(Long id) {
        return sueldoMapper.toResponse(sueldoRepository.findById(id).orElseThrow());
    }

    @Override
    public List<SueldoResponse> obtenerSueldosPorUsuario(Long usuarioId) {
        return sueldoMapper.toResponseList(sueldoRepository.findByUsuario_Id(usuarioId));
    }

    @Override
    public void actualizarSueldo(Long id, SueldoRequest sueldoRequest) {
        Sueldo sueldo = sueldoRepository.findById(id).orElseThrow();
        sueldo.setMonto(sueldoRequest.getMonto());
        sueldo.setTipo(sueldoRequest.getTipo());
        sueldo.setFrecuencia(sueldoRequest.getFrecuencia());

        sueldoRepository.save(sueldo);
    }

    @Override
    public void eliminarSueldo(Long id) {
        sueldoRepository.deleteById(id);
    }

    @Override
    public List<SueldoResponse> getAllSueldos() {
        return sueldoRepository.findAllResponse();
    }

    @Override
    public void deleteSueldo(Long id) {
        sueldoRepository.deleteById(id);
    }

    @Override
    public Sueldo updateSueldo(Long id, SueldoResponse sueldo) {
        Sueldo sueldoFinded = sueldoRepository.findById(id).orElseThrow();

        return sueldoRepository.save(sueldoFinded);
    }

}
