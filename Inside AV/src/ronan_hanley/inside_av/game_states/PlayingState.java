package ronan_hanley.inside_av.game_states;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import ronan_hanley.inside_av.InsideAV;
import ronan_hanley.inside_av.Level;
import ronan_hanley.inside_av.enemy.Enemy;
import ronan_hanley.inside_av.weapons_systems.Tier1BulletWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.Tier1LaserWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.Tier1MortarWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.Tier1RocketWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.WeaponSystem;
import ronan_hanley.inside_av.weapons_systems.WeaponSystemGrid;

public final class PlayingState extends InsideAVState {
	// the level the player is currently on
	private int level;
	private Level currentLevel;
	private ArrayList<Enemy> enemies;
	private boolean waveActive = false;
	// The player's money. Currency is bitcoin.
	private double playerMoney;
	/* The health of the computer system; enemies apply damage when
	 * they reach the end point. If this reaches 0, it's game over.
	 */
	private double systemHealth = 1000;
	private int substate;
	private Image weaponWheel;
	private boolean selectingWeapon = false;
	private int weaponWheelX, weaponWheelY;
	private static final String[] TUTORIAL_TEXT =  
		{"- Stop the enemies from\nreaching the end",
	     "- Click a tile to place\na weapon system there",
	     "- Press space to\nstart the next wave",
	     "- Good luck! (press\nenter to close this tutorial)"};
	private WeaponSystemGrid weapons;
	
	public PlayingState(StateBasedGame sbg) {
		super(sbg);
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		substate = Substate.TUTORIAL;
		level =	1;
		playerMoney = 100;
		currentLevel = new Level(level);
		enemies = new ArrayList<Enemy>();
		weapons = new WeaponSystemGrid(InsideAV.SCREEN_TILES_X, InsideAV.SCREEN_TILES_Y);
		weaponWheel = new Image("res/images/weapons/weapon_wheel.png");
	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		switch (substate) {
		case Substate.PLAYING:
			currentLevel.update(enemies);
			
			if (waveActive) {
				// update all enemies
				for (int i = 0; i < enemies.size(); ++i) {
					Enemy enemy = enemies.get(i);
					boolean enemyReachedEnd = enemy.update();
					
					if (enemyReachedEnd) {
						// remove enemy, apply damage to the computer system
						applySystemDamage(enemy.getSystemDamage());
						enemies.remove(i);
					}
				}
			} else {
				// wave not active
			}
			break;
		case Substate.TUTORIAL:
			
			break;
		}
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		g.scale(InsideAV.screenScale, InsideAV.screenScale);
		
		switch (substate) {
		case Substate.PLAYING:
			currentLevel.render(g);
			
			// render weapons
			weapons.renderAll(g);
			
			if (waveActive) {
				// render all enemies
				for (Enemy enemy : enemies)
					enemy.render(g);
			} else {
				// wave not active
				if (selectingWeapon) {
					weaponWheel.draw(weaponWheelX - InsideAV.TILE_SIZE, weaponWheelY - InsideAV.TILE_SIZE);
				}
			}
			
			break;
		case Substate.TUTORIAL:
			int rectStart = InsideAV.TILE_SIZE;
			int rectSize = InsideAV.SCREEN_WIDTH - (rectStart * 2);
			g.setColor(Color.white);
			g.fillRect(rectStart, rectStart, rectSize, rectSize);
			
			final int TUT_TEXT_SCALE = 3;
			int cursorX = rectStart + 2;
			int cursorY = rectStart + 2;
			for (int i = 0; i < TUTORIAL_TEXT.length; ++i) {
				InsideAV.font.drawString(TUTORIAL_TEXT[i], cursorX, cursorY, Color.black, TUT_TEXT_SCALE, false, g);
				cursorY += (TUT_TEXT_SCALE * InsideAV.font.getCharHeight()) * 3;
			}
			break;
		}
	}
	
	@Override
	public void mousePressed(int button, int x, int y) {
		// account for screen scale
		x /= InsideAV.screenScale;
		y /= InsideAV.screenScale;
		
		if (selectingWeapon) {
			int offsetX = (x - weaponWheelX);
			int offsetY = (y - weaponWheelY);
			
			if (Math.abs(offsetX) > InsideAV.TILE_SIZE || Math.abs(offsetY) > InsideAV.TILE_SIZE) {
				// user did not click a weapon; cancel weapon select
				selectingWeapon = false;
			} else {
				// user clicked a weapon
				WeaponSystem weapon = null;
				double wepCost = -1;
				int wepX = weaponWheelX / InsideAV.TILE_SIZE;
				int wepY = weaponWheelY / InsideAV.TILE_SIZE;
				
				if (offsetX < 0) {
					if (offsetY < 0) {
						weapon = new Tier1BulletWeaponSystem(wepX, wepY);
					} else {
						weapon = new Tier1MortarWeaponSystem(wepX, wepY);
					}
				} else {
					if (offsetY < 0) {
						weapon = new Tier1LaserWeaponSystem(wepX, wepY);
					} else {
						weapon = new Tier1RocketWeaponSystem(wepX, wepY);
					}
				}
				
				if (wepCost <= playerMoney) {
					wepCost = weapon.getCost();
					playerMoney -= wepCost;
					weapons.addWeaponSystem(weapon, wepX, wepY);
				}
			}
			
			selectingWeapon = false;
		} else {
			// not selecting weapon yet
			int tileX = x / InsideAV.TILE_SIZE;
			int tileY = y / InsideAV.TILE_SIZE;
			if (weapons.tileHasWeapon(tileX, tileY)) {
				// weapon upgrades
				
			} else {
				// weapon select
				weaponWheelX = x;
				weaponWheelY = y;
			}
			
			selectingWeapon = true;
		}
	}
	
	@Override
	public void mouseReleased(int button, int x, int y) {}
	
	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy) {}
	
	@Override
	public void keyPressed(int key, char c) {
		switch (substate) {
		case Substate.PLAYING:
			
			break;
		case Substate.TUTORIAL:
			switch (key) {
			case Input.KEY_ENTER:
				substate = Substate.PLAYING;
			}
			break;
		}
	}
	
	@Override
	public int getID() {
		return 1;
	}
	
	private void applySystemDamage(double damage) {
		systemHealth -= damage;
	}
	
}
