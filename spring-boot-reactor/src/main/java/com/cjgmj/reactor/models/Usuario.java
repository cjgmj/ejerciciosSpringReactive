package com.cjgmj.reactor.models;

public class Usuario {

	private String nombre;
	private String apellido;

	public Usuario() {
	}

	public Usuario(String nombre, String apellido) {
		this.nombre = nombre;
		this.apellido = apellido;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return this.apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	@Override
	public String toString() {
		return "Usuario [nombre=" + this.nombre + ", apellido=" + this.apellido + "]";
	}

}
