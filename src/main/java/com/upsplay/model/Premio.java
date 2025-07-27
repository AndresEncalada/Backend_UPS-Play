package com.upsplay.model;


public class Premio {
    private String uuid;
    private String nombrePremio; // Ej. "Grammy", "Premio Lo Nuestro"
    private int anio;
    private String categoria; // Ej. "Mejor Canción del Año", "Mejor Artista Pop"

    public Premio() {
    }

    public Premio(String uuid, String nombrePremio, int anio, String categoria) {
        this.uuid = uuid;
        this.nombrePremio = nombrePremio;
        this.anio = anio;
        this.categoria = categoria;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getNombrePremio() {
        return nombrePremio;
    }

    public void setNombrePremio(String nombrePremio) {
        this.nombrePremio = nombrePremio;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public String toString() {
        return "Premio{" +
               "uuid='" + uuid + '\'' +
               ", nombrePremio='" + nombrePremio + '\'' +
               ", anio=" + anio +
               ", categoria='" + categoria + '\'' +
               '}';
    }
}