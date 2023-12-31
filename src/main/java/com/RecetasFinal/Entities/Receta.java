package com.RecetasFinal.Entities;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recetas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Receta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReceta")
    private int idReceta;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "idUsuario")
    private Usuario usuario;


    @Column(name = "nombre")
    private String nombre;


    @Column(name = "descripcion")
    private String descripcion;


    @OneToOne
    @JoinColumn(name = "foto")
    private Foto foto;

    @JsonIgnore
    @Column(name = "habilitado")
    private String habilitado;
    
    
    @Column(name = "porciones")
    private int porciones;


    @Column(name = "cantidadPersonas")
    private int cantidadPersonas;

    @ManyToOne
    @JoinColumn(name = "idTipo")
    private Tipo tipo;

    @OneToMany(mappedBy = "receta", orphanRemoval = true)
    private List<Foto> fotos= new ArrayList<>();
    
    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Utilizado> utilizados = new ArrayList<>();

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Calificacion> calificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paso> pasos = new ArrayList<>();
    
    
    public int getValoracionGeneral() {
    	if(calificaciones.size()==0) {
    		return 0;
    	}
    	else {
    		int valoracionTotal = calificaciones.stream().mapToInt(Calificacion::getCalificacion).sum();
    		long cantidadCalificacionesPositivas = calificaciones.stream()
    		        .filter(calificacion -> calificacion.getCalificacion() > 0)
    		        .count();
    		if(cantidadCalificacionesPositivas == 0) {
    			return 0;
    		}
    		else {
    			return (int) (valoracionTotal/cantidadCalificacionesPositivas);
    		}
        	
    	}
    }
    
    public void modificarIngrediente(Ingrediente ingrediente, int cantidad) {
    	Utilizado utilizado = utilizados.stream().filter(u -> u.isUtilizado(ingrediente)).findFirst().get();
    	float cantidadActual = utilizado.getCantidad();
    	float proporcion = (float) cantidad/cantidadActual;
    	cantidadPersonas = (int) Math.floor(cantidadPersonas*proporcion);
    	porciones = (int) Math.floor((float)porciones * proporcion);
    	for(Utilizado utilizadoNuevos: utilizados) {
    		utilizadoNuevos.setCantidad((int) Math.ceil(utilizadoNuevos.getCantidad()*proporcion));
    	}
    	//int resultado = (int) Math.ceil(entero * flotante);
    }
    
    public void modificarPorciones(int porcion) {
    	float porcionActual = porciones;
    	float proporcion = (float) porcion/porcionActual;
    	System.out.println(proporcion);
    	porciones = porcion;
    	cantidadPersonas = (int) Math.floor(cantidadPersonas*proporcion);
    	for(Utilizado utilizadoNuevos: utilizados) {
    		utilizadoNuevos.setCantidad((int) Math.ceil(utilizadoNuevos.getCantidad()*proporcion));
    	}	
    }
    
    public void modificarCantPersonas(int personas) {
    	float personasActual = cantidadPersonas;
    	float proporcion = (float) personas/personasActual;
    	cantidadPersonas = personas;
    	porciones = (int) Math.floor(porciones*proporcion);
    	for(Utilizado utilizadoNuevos: utilizados) {
    		utilizadoNuevos.setCantidad((int) Math.ceil(utilizadoNuevos.getCantidad()*proporcion));
    	}	
    }
    
    public void getRecetaMitad() {
    	for(Utilizado utilizado: utilizados) {
    		utilizado.setCantidad(utilizado.getCantidad()/2);
    	}
    	porciones = porciones/2;
    	cantidadPersonas = cantidadPersonas/2;
    }
    
    public void getRecetaDoble() {
    	for(Utilizado utilizado: utilizados) {
    		utilizado.setCantidad(utilizado.getCantidad()*2);
    	}
    	porciones = porciones*2;
    	cantidadPersonas = cantidadPersonas*2;
    }
    
    public void addCalificacion(Calificacion calificacion) {
    	calificaciones.add(calificacion);
    }
    
    public void addPaso(Paso paso) {
    	pasos.add(paso);
    }
    public void addFotos(Foto foto) {
    	fotos.add(foto);
    }
    public void addutilizados(Utilizado utilizado) {
    	utilizados.add(utilizado);
    }
    
    public String getNombreUsuario() {
    	return this.usuario.getNombre();
    }
    
    public String getFotoUsuario() {
    	return this.usuario.getAvatar();
    }
    
    public boolean tieneIngrediente(Ingrediente ingrediente) {
    	return 0<utilizados.stream().filter(u -> u.getIngrediente() == ingrediente).count();
    }

	@Override
	public String toString() {
		return "Receta [idReceta=" + idReceta + ", usuario=" + usuario + ", nombre=" + nombre + ", descripcion="
				+ descripcion + ", porciones=" + porciones + ", cantidadPersonas=" + cantidadPersonas
				+ ", tipo=" + tipo.getDescripcion() + "]";
	}
    
    
}
