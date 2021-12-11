package com.MarioBros.sprites.tileObject;

import com.MarioBros.game.MarioBrosServer;
import com.MarioBros.scenes.Hud;
import com.MarioBros.screens.PlayScreen;
import com.MarioBros.sprites.Mario;
import com.MarioBros.sprites.items.ItemDef;
import com.MarioBros.sprites.items.Mushroom;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;

public class Coin extends InteractiveTileObject {
    private static TiledMapTileSet tileSet;
    private final int BLANK_COIN = 28;

    public Coin(PlayScreen screen, MapObject object){
        super(screen, object);
        tileSet = map.getTileSets().getTileSet("tileset_gutter");
        fixture.setUserData(this);
        setCategoryFilter(MarioBrosServer.COIN_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        if(getCell().getTile().getId() == BLANK_COIN)
            MarioBrosServer.manager.get("audio/sounds/bump.wav", Sound.class).play();
        else {
            if(object.getProperties().containsKey("mushroom")) {
                screen.spawnItem(new ItemDef(new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioBrosServer.PPM),
                        Mushroom.class));
                MarioBrosServer.manager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
            }
            else
                MarioBrosServer.manager.get("audio/sounds/coin.wav", Sound.class).play();
            getCell().setTile(tileSet.getTile(BLANK_COIN));
            Hud.addScore(100);
        }
    }
}
