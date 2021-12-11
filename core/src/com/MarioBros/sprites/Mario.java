package com.MarioBros.sprites;

import com.MarioBros.game.MarioBrosServer;
import com.MarioBros.screens.GameOverScreen;
import com.MarioBros.screens.PlayScreen;
import com.MarioBros.sprites.Mario.State;
import com.MarioBros.sprites.enemies.Enemy;
import com.MarioBros.sprites.enemies.Turtle;
//import com.MarioBros.sprites.other.FireBall;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class Mario extends Sprite {
	public enum State {
		FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD
	};

	public boolean fin = false;
	
	public State currentState;
	public State previousState;

	public World world;
	public Body b2body;

	private TextureRegion marioStand;
	private Animation marioRun;
	private TextureRegion marioJump;
	private TextureRegion marioDead;
	private TextureRegion bigMarioStand;
	private TextureRegion bigMarioJump;
	private Animation bigMarioRun;
	private Animation growMario;

	private float stateTimer;
	private boolean runningRight;
	private boolean marioIsBig;
	private boolean runGrowAnimation;
	private boolean timeToDefineBigMario;
	private boolean timeToRedefineMario;
	private boolean marioIsDead;
	private boolean llegoSalida;

	private PlayScreen screen;

	public boolean mueveArriba,mueveIzquierda,mueveDerecha;

//    private Array<FireBall> fireballs;

	public Mario(PlayScreen screen) {
		this.screen = screen;
		this.world = screen.getWorld();
		currentState = State.STANDING;
		previousState = State.STANDING;
		stateTimer = 0;
		runningRight = true;
		llegoSalida = false;

		Array<TextureRegion> frames = new Array<TextureRegion>();

		for (int i = 1; i < 4; i++)
			frames.add(new TextureRegion(screen.getAtlas().findRegion("little_mario"), i * 16, 0, 16, 16));
		marioRun = new Animation(0.1f, frames);

		frames.clear();

		for (int i = 1; i < 4; i++)
			frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), i * 16, 0, 16, 32));
		bigMarioRun = new Animation(0.1f, frames);

		frames.clear();

		frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
		frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
		frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
		frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
		growMario = new Animation(0.2f, frames);

		marioJump = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 80, 0, 16, 16);
		bigMarioJump = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 80, 0, 16, 32);

		marioStand = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 0, 0, 16, 16);
		bigMarioStand = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32);

		marioDead = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 96, 0, 16, 16);

		defineMario();

		setBounds(0, 0, 16 / MarioBrosServer.PPM, 16 / MarioBrosServer.PPM);
		setRegion(marioStand);

//        fireballs = new Array<FireBall>();

	}

	public void update(float dt) {

		if (screen.getHud().isTimeUp() && !isDead()) {
			die();
		}

		if (marioIsBig)
			setPosition(b2body.getPosition().x - getWidth() / 2,
					b2body.getPosition().y - getHeight() / 2 - 6 / MarioBrosServer.PPM);
		else
			setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
		setRegion(getFrame(dt));
		if (timeToDefineBigMario)
			defineBigMario();
		if (timeToRedefineMario)
			redefineMario();

		/*
		 * for(FireBall ball : fireballs) { ball.update(dt); if(ball.isDestroyed())
		 * fireballs.removeValue(ball, true); }
		 */

	}

	public TextureRegion getFrame(float dt) {
		currentState = getState();

		TextureRegion region;

		switch (currentState) {
		case DEAD:
			region = marioDead;
			break;
		case GROWING:
			region = (TextureRegion) growMario.getKeyFrame(stateTimer);
			if (growMario.isAnimationFinished(stateTimer)) {
				runGrowAnimation = false;
			}
			break;
		case JUMPING:
			region = marioIsBig ? bigMarioJump : marioJump;
			break;
		case RUNNING:
			region = (TextureRegion) (marioIsBig ? bigMarioRun.getKeyFrame(stateTimer, true)
					: marioRun.getKeyFrame(stateTimer, true));
			break;
		case FALLING:
		case STANDING:
		default:
			region = marioIsBig ? bigMarioStand : marioStand;
			break;
		}

		if ((b2body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
			region.flip(true, false);
			runningRight = false;
		}

		else if ((b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
			region.flip(true, false);
			runningRight = true;
		}

		stateTimer = currentState == previousState ? stateTimer + dt : 0;

		previousState = currentState;
		return region;

	}

	public State getState() {
		if (marioIsDead)
			return State.DEAD;
		else if (runGrowAnimation)
			return State.GROWING;
		else if ((b2body.getLinearVelocity().y > 0 && currentState == State.JUMPING)
				|| (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING))
			return State.JUMPING;
		else if (b2body.getLinearVelocity().y < 0)
			return State.FALLING;
		else if (b2body.getLinearVelocity().x != 0)
			return State.RUNNING;
		else
			return State.STANDING;
	}

	public void grow() {
		if (!isBig()) {
			runGrowAnimation = true;
			marioIsBig = true;
			timeToDefineBigMario = true;
			setBounds(getX(), getY(), getWidth(), getHeight() * 2);
			MarioBrosServer.manager.get("audio/sounds/powerup.wav", Sound.class).play();
		}
	}

	public void die() {

		if (!isDead()) {

			MarioBrosServer.manager.get("audio/music/mario_music.ogg", Music.class).stop();
			MarioBrosServer.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
			marioIsDead = true;
			Filter filter = new Filter();
			filter.maskBits = MarioBrosServer.NOTHING_BIT;

			for (Fixture fixture : b2body.getFixtureList()) {
				fixture.setFilterData(filter);
			}

			b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
		}
	}

	public boolean isDead() {
		return marioIsDead;
	}

	public float getStateTimer() {
		return stateTimer;
	}

	public boolean isBig() {
		return marioIsBig;
	}

	public void jump() {
		if (currentState != State.JUMPING) {
			b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
			currentState = State.JUMPING;
		}
	}
	
	public void correrDerecha() {
		if (currentState != State.RUNNING) {
			b2body.applyLinearImpulse(new Vector2(0.1f, 0), b2body.getWorldCenter(), true);
			currentState = State.RUNNING;
		}
	}

	public void correrIzquierda() {
		if(currentState != State.RUNNING) {
			b2body.applyLinearImpulse(new Vector2(-0.1f, 0), b2body.getWorldCenter(), true);
			currentState = State.RUNNING;
		}
	}
	
	public void hit(Enemy enemy) {
		if (enemy instanceof Turtle && ((Turtle) enemy).currentState == Turtle.State.STANDING_SHELL)
			((Turtle) enemy)
					.kick(enemy.b2body.getPosition().x > b2body.getPosition().x ? Turtle.KICK_RIGHT : Turtle.KICK_LEFT);
		else {
			if (marioIsBig) {
				marioIsBig = false;
				timeToRedefineMario = true;
				setBounds(getX(), getY(), getWidth(), getHeight() / 2);
				MarioBrosServer.manager.get("audio/sounds/powerdown.wav", Sound.class).play();
			} else {
				die();
			}
		}
	}

	public void redefineMario() {
		Vector2 position = b2body.getPosition();
		world.destroyBody(b2body);

		BodyDef bdef = new BodyDef();
		bdef.position.set(position);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		CircleShape shape = new CircleShape();
		shape.setRadius(6 / MarioBrosServer.PPM);
		fdef.filter.categoryBits = MarioBrosServer.MARIO_BIT;
		fdef.filter.maskBits = MarioBrosServer.GROUND_BIT | MarioBrosServer.COIN_BIT | MarioBrosServer.BRICK_BIT | MarioBrosServer.ENEMY_BIT
				| MarioBrosServer.OBJECT_BIT | MarioBrosServer.ENEMY_HEAD_BIT | MarioBrosServer.ITEM_BIT;

		fdef.shape = shape;
		b2body.createFixture(fdef).setUserData(this);

		EdgeShape head = new EdgeShape();
		head.set(new Vector2(-2 / MarioBrosServer.PPM, 6 / MarioBrosServer.PPM), new Vector2(2 / MarioBrosServer.PPM, 6 / MarioBrosServer.PPM));
		fdef.filter.categoryBits = MarioBrosServer.MARIO_HEAD_BIT;
		fdef.shape = head;
		fdef.isSensor = true;

		b2body.createFixture(fdef).setUserData(this);

		timeToRedefineMario = false;

	}

	public void defineBigMario() {
		Vector2 currentPosition = b2body.getPosition();
		world.destroyBody(b2body);

		BodyDef bdef = new BodyDef();
		bdef.position.set(currentPosition.add(0, 10 / MarioBrosServer.PPM));
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		CircleShape shape = new CircleShape();
		shape.setRadius(6 / MarioBrosServer.PPM);
		fdef.filter.categoryBits = MarioBrosServer.MARIO_BIT;
		fdef.filter.maskBits = MarioBrosServer.GROUND_BIT | MarioBrosServer.COIN_BIT | MarioBrosServer.BRICK_BIT | MarioBrosServer.ENEMY_BIT
				| MarioBrosServer.OBJECT_BIT | MarioBrosServer.ENEMY_HEAD_BIT | MarioBrosServer.ITEM_BIT;

		fdef.shape = shape;
		b2body.createFixture(fdef).setUserData(this);
		shape.setPosition(new Vector2(0, -14 / MarioBrosServer.PPM));
		b2body.createFixture(fdef).setUserData(this);

		EdgeShape head = new EdgeShape();
		head.set(new Vector2(-2 / MarioBrosServer.PPM, 6 / MarioBrosServer.PPM), new Vector2(2 / MarioBrosServer.PPM, 6 / MarioBrosServer.PPM));
		fdef.filter.categoryBits = MarioBrosServer.MARIO_HEAD_BIT;
		fdef.shape = head;
		fdef.isSensor = true;

		b2body.createFixture(fdef).setUserData(this);
		timeToDefineBigMario = false;
	}

	public void defineMario() {
		BodyDef bdef = new BodyDef();
		bdef.position.set(32 / MarioBrosServer.PPM, 32 / MarioBrosServer.PPM);
		bdef.type = BodyDef.BodyType.DynamicBody;
		b2body = world.createBody(bdef);

		FixtureDef fdef = new FixtureDef();
		CircleShape shape = new CircleShape();
		shape.setRadius(6 / MarioBrosServer.PPM);
		fdef.filter.categoryBits = MarioBrosServer.MARIO_BIT;
		fdef.filter.maskBits = MarioBrosServer.GROUND_BIT | MarioBrosServer.COIN_BIT | MarioBrosServer.BRICK_BIT | MarioBrosServer.ENEMY_BIT
				| MarioBrosServer.OBJECT_BIT | MarioBrosServer.ENEMY_HEAD_BIT | MarioBrosServer.ITEM_BIT;

		fdef.shape = shape;
		b2body.createFixture(fdef).setUserData(this);

		EdgeShape head = new EdgeShape();
		head.set(new Vector2(-2 / MarioBrosServer.PPM, 6 / MarioBrosServer.PPM), new Vector2(2 / MarioBrosServer.PPM, 6 / MarioBrosServer.PPM));
		fdef.filter.categoryBits = MarioBrosServer.MARIO_HEAD_BIT;
		fdef.shape = head;
		fdef.isSensor = true;

		b2body.createFixture(fdef).setUserData(this);
	}

	/*
	 * public void fire(){ fireballs.add(new FireBall(screen,
	 * b2body.getPosition().x, b2body.getPosition().y, runningRight ? true :
	 * false)); }
	 */

	public void draw(Batch batch) {
		super.draw(batch);
//        for(FireBall ball : fireballs)
//            ball.draw(batch);
	}
	
	public void llegoSalida() {
		fin = true;
	}
	
	public Boolean isPuedeSalir() {
		return fin;
	}
	
}
