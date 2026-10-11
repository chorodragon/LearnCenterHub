package com.proyecto.learncenterhub.entidades;

import java.util.function.Function;

public enum Categoria {

    CURSO("Curso", "Cursos", "un curso", "el curso", "Nuevo curso", Aplicacion::getCurso),
    MATERIA("Materia", "Materias", "una materia", "la materia", "Nueva materia", Aplicacion::getMateria),
    TIPO("Tipo", "Tipos", "un tipo", "el tipo", "Nuevo tipo", Aplicacion::getTipo);

    private final String nombre;
    private final String plural;
    private final String conArticulo;
    private final String conArticuloDefinido;
    private final String nuevo;
    private final Function<Aplicacion, String> valorDe;

    Categoria(String nombre, String plural, String conArticulo, String conArticuloDefinido, String nuevo,
            Function<Aplicacion, String> valorDe) {
        this.nombre = nombre;
        this.plural = plural;
        this.conArticulo = conArticulo;
        this.conArticuloDefinido = conArticuloDefinido;
        this.nuevo = nuevo;
        this.valorDe = valorDe;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPlural() {
        return plural;
    }

    public String getConArticulo() {
        return conArticulo;
    }

    public String getConArticuloDefinido() {
        return conArticuloDefinido;
    }

    public String getNuevo() {
        return nuevo;
    }

    public String valorDe(Aplicacion aplicacion) {
        return valorDe.apply(aplicacion);
    }
}
