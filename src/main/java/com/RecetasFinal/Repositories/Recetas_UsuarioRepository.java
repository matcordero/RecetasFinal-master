package com.RecetasFinal.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.RecetasFinal.Entities.Receta;
import com.RecetasFinal.Entities.Recetas_Usuario;

@Repository
public interface Recetas_UsuarioRepository extends JpaRepository<Recetas_Usuario, Integer> {
	List<Recetas_Usuario> findByReceta(Receta receta);
}
