package com.upsplay.model;


public class Autor {
    private String uuid;
    private String nombre; 
    private String paisOrigen; 
    private int edad;

    public Autor() {
    }

    public Autor(String uuid, String nombre, String paisOrigen, int edad ) {
        this.uuid = uuid;
        this.nombre = nombre;
        this.edad= edad;
        this.paisOrigen = paisOrigen;
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

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
    }
    
    

    public int getEdad() {
		return edad;
	}

	public void setEdad(int edad) {
		this.edad = edad;
	}

	@Override
    public String toString() {
        return "Autor{" +
               "uuid='" + uuid + '\'' +
               ", nombre='" + nombre + '\'' +
               ", paisOrigen='" + paisOrigen + '\'' +
               '}';
    }
}