package com.RecetasFinal.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.RecetasFinal.Entities.Unidad;
import com.RecetasFinal.Repositories.UnidadRepository;
import com.RecetasFinal.Repositories.UsuarioRepository;

@Service
public class UnidadService {

    @Autowired
    private UnidadRepository unidadRepository;
    
    
    @Autowired
    public List<Unidad> findAll(){
    	return unidadRepository.findAll();
    }
}
