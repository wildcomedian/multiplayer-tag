package platformer.code.gamelogic.player;

import java.awt.Color;
import java.awt.Graphics;

import platformer.code.gameengine.PhysicsObject;
import platformer.code.gameengine.graphics.MyGraphics;
import platformer.code.gameengine.hitbox.RectHitbox;
import platformer.code.gamelogic.Main;
import platformer.code.gamelogic.level.Level;
import platformer.code.gamelogic.tiles.Tile;

public class DummyPlayer extends PhysicsObject{
	private static int id = 0;
	
	public DummyPlayer(float x, float y, Level level) {
		super(x, y, level.getLevelData().getTileSize(), level.getLevelData().getTileSize(), level);
		int offset =(int)(level.getLevelData().getTileSize()*0.1); //hitbox is offset by 10% of the player size.
		this.hitbox = new RectHitbox(this, offset,offset, width -offset, height - offset);
		id++;
	}

	public int getId(){
		return id;
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(Color.BLUE);
		MyGraphics.fillRectWithOutline(g, (int)getX(), (int)getY(), width, height);
		
		if(Main.DEBUGGING) {
			for (int i = 0; i < closestMatrix.length; i++) {
				Tile t = closestMatrix[i];
				if(t != null) {
					g.setColor(Color.RED);
					g.drawRect((int)t.getX(), (int)t.getY(), t.getSize(), t.getSize());
				}
			}
		}
		
		hitbox.draw(g);
	}
	
	public void setPosition(float x, float y, float originalPlayerMovementX, float originalPlayerMovementY) {
		int correctionX;
		int correctionY;
		if (originalPlayerMovementX > 0) {
			correctionX = 15;
		}
		else if (originalPlayerMovementX < 0) {
			correctionX = -15;
		}
		else {
			correctionX = 0;
		}

		if (originalPlayerMovementY > 0) {
			correctionY = 20;
		}
		else if (originalPlayerMovementY < 0) {
			correctionY = -20;
		}
		else {
			correctionY = 0;
		}

		position.x = x + correctionX;
		position.y = y + correctionY;
	}


}
