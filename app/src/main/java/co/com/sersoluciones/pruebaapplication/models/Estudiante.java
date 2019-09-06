package co.com.sersoluciones.pruebaapplication.models;

import java.io.Serializable;

public class Estudiante implements Serializable {

    private String nombre;
    private String apellido;
    private String curso;

    public Estudiante() {
    }

    public Estudiante(String nombre, String apellido, String curso) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.curso = curso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    @Override
    public String toString() {
        return String.format("%s %s, Curso: %s", nombre, apellido, curso);
    }
}
