package com.proyecto.learncenterhub.entidades;

import java.util.ArrayList;
import java.util.List;

public class Catalogo {

    private List<Curso> cursos = new ArrayList<>();
    private List<String> tipos = new ArrayList<>();
    private List<Aplicacion> aplicaciones = new ArrayList<>();

    public List<Curso> getCursos() {
        if (cursos == null) {
            cursos = new ArrayList<>();
        }
        return cursos;
    }

    public List<String> getTipos() {
        if (tipos == null) {
            tipos = new ArrayList<>();
        }
        return tipos;
    }

    public List<Aplicacion> getAplicaciones() {
        if (aplicaciones == null) {
            aplicaciones = new ArrayList<>();
        }
        return aplicaciones;
    }

}
