package com.RecetasFinal.Repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.RecetasFinal.Entities.Ingrediente;
import com.RecetasFinal.Entities.Receta;
import com.RecetasFinal.Entities.Tipo;
import com.RecetasFinal.Entities.Usuario;

import java.util.List;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Integer> {
	
    List<Receta> findByNombre(String nombre);
    List<Receta> findByTipo(Tipo tipo);
    List<Receta> findByUsuario(Usuario usuario);
    List<Receta> findByNombreLike(String nombre);

    
    //List<Receta> findByUsuarioId(int idUsuario);


}
