package com.MarioBros.Utilidades;

import com.MarioBros.game.MarioBrosServer;
import com.MarioBros.sprites.Mario;
import com.MarioBros.sprites.enemies.Enemy;
import com.MarioBros.sprites.items.Item;
import com.MarioBros.sprites.tileObject.InteractiveTileObject;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class WorldContactListener implements ContactListener {
	@Override
	public void beginContact(Contact contact) {
		Fixture fixA = contact.getFixtureA();
		Fixture fixB = contact.getFixtureB();

		int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

		switch (cDef) {
		case MarioBrosServer.MARIO_HEAD_BIT | MarioBrosServer.BRICK_BIT:
		case MarioBrosServer.MARIO_HEAD_BIT | MarioBrosServer.COIN_BIT:
			if (fixA.getFilterData().categoryBits == MarioBrosServer.MARIO_HEAD_BIT)
				((InteractiveTileObject) fixB.getUserData()).onHeadHit((Mario) fixA.getUserData());
			else
				((InteractiveTileObject) fixA.getUserData()).onHeadHit((Mario) fixB.getUserData());
			break;
		case MarioBrosServer.ENEMY_HEAD_BIT | MarioBrosServer.MARIO_BIT:
			if (fixA.getFilterData().categoryBits == MarioBrosServer.ENEMY_HEAD_BIT)
				((Enemy) fixA.getUserData()).hitOnHead((Mario) fixB.getUserData());
			else
				((Enemy) fixB.getUserData()).hitOnHead((Mario) fixA.getUserData());
			break;
		case MarioBrosServer.ENEMY_BIT | MarioBrosServer.OBJECT_BIT:
			if (fixA.getFilterData().categoryBits == MarioBrosServer.ENEMY_BIT)
				((Enemy) fixA.getUserData()).reverseVelocity(true, false);
			else
				((Enemy) fixB.getUserData()).reverseVelocity(true, false);
			break;
		case MarioBrosServer.MARIO_BIT | MarioBrosServer.ENEMY_BIT:
			if (fixA.getFilterData().categoryBits == MarioBrosServer.MARIO_BIT)
				((Mario) fixA.getUserData()).hit((Enemy) fixB.getUserData());
			else
				((Mario) fixB.getUserData()).hit((Enemy) fixA.getUserData());
			break;
		case MarioBrosServer.ENEMY_BIT | MarioBrosServer.ENEMY_BIT:
			((Enemy) fixA.getUserData()).hitByEnemy((Enemy) fixB.getUserData());
			((Enemy) fixB.getUserData()).hitByEnemy((Enemy) fixA.getUserData());
			break;
		case MarioBrosServer.ITEM_BIT | MarioBrosServer.OBJECT_BIT:
			if (fixA.getFilterData().categoryBits == MarioBrosServer.ITEM_BIT)
				((Item) fixA.getUserData()).reverseVelocity(true, false);
			else
				((Item) fixB.getUserData()).reverseVelocity(true, false);
			break;
		case MarioBrosServer.ITEM_BIT | MarioBrosServer.MARIO_BIT:
			if (fixA.getFilterData().categoryBits == MarioBrosServer.ITEM_BIT)
				((Item) fixA.getUserData()).use((Mario) fixB.getUserData());
			else
				((Item) fixB.getUserData()).use((Mario) fixA.getUserData());
			break;
//            case MarioBros.FIREBALL_BIT | MarioBros.OBJECT_BIT:
//                if(fixA.getFilterData().categoryBits == MarioBros.FIREBALL_BIT)
//                    ((FireBall)fixA.getUserData()).setToDestroy();
//                else
//                    ((FireBall)fixB.getUserData()).setToDestroy();
//                break;
		}
	}

	@Override
	public void endContact(Contact contact) {
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
}
