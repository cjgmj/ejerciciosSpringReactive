package com.cjgmj.webflux.models.documents;

import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

@Document(collection = "productos")
public class Producto {

	@Id
	private String id;

	@NotEmpty
	private String nombre;

	@NotNull
	private Double precio;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createAt;

	@Valid
	private Categoria categoria;

	private String foto;

	public Producto() {
	}

	public Producto(String nombre, Double precio) {
		this.nombre = nombre;
		this.precio = precio;
	}

	public Producto(String nombre, Double precio, Categoria categoria) {
		this(nombre, precio);
		this.categoria = categoria;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Double getPrecio() {
		return this.precio;
	}

	public void setPrecio(Double precio) {
		this.precio = precio;
	}

	public Date getCreateAt() {
		return this.createAt;
	}

	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}

	public Categoria getCategoria() {
		return this.categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public String getFoto() {
		return this.foto;
	}

	public void setFoto(String foto) {
		this.foto = foto;
	}

}
