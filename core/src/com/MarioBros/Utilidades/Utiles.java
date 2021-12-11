package com.MarioBros.Utilidades;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

import com.MarioBros.interfaces.JuegoEventListener;

public final class Utiles {
	
	public static Random r = new Random();
	public static Scanner s = new Scanner(System.in);

	public static JuegoEventListener listener;
	
	public static ArrayList<EventListener> listeners = new ArrayList();
	
	public static void delay(long milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static int ingresarEntero(int min, int max) {
        boolean error = false;
        int valor = 0;
        do {
            error = false;
            try {
                valor = s.nextInt();
                if((valor<min)||(valor>max)) {
                    error = true;
                    System.out.println("Error. Debe ingresar un numero entre " + min + " y " + max);
                }
//            }catch(InputMismatchException e) {
//                
//                error = true;
//                String aux = s.nextLine();
//                for (int i = 0; i < Utiles.listeners.size(); i++) {
//					try {
//                	((TrucosEventListener)Utiles.listeners.get(i)).verificarTruco(aux);
//					} catch(Exception e2) {System.out.println("Tipo de dato mal ingresado. Vuelva a intentar");}
//				}
            }catch(Exception e) {
                System.out.println("Error desconocido");
                error = true;
                s.nextLine();
            }
        }while(error);
        s.nextLine();
        return valor;
    }
	
	public static float ingresarDecimal(float min, float max) {
        boolean error = false;
        float valor = 0;
        do {
            error = false;
            try {
                valor = s.nextFloat();
                if((valor<min)||(valor>max)) {
                    error = true;
                    System.out.println("Error. Debe ingresar un numero entre " + min + " y " + max);
                }
            }catch(InputMismatchException e) {
                System.out.println("Tipo de dato mal ingresado. Vuelva a intentar");
                error = true;
                s.nextLine();
            }catch(Exception e) {
                System.out.println("Error desconocido");
                error = true;
                s.nextLine();
            }
        }while(error);
        s.nextLine();
        return valor;
    }
	
	public static String formatearCadena(String cadena) {
		String nombre = cadena.toLowerCase();
		return nombre.substring(0,1).toUpperCase() + nombre.substring(1);
	}

}
