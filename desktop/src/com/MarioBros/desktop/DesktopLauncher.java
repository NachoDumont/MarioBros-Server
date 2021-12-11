package com.MarioBros.desktop;

import com.MarioBros.Utilidades.Config;
import com.MarioBros.game.MarioBrosServer;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = Config.ANCHO;
		config.height = Config.ALTO;
		config.title = Config.NOMBRE;
		new LwjglApplication(new MarioBrosServer(), config);
	}
}
