package com.proyecto.learncenterhub.entidades;

public class Aplicacion {

    private int id;
    private String nombre;
    private String descripcion;
    private String comando;
    private String caratula;
    private String tags;
    private String curso;
    private String materia;
    private String tipo;

    public Aplicacion(int id, String nombre, String descripcion, String comando, String caratula, String tags,
            String curso, String materia, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.comando = comando;
        this.caratula = caratula;
        this.tags = tags;
        this.curso = curso;
        this.materia = materia;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getComando() {
        return comando;
    }

    public String getCaratula() {
        return caratula;
    }

    public String getTags() {
        return tags;
    }

    public String getCurso() {
        return curso;
    }

    public String getMateria() {
        return materia;
    }

    public String getTipo() {
        return tipo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setComando(String comando) {
        this.comando = comando;
    }

}
