package com.cjgmj.reactor.models;

public class UsuarioComentario {

	private Usuario usuario;

	private Comentario comentario;

	public UsuarioComentario(Usuario usuario, Comentario comentario) {
		this.usuario = usuario;
		this.comentario = comentario;
	}

	@Override
	public String toString() {
		return "UsuarioComentario [usuario=" + this.usuario + ", comentario=" + this.comentario + "]";
	}

}
