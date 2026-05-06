/*
 * 
 */
package platformer.code.gameengine;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import platformer.code.gameengine.graphics.MyWindow;
import platformer.code.gameengine.input.KeyboardInputManager;
import platformer.code.gameengine.input.MouseInputManager;

/**
 * 
 * @author Paul Kappmeyer & Daniel Lucarz
 *
 */
public abstract class GameBase {
	protected MyWindow window;

	//-----------------------------------------------ABSTRACT METHODS FOR SUB-CLASS
	public abstract void init();
	public abstract void update(float tslf);
	public abstract void draw(Graphics graphics);
	//-----------------------------------------------END ABSTRACT METHODS

	/**
	 * Creates a new window and starts the game loop
	 * @param title The title of the window
	 * @param width The width of the window
	 * @param height The height of the window
	 */
	public void start(String title, int width, int height) {
		synchronized (this) {
			window = new MyWindow(title, width, height);
			
			//Adding inputManagers to window
			window.addKeyListener(new KeyboardInputManager());
			MouseInputManager mouseInputManager = new MouseInputManager(window);
			window.addMouseListener(mouseInputManager);
			window.addMouseMotionListener(mouseInputManager);
			window.addMouseWheelListener(mouseInputManager);
			
		}
			long StartOfInit = System.currentTimeMillis();
			init(); //Calling method init() in the sub-class
			long StartOfGame = System.currentTimeMillis();
			System.out.println("Time needed for initialization: [" + (StartOfGame - StartOfInit) + "ms]");
			
			long lastFrame = System.currentTimeMillis();

		while(true) {
			lastFrame = System.currentTimeMillis();
			while(window.isActive()) {
				//Calculating time since last frame
				long thisFrame = System.currentTimeMillis();
				float tslf = (float)(thisFrame - lastFrame) / 1000f;
				lastFrame = thisFrame;

				update(tslf); //Calling method update() in the sub-class 
				BufferStrategy bs = window.beginDrawing();
				do{
					do{
						Graphics g = bs.getDrawGraphics();
						g.translate(window.getInsetX(), window.getInsetY());
						draw(g); //Calling method draw() in the sub-class
						g.dispose();
					}while(bs.contentsLost());
					bs.show();
				}while(bs.contentsLost());
				
			}
		}
	}
}
