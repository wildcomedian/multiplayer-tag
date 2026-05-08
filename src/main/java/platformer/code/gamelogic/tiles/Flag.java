package platformer.code.gamelogic.tiles;

import java.awt.image.BufferedImage;

import platformer.code.gameengine.hitbox.RectHitbox;
import platformer.code.gamelogic.level.Level;

public class Flag extends Tile{

	public Flag(float x, float y, int size, BufferedImage image, Level level) {
		super(x, y, size, image, false, level);
		hitbox = new RectHitbox(x*size, y*size, 30, 0, size-30, size);
	}

	@Override
	public void update(float tslf) {
		//if(hitbox.isIntersecting(Level.player.getHitbox())) level.onPlayerWin();
	}
	
}
