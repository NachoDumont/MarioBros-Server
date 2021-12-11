package com.MarioBros.screens;

import java.util.concurrent.LinkedBlockingQueue;

import com.MarioBros.Utilidades.B2WorldCreator;
import com.MarioBros.Utilidades.Config;
import com.MarioBros.Utilidades.Recursos;
import com.MarioBros.Utilidades.Render;
import com.MarioBros.Utilidades.Texto;
import com.MarioBros.Utilidades.WorldContactListener;
import com.MarioBros.game.MarioBrosServer;
import com.MarioBros.interfaces.JuegoEventListener;
import com.MarioBros.red.Servidor;
import com.MarioBros.scenes.Hud;
import com.MarioBros.sprites.Mario;
import com.MarioBros.sprites.enemies.Enemy;
import com.MarioBros.sprites.items.Item;
import com.MarioBros.sprites.items.ItemDef;
import com.MarioBros.sprites.items.Mushroom;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class PlayScreen implements Screen, JuegoEventListener {

	private Servidor servidor;
	private boolean empieza = false;
	private int jugador = 0;

	private MarioBrosServer game;
	private TextureAtlas atlas;
	public static boolean alreadyDestroyed = false;

	private OrthographicCamera gamecam;
	private Viewport gamePort;
	private Hud hud;

	private TmxMapLoader maploader;
	private TiledMap map;
	private OrthogonalTiledMapRenderer renderer;

	private Texto espera;

	private World world;
	// Dibuja el contorno de todas las cajas de colision
	private Box2DDebugRenderer b2dr;
	private B2WorldCreator creator;

	private Mario player, player2;

	private Music music;

	private Array<Item> items;
	private LinkedBlockingQueue<ItemDef> itemsToSpawn;

	public PlayScreen(MarioBrosServer game) {

		atlas = new TextureAtlas("Mario_and_Enemies.pack");

		this.game = game;

		gamecam = new OrthographicCamera();

		gamePort = new FitViewport(MarioBrosServer.V_WIDTH / MarioBrosServer.PPM,
				MarioBrosServer.V_HEIGHT / MarioBrosServer.PPM, gamecam);

		hud = new Hud(Render.sb);

		espera = new Texto(Recursos.FUENTE, 40, Color.WHITE, false);
		espera.setTexto("Esperando jugadores");
		espera.setPosition((Config.ANCHO / 2) - (espera.getAncho() / 2), (Config.ALTO / 2) + (espera.getAlto() / 2));

		maploader = new TmxMapLoader();
		map = maploader.load("level1.tmx");
		renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioBrosServer.PPM);

		gamecam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

		world = new World(new Vector2(0, -10), true);

		b2dr = new Box2DDebugRenderer();

		creator = new B2WorldCreator(this);

		player = new Mario(this);
		player2 = new Mario(this);

		world.setContactListener(new WorldContactListener());

		music = MarioBrosServer.manager.get("audio/music/mario_music.ogg", Music.class);
		music.setLooping(true);
		music.setVolume(0.1f);
		music.play();

		items = new Array<Item>();
		itemsToSpawn = new LinkedBlockingQueue<ItemDef>();
		servidor = new Servidor();
	}

	public void spawnItem(ItemDef idef) {
		itemsToSpawn.add(idef);
	}

	public void handleSpawningItems() {
		if (!itemsToSpawn.isEmpty()) {
			ItemDef idef = itemsToSpawn.poll();
			if (idef.type == Mushroom.class) {
				items.add(new Mushroom(this, idef.position.x, idef.position.y));
			}
		}
	}

	public TextureAtlas getAtlas() {
		return atlas;
	}

	@Override
	public void show() {

	}

	public void handleInput(float dt) {
		if (player.mueveArriba) {
			player.jump();
			servidor.enviarATodos("coordenadas!player!" + player.getY() + "!" + player.getX());
		}
		if (player.mueveIzquierda) {
			player.correrIzquierda();
			servidor.enviarATodos("coordenadas!player!" + player.getY()  + "!" + player.getX());
		}
		if(player.mueveDerecha) {
			player.correrDerecha();
			servidor.enviarATodos("coordenadas!player!" + player.getY() + "!" + player.getX());
		}
		if (player2.mueveArriba) {
			player2.jump();
			servidor.enviarATodos("coordenadas!player2!" + player2.getY() + "!" + player2.getX());
		}
		if (player2.mueveIzquierda) {
			player2.correrIzquierda();
			servidor.enviarATodos("coordenadas!player2!" + player2.getY() + "!" + player2.getX());
		}
		if (player2.mueveDerecha) {
			player.correrDerecha();
			servidor.enviarATodos("coordenadas!player2!" + player2.getY() + "!" + player2.getX());
		}
	}

	public void update(float dt) {
		handleInput(dt);
		handleSpawningItems();

		world.step(1 / 60f, 6, 2);

		player.update(dt);
		player2.update(dt);
		for (Enemy enemy : creator.getEnemies()) {
			enemy.update(dt);
			if (enemy.getX() < player.getX() + 224 / MarioBrosServer.PPM) {
				enemy.b2body.setActive(true);
			}
		}

		for (Item item : items)
			item.update(dt);

		hud.update(dt);

		if (player.getY() < 0) {
			player.currentState = Mario.State.DEAD;
		}

		if (player.getX() > 32.88f) {
			player.llegoSalida();
		}

		if (player.currentState != Mario.State.DEAD) {
			gamecam.position.x = player.b2body.getPosition().x;
		}

		if (player2.getY() < 0) {
			player2.currentState = Mario.State.DEAD;
		}

		if (player2.getX() > 32.88f) {
			player2.llegoSalida();
		}

		if (player2.currentState != Mario.State.DEAD) {
			gamecam.position.x = player2.b2body.getPosition().x;
		}

		gamecam.update();

		renderer.setView(gamecam);

	}

	@Override
	public void render(float delta) {
		if (empieza) {
			update(delta);

			Gdx.gl.glClearColor(0, 0, 0, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			renderer.render();

			b2dr.render(world, gamecam.combined);

			Render.sb.setProjectionMatrix(gamecam.combined);
			Render.begin();
				player.draw(Render.sb);
				player2.draw(Render.sb);
				for (Enemy enemy : creator.getEnemies())
					enemy.draw(Render.sb);
				for (Item item : items)
					item.draw(Render.sb);
			Render.end();

			Render.sb.setProjectionMatrix(hud.stage.getCamera().combined);
			hud.stage.draw();

			if (gameOver()) {
				game.setScreen(new GameOverScreen(game));
				dispose();
			}

			if (player.isPuedeSalir() && player2.isPuedeSalir()) {
				game.setScreen(new EndScreen(game));
				dispose();
			}
		} else {
			Render.begin();
				espera.dibujar();
			Render.end();
		}

	}

	public boolean gameOver() {
		if (player.currentState == Mario.State.DEAD && player.getStateTimer() > 3) {
			return true;
		}
		return false;
	}

	@Override
	public void resize(int width, int height) {
		gamePort.update(width, height);

	}

	public TiledMap getMap() {
		return map;
	}

	public World getWorld() {
		return world;
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	@Override
	public void dispose() {
		map.dispose();
		renderer.dispose();
		world.dispose();
		b2dr.dispose();
		hud.dispose();
	}

	public Hud getHud() {
		return hud;
	}

	@Override
	public boolean handle(Event event) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void empezar() {
		empieza = true;
	}

	@Override
	public void apretoTecla(int nroPlayer, String tecla) {
		if (nroPlayer == 1) {
			if (tecla.equals("Arriba")) {
				player.mueveArriba = true;

			}

			if (tecla.equals("Izquierda")) {
				player.mueveIzquierda = true;
			}
			
			if (tecla.equals("Derecha")) {
				player.mueveDerecha = true;
			}
		} else {
			if (tecla.equals("Arriba")) {
				player2.mueveArriba = true;
			}

			if (tecla.equals("Izquierda")) {
				player2.mueveIzquierda = true;
			}
			
			if (tecla.equals("Derecha")) {
				player2.mueveDerecha = true;
			}
		}
	}

	@Override
	public void soltoTecla(int nroPlayer, String tecla) {
		if (nroPlayer == 1) {
			if (tecla.equals("Arriba")) {
				player.mueveArriba = false;

			}

			if (tecla.equals("Izquierda")) {
				player.mueveIzquierda = false;
			}
			
			if (tecla.equals("Derecha")) {
				player.mueveDerecha = false;
			}
		} else {
			if (tecla.equals("Arriba")) {
				player2.mueveArriba = false;
			}

			if (tecla.equals("Izquierda")) {
				player2.mueveIzquierda = false;
			}
			
			if (tecla.equals("Derecha")) {
				player2.mueveDerecha = false;
			}
		}
	}
}
