package ronan_hanley.inside_av;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import ronan_hanley.inside_av.game_states.MenuState;
import ronan_hanley.inside_av.game_states.PlayingState;

/**
 * A tower defence game, with the theme of malware
 * attacking a computer system.
 * 
 * Project started on 4/10/17.
 * @author Ronan Hanley
 */
public final class InsideAV extends StateBasedGame {
	// Some constants about the basics of the game
	private static final String GAME_NAME = "Inside AV";
	public static final int FPS = 60;
	public static final int TILE_SIZE = 32;
	public static final int HALF_TILE_SIZE = TILE_SIZE /2;
	public static final int STATUS_PANEL_SIZE = TILE_SIZE * 7;
	// Maybe doing these calculations in code is overkill
	// Game window size: 480 x 480
	// Full size: 704 x 480
	public static final int SCREEN_TILES_X = 15;
	public static final int SCREEN_TILES_Y = 15;
	public static final int GAME_SCREEN_WIDTH = SCREEN_TILES_X * TILE_SIZE;
	public static final int SCREEN_WIDTH = GAME_SCREEN_WIDTH + STATUS_PANEL_SIZE;
	public static final int SCREEN_HEIGHT = SCREEN_TILES_Y * TILE_SIZE; 
	public static final int STATUS_PANEL_START = SCREEN_WIDTH - STATUS_PANEL_SIZE;
	// debug mode (diplays some useful figures/collision boxes/etc)
	public static final boolean DEBUG = false;
	public static int screenScale;
	
	public static ImageFont font;
	
	public InsideAV() {
		super(GAME_NAME);
	}
	
	private void init() {
		// initialize game font
		String format =
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,!?:;+-=*#%^&_|/\\~\"'`$@()[]{}<> ";
		try {
			font = new ImageFont(new Image("res/images/computer_font.png", false, Image.FILTER_NEAREST), format, 6, 9);
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void initStatesList(GameContainer container) throws SlickException {
		addState(new MenuState(this));
		addState(new PlayingState(this));
		
		init();
		
		// Enter menu state
		enterState(0);
	}
	
    public static void main(String[] args) {
        try {
        	final int LOGIC_INTERVAL = 1000 / FPS;
        	
            AppGameContainer app = new AppGameContainer(new InsideAV());
            
            // Calculate the window size
            // It will scale to be appropriate the screen size
            
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            
            /* Allow some space on the top/bottom of the window so it
             * doesn't overlap over the taskbar etc.
             */
            screenSize.height -= 50;
            
            screenScale = screenSize.height / SCREEN_HEIGHT;
            final int ADJUSTED_WIDTH = SCREEN_WIDTH * screenScale;
            final int ADJUSTED_HEIGHT = SCREEN_HEIGHT * screenScale;
            app.setDisplayMode(ADJUSTED_WIDTH, ADJUSTED_HEIGHT, false);
            app.setTargetFrameRate(FPS);
            app.setShowFPS(false);
            app.setMaximumLogicUpdateInterval(LOGIC_INTERVAL + 1);
            app.setMaximumLogicUpdateInterval(LOGIC_INTERVAL);
            app.setVSync(true);
            app.setAlwaysRender(true);
            app.setIcons(new String[]
            	{"res/images/game_icon_64.png",
            	 "res/images/game_icon_32.png",
            	 "res/images/game_icon_24.png",
            	 "res/images/game_icon_16.png"});
            app.start();
        } catch(SlickException e) {
            e.printStackTrace();
        }
    }

}
