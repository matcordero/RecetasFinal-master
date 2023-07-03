package com.RecetasFinal.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UtilizadoRecibido {
    private int idUtilizado;

    private int idReceta;


    private Ingrediente ingrediente;


    private int cantidad;


    private Unidad unidad;


    private String observaciones;
    

}