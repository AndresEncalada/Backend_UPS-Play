package com.upsplay.model;

import java.util.Date; // Para la fecha de estreno
import java.util.List; // Para las listas de géneros, autores, premios

import jakarta.json.bind.annotation.JsonbDateFormat;

import java.util.ArrayList; // Para inicializar las listas

public class Cancion {
    private String uuid;
    private String titulo;
    private Long fechaEstreno; // Nueva propiedad: Fecha de estreno
    private List<Genero> generos; // Nueva propiedad: Lista de objetos Genero
    private List<Autor> autores; // Nueva propiedad: Lista de objetos Autor
    private List<Premio> premios; // Nueva propiedad: Lista de objetos Premio
    private long reproducciones; // Nueva propiedad: Número de reproducciones
    private String enlaceCancion;

    // Constructor vacío (necesario para la deserialización JSON)
    public Cancion() {
        this.generos = new ArrayList<>(); // Inicializar listas para evitar NullPointerException
        this.autores = new ArrayList<>();
        this.premios = new ArrayList<>();
    }

    // Constructor con todos los campos (ajustado a las nuevas propiedades)
    public Cancion(String uuid, String titulo, Long fechaEstreno, List<Genero> generos, 
                   List<Autor> autores, List<Premio> premios, long reproducciones, String enlaceCancion) {
        this.uuid = uuid;
        this.titulo = titulo;
        this.fechaEstreno = fechaEstreno;
        this.generos = (generos != null) ? generos : new ArrayList<>();
        this.autores = (autores != null) ? autores : new ArrayList<>();
        this.premios = (premios != null) ? premios : new ArrayList<>();
        this.reproducciones = reproducciones;
        this.enlaceCancion=enlaceCancion;
    }

    // Getters y Setters para las propiedades existentes
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // Getters y Setters para las nuevas propiedades
    public Long getFechaEstreno() {
        return fechaEstreno;
    }

    public void setFechaEstreno(Long fechaEstreno) {
        this.fechaEstreno = fechaEstreno;
    }

    public List<Genero> getGeneros() {
        return generos;
    }

    public void setGeneros(List<Genero> generos) {
        this.generos = (generos != null) ? generos : new ArrayList<>();
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autores) {
        this.autores = (autores != null) ? autores : new ArrayList<>();
    }

    public List<Premio> getPremios() {
        return premios;
    }

    public void setPremios(List<Premio> premios) {
        this.premios = (premios != null) ? premios : new ArrayList<>();
    }

    public long getReproducciones() {
        return reproducciones;
    }

    public void setReproducciones(long reproducciones) {
        this.reproducciones = reproducciones;
    }
    
    
    public String getEnlaceCancion() {
		return enlaceCancion;
	}

	public void setEnlaceCancion(String enlaceCancion) {
		this.enlaceCancion = enlaceCancion;
	}

	// Opcional: Método toString para depuración
    @Override
    public String toString() {
        return "Cancion{" +
               "uuid='" + uuid + '\'' +
               ", titulo='" + titulo + '\'' +
               ", fechaEstreno=" + fechaEstreno +
               ", generos=" + generos +
               ", autores=" + autores +
               ", premios=" + premios +
               ", reproducciones=" + reproducciones +
               '}';
    }
}