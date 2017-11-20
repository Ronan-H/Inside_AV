package ronan_hanley.inside_av.game_states;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;

import ronan_hanley.inside_av.Explosion;
import ronan_hanley.inside_av.InsideAV;
import ronan_hanley.inside_av.Level;
import ronan_hanley.inside_av.enemy.Enemy;
import ronan_hanley.inside_av.weapons_systems.Projectile;
import ronan_hanley.inside_av.weapons_systems.Rocket;
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
		playerMoney = 150;
		playerMoney += 10000;
		currentLevel = new Level(level);
		enemies = new ArrayList<Enemy>();
		weapons = new WeaponSystemGrid(InsideAV.SCREEN_TILES_X, InsideAV.SCREEN_TILES_Y);
		weaponWheel = new Image("res/images/weapons/weapon_wheel.png", false, Image.FILTER_NEAREST);
	}
	
	@Override
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		switch (substate) {
		case Substate.PLAYING:
			currentLevel.update(enemies);
			
			if (currentLevel.isWaveActive()) {
				// update all weapons
				weapons.updateAll(enemies);
				
				// update all enemies
				enemyLoop:
				for (int i = 0; i < enemies.size(); ++i) {
					Enemy enemy = enemies.get(i);
					boolean enemyReachedEnd = enemy.update();
					
					if (enemyReachedEnd) {
						// remove enemy, apply damage to the computer system
						applySystemDamage(enemy.getSystemDamage());
						enemy.kill();
						enemies.remove(i);
						--i;
						continue enemyLoop;
					}
					
					// make a list of projectiles to remove later to avoid ConcurrentModificationException
					ArrayList<Projectile> toRemove = new ArrayList<Projectile>();
					// check for any projectile collision on this enemy
					weaponLoop:
					for (WeaponSystem weapon : weapons.getWeapons()) {
						for (Projectile projectile : weapon.getProjectiles()) {
							if (projectile.touchingEnemy(enemy)) {
								if (projectile instanceof Rocket) {
									// create an explosion
									Explosion explosion = new Explosion(projectile.getX(), projectile.getY(),
																		projectile.getDamage(), InsideAV.TILE_SIZE * 4);
									
									// apply damage to all enemies
									for (Enemy enemy2 : enemies) {
										explosion.damageEnemy(enemy2);
									}
								}
								
								toRemove.add(projectile);
								enemy.applyDamage(projectile.getDamage());
								if (enemy.isDead()) break weaponLoop;
							}
						}
					}
					// remove projectiles that have hit an enemy
					for (WeaponSystem weapon : weapons.getWeapons()) {
						weapon.getProjectiles().removeAll(toRemove);
					}
				}
				
				// remove dead enemies
				for (int i = 0; i < enemies.size(); ++i) {
					Enemy enemy = enemies.get(i);
					if (enemy.isDead()) {
						// award the player the kill reward
						playerMoney += enemy.getKillReward();
						
						// remove enemy
						enemy.kill();
						enemies.remove(i);
						--i;
						enemy = null;
					}
				}
			} else {
				// wave not active
				
				/* remove all projectiles, if there are any
				 * (clean up the previous wave)
				 */
				for (WeaponSystem weapon : weapons.getWeapons()) {
					while (weapon.getProjectiles().size() > 0) {
						weapon.getProjectiles().remove(weapon.getProjectiles().size() - 1);
					}
				}
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
			
			if (currentLevel.isWaveActive()) {
				// render all enemies
				for (Enemy enemy : enemies)
					enemy.render(g);
			} else {
				// wave not active
				if (selectingWeapon) {
					weaponWheel.draw(weaponWheelX - InsideAV.TILE_SIZE, weaponWheelY - InsideAV.TILE_SIZE);
				}
			}
			
			// draw rectangle over info panel to hide anything underneath
			// (eg. stray bullets)
			g.setColor(Color.black);
			g.fillRect(InsideAV.STATUS_PANEL_START, 0, InsideAV.STATUS_PANEL_SIZE, InsideAV.SCREEN_HEIGHT);
			
			// render money amount
			final int PANEL_TEXT_SCALE = 3;
			int panelCursorY = PANEL_TEXT_SCALE;
			InsideAV.font.drawString(String.format("Money:%.1f BTC", playerMoney),
				InsideAV.STATUS_PANEL_START + PANEL_TEXT_SCALE, panelCursorY, Color.white, 2, false, g);
			
			// render system health			
			panelCursorY += InsideAV.font.getCharHeight() * PANEL_TEXT_SCALE;
			InsideAV.font.drawString(String.format("Health:%.1f", systemHealth),
					InsideAV.STATUS_PANEL_START + PANEL_TEXT_SCALE, panelCursorY, Color.white, 2, false, g);
			
			// render wave started text if necessary
			if (currentLevel.isWaveActive() && currentLevel.getWaveTimer() < (3 * InsideAV.FPS)) {
				InsideAV.font.drawString("Malware payload incoming!!", InsideAV.SCREEN_WIDTH / 2, InsideAV.SCREEN_HEIGHT /2 - InsideAV.font.getCharHeight() * 2, Color.red, 4, true, g);
			}
			break;
		case Substate.TUTORIAL:
			int rectStart = InsideAV.TILE_SIZE;
			int rectWidth = InsideAV.SCREEN_WIDTH - (rectStart * 2);
			int rectHeight = InsideAV.SCREEN_HEIGHT - (rectStart * 2);
			g.setColor(Color.white);
			g.fillRect(rectStart, rectStart, rectWidth, rectHeight);
			
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
		
		// check if the mouse is outside the level area
		if (x >= InsideAV.STATUS_PANEL_START) return;
		
		if (selectingWeapon) {
			int offsetX = (x - weaponWheelX);
			int offsetY = (y - weaponWheelY);
			
			if (Math.abs(offsetX) > InsideAV.TILE_SIZE || Math.abs(offsetY) > InsideAV.TILE_SIZE) {
				// user did not click a weapon; cancel weapon select
				selectingWeapon = false;
			} else {
				// user clicked a weapon
				WeaponSystem weapon = null;
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
				
				double wepCost = weapon.getCost();
				if (playerMoney >= wepCost) {
					playerMoney -= wepCost;
					weapons.addWeaponSystem(weapon, wepX, wepY);
				}
			}
			
			selectingWeapon = false;
		} else {
			// not selecting weapon yet
			int tileX = x / InsideAV.TILE_SIZE;
			int tileY = y / InsideAV.TILE_SIZE;
			
			// check if the player clicked a wall
			if (!currentLevel.solidAt(tileX, tileY)) return;
			
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
			switch (key) {
			case Input.KEY_SPACE:
				// start the next wave, if it can be started
				if (!currentLevel.isWaveActive()) {
					currentLevel.setWaveActive(true);
				}
				break;
			}
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
