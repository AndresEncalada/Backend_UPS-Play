package com.upsplay.model;


public class Genero {
    private String uuid;
    private String nombre; // Ej. "Rock", "Pop", "Salsa"

    public Genero() {
    }

    public Genero(String uuid, String nombre) {
        this.uuid = uuid;
        this.nombre = nombre;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Genero{" +
               "uuid='" + uuid + '\'' +
               ", nombre='" + nombre + '\'' +
               '}';
    }
}