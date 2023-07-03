package com.RecetasFinal.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.RecetasFinal.Entities.Receta;
import com.RecetasFinal.Entities.Recetas_Usuario;
import com.RecetasFinal.Repositories.Recetas_UsuarioRepository;

@Service
public class Recetas_UsuarioService {
	@Autowired
	private Recetas_UsuarioRepository recetas_UsuarioRepository;
	
	public List<Recetas_Usuario> obtenerPorReceta(Receta receta){
		return recetas_UsuarioRepository.findByReceta(receta);
	}
	
	public void eliminar(Recetas_Usuario recetas_Usuario) {
		recetas_UsuarioRepository.delete(recetas_Usuario);
	}
}
