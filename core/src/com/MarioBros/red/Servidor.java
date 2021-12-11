package com.MarioBros.red;

public class Servidor {

	HiloServidor hs;

	public Servidor() {
		hs = new HiloServidor();
		hs.start();
	}

	public void enviarATodos(String msg) {
		hs.enviarATodos(msg);
	}
	
}
