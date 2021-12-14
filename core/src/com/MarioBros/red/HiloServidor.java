package com.MarioBros.red;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.MarioBros.Utilidades.Utiles;

public class HiloServidor extends Thread {

	DatagramSocket conexion;
	boolean fin = false;
	int cantConexiones = 0;
	InetAddress ip1, ip2;
	int p1, p2;
	private int cantClientes = 0;
	private int maxClientes = 2;
	private DireccionRed[] clientes = new DireccionRed[maxClientes];
	

	public HiloServidor() {
		try {
			conexion = new DatagramSocket(3333);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (!fin) {
			byte[] datos = new byte[1024];
			DatagramPacket paquete = new DatagramPacket(datos, datos.length);
			try {
				conexion.receive(paquete);
				procesarMensaje(paquete);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void procesarMensaje(DatagramPacket paquete) {
		String msg = new String(paquete.getData()).trim();
		System.out.println(msg);

		if(msg.equals("Conectar")) {
			if(cantClientes<2) {
				this.clientes[cantClientes] = new DireccionRed(paquete.getAddress(),paquete.getPort());
				enviarMensaje("ConexionAceptada!"+(cantClientes+1), paquete.getAddress(), paquete.getPort());
				cantClientes++;
				if(cantClientes==2) {
					Utiles.listener.empezar();
					enviarATodos("Empieza");
				}
			}
		}
		if(cantClientes==2) {
			int nroPlayer = obtenerNroPlayer(paquete.getAddress(), paquete.getPort());
					
			//System.out.println(nroPlayer);
			if(msg.equals("ApretoArriba")) {
				Utiles.listener.apretoTecla(nroPlayer,"Arriba");
			}
			
			if(msg.equals("ApretoIzquierda")) {
				Utiles.listener.apretoTecla(nroPlayer,"Izquierda");
			}
			
			if(msg.equals("ApretoDerecha")) {
				Utiles.listener.apretoTecla(nroPlayer,"Derecha");
			}
			
			if(msg.equals("DejoApretarArriba")) {
				Utiles.listener.soltoTecla(nroPlayer,"Arriba");
			}  
			
			if(msg.equals("DejoApretarIzquierda")) {
				Utiles.listener.soltoTecla(nroPlayer,"Izquierda");
			}
			
			if(msg.equals("DejoApretarDerecha")) {
				Utiles.listener.soltoTecla(nroPlayer,"Derecha");
			}
		}
	}

	private int obtenerNroPlayer(InetAddress address, int port) {
		boolean fin = false;
		int i = 0;
		do {
			if(address.equals(this.clientes[i].getIp())&&(port == this.clientes[i].getPuerto())) {
				fin = true;
			}
			i++;
			if(i==this.clientes.length) {
				fin = true;
			}
		}while(!fin);
		return i;
	}
	
	void enviarMensaje(String msg, InetAddress ipDestino, int puerto) {
		byte[] data = msg.getBytes();
		try {
			DatagramPacket paquete = new DatagramPacket(data, data.length, ipDestino, puerto);
			conexion.send(paquete);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void enviarATodos(String msg) {
		for (int i = 0; i < clientes.length; i++) {
			enviarMensaje(msg, clientes[i].getIp(), clientes[i].getPuerto());
		}
	}
}
