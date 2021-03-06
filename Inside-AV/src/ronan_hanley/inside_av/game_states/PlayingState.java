package ronan_hanley.inside_av.game_states;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.particles.ConfigurableEmitter;
import org.newdawn.slick.particles.ParticleIO;
import org.newdawn.slick.particles.ParticleSystem;
import org.newdawn.slick.state.StateBasedGame;

import ronan_hanley.inside_av.InsideAV;
import ronan_hanley.inside_av.Level;
import ronan_hanley.inside_av.QuadraticDamageSource;
import ronan_hanley.inside_av.enemy.Enemy;
import ronan_hanley.inside_av.weapons_systems.BulletWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.LaserWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.Mortar;
import ronan_hanley.inside_av.weapons_systems.MortarWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.Projectile;
import ronan_hanley.inside_av.weapons_systems.Rocket;
import ronan_hanley.inside_av.weapons_systems.RocketWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.Tier1BulletWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.Tier1LaserWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.Tier1MortarWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.Tier1RocketWeaponSystem;
import ronan_hanley.inside_av.weapons_systems.WeaponSystem;
import ronan_hanley.inside_av.weapons_systems.WeaponSystemGrid;

/**
 * The main game state. Used for playing, the tutorial, and anything
 * else in the Substate class.
 * 
 * This class is sort of the centre to all the classes in this project.
 * @author Ronan
 */
public final class PlayingState extends InsideAVState {
	private static final int NUM_LEVELS = 3;
	// the level the player is currently on
	private int level;
	private Level currentLevel;
	private ArrayList<Enemy> enemies;
	// The player's money. Currency is bitcoin.
	private static final double STARTING_PLAYER_MONEY = 150;
	private double playerMoney;
	/* The health of the computer system; enemies apply damage when
	 * they reach the end point. If this reaches 0, it's game over.
	 */
	private double systemHealth;
	private int substate;
	private Image weaponWheel;
	private boolean selectingWeapon = false;
	private int weaponWheelX, weaponWheelY;
	private static final String[] TUTORIAL_TEXT =  
		{"Stop the enemies from reaching the\nend",
	     "Click a tile to place a weapon\nsystem there",
	     "Hold shift to upgrade your weapons",
	     "Press space to start the next wave",
	     "Good luck!\n\n(Press enter to close this tutorial)"};
	private WeaponSystemGrid weapons;
	private ArrayList<ParticleSystem> particleSystems;
	private ParticleSystem explosionSystem;
	private Sound explosionSound;
	private Queue<QuadraticDamageSource> explosionQueue;
	/* true if text on the screen telling the player to progress
	 * to the next level when their ready, is shown. 
	 */
	private boolean nextLevelPromptShowing = false;
	// used to draw a rectangle on the grid where the mouse is
	private int mouseX, mouseY;
	// used for input polling
	private Input input;
	
	public PlayingState(StateBasedGame sbg) {
		super(sbg);
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		input = container.getInput();
		
		substate = Substate.TUTORIAL;
		level =	1;
		systemHealth = 1000;
		playerMoney = STARTING_PLAYER_MONEY;
		
		if (InsideAV.DEBUG) {
			playerMoney = 10000;
		}
		
		currentLevel = new Level(level);
		enemies = new ArrayList<Enemy>();
		weapons = new WeaponSystemGrid(InsideAV.SCREEN_TILES_X, InsideAV.SCREEN_TILES_Y);
		weaponWheel = new Image("res/images/weapons/weapon_wheel.png", false, Image.FILTER_NEAREST);
		
		// Load particle systems
		try {
			explosionSystem = ParticleIO.loadConfiguredSystem(
				"res/particle_systems/explosion_particle_system.xml");
			ParticleSystem.setRelativePath("res/images/particles/");
		} catch (IOException e) {
			e.printStackTrace();
		}
		particleSystems = new ArrayList<ParticleSystem>();
		
		explosionSound = new Sound("res/sound/sfx/explosion.ogg");
		explosionQueue = new LinkedList<QuadraticDamageSource>();
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
									QuadraticDamageSource explosion =
										new QuadraticDamageSource(projectile.getX(),
																  projectile.getY(),
																  projectile.getDamage(),
																  InsideAV.TILE_SIZE * 3);
									queueExplosion(explosion);
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
				
				// check for mortars that have reached their destination
				ArrayList<Mortar> mortarsToProcess = new ArrayList<Mortar>();
				for (WeaponSystem weapon : weapons.getWeapons()) {
					for (Projectile projectile : weapon.getProjectiles()) {
						if (projectile instanceof Mortar) {
							Mortar mortar = (Mortar) projectile;
							
							if (mortar.reachedDestination()) {
								mortarsToProcess.add(mortar);
							}
						}
					}
				}
				
				outerLoop:
				for (Mortar mortar : mortarsToProcess) {
					QuadraticDamageSource explosion =
						new QuadraticDamageSource(mortar.getX(), mortar.getY(),
												  mortar.getDamage(),
												  InsideAV.TILE_SIZE * 4);
					queueExplosion(explosion);
					
					for (WeaponSystem weapon : weapons.getWeapons()) {
						if (weapon.getProjectiles().contains(mortar)) {
							weapon.getProjectiles().remove(mortar);
							continue outerLoop;
						}
					}
				}
			} else {
				// wave not active
				
				nextLevelPromptShowing = currentLevel.isLevelFinished();
				
				/* remove all projectiles, if there are any
				 * (clean up the previous wave)
				 */
				for (WeaponSystem weapon : weapons.getWeapons()) {
					while (weapon.getProjectiles().size() > 0) {
						weapon.getProjectiles().remove(weapon.getProjectiles().size() - 1);
					}
				}
			}
			
			// execute all explosions in the queue
			executeAllExplosions();
			
			// update all particles
			for (ParticleSystem particleSystem : particleSystems) {
				particleSystem.update(delta);
			}
			
			// check for game over
			if (systemHealth <= 0) {
				substate = Substate.GAME_OVER;
				currentLevel.stopMusic();
			}
			break;
		}
	}

	@Override
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		g.scale(InsideAV.screenScale, InsideAV.screenScale);
		
		// used for drawing strings
		int cursorX, cursorY;
		
		switch (substate) {
		case Substate.PLAYING:
			currentLevel.render(g);
			
			if (currentLevel.isWaveActive()) {
				// render all enemies
				for (Enemy enemy : enemies)
					enemy.render(g);
			}
			
			// render weapons
			weapons.renderAll(g);
			
			// render all particles
			for (ParticleSystem particleSystem : particleSystems) {
				particleSystem.render();
			}
			
			// draw rectangle over info panel to hide anything underneath
			// (eg. stray bullets)
			g.setColor(Color.black);
			g.fillRect(InsideAV.STATUS_PANEL_START, 0,
					   InsideAV.STATUS_PANEL_SIZE, InsideAV.SCREEN_HEIGHT);
			
			if (selectingWeapon) {
				weaponWheel.draw(weaponWheelX - InsideAV.TILE_SIZE, weaponWheelY - InsideAV.TILE_SIZE);
				
				// draw weapon prices
				final int PRICE_SCALE = 2;
				int xOffset = InsideAV.HALF_TILE_SIZE * 3;
				int yOffset = InsideAV.TILE_SIZE + (InsideAV.font.getCharHeight() * PRICE_SCALE);
				
				double[] prices = {BulletWeaponSystem.COST,
						   LaserWeaponSystem.COST,
						   MortarWeaponSystem.COST,
						   RocketWeaponSystem.COST};
				
				int priceCounter = 0;
				for (int yOff = -1; yOff <= 1; yOff += 2) {
					for (int xOff = -1; xOff <= 1; xOff += 2) {
						int priceX = weaponWheelX + (xOff * xOffset);
						int priceY = weaponWheelY + (yOff * yOffset);
						
						// make price green if player can afford, or red if cannot
						Color priceColor;
						if (playerMoney >= prices[priceCounter]) {
							priceColor = Color.green;
						} else {
							priceColor = Color.red;
						}
						
						InsideAV.font.drawString(Double.toString(prices[priceCounter++]),
												 priceX,
												 priceY,
												 priceColor,
												 PRICE_SCALE,
												 true, g);
					}
				}
			}
			
			// render money amount
			final int PANEL_TEXT_SCALE = 3;
			int panelCursorY = PANEL_TEXT_SCALE;
			InsideAV.font.drawString(String.format("Money:%.1f BTC", playerMoney),
				InsideAV.STATUS_PANEL_START + PANEL_TEXT_SCALE,
				panelCursorY,
				Color.white,
				2,
				false, g);
			
			// render system health			
			panelCursorY += InsideAV.font.getCharHeight() * PANEL_TEXT_SCALE;
			InsideAV.font.drawString(String.format("Health:%.1f", systemHealth),
					InsideAV.STATUS_PANEL_START + PANEL_TEXT_SCALE,
					panelCursorY,
					Color.white,
					2,
					false, g);
			
			// render wave started text if necessary
			if (currentLevel.isWaveActive() && currentLevel.getWaveTimer() < (3 * InsideAV.FPS)) {
				InsideAV.font.drawString("Malware payload incoming!!",
					 InsideAV.SCREEN_WIDTH / 2,
					 InsideAV.SCREEN_HEIGHT /2 - InsideAV.font.getCharHeight() * 2,
					 Color.red,
					 4,
					 true, g);
			}
			
			// render next level prompt if necessary
			if (nextLevelPromptShowing) {
				InsideAV.font.drawString(String.format("Level complete! Press space to continue to level %d.",
					currentLevel.getLevelNumber() + 1),
					InsideAV.SCREEN_WIDTH / 2,
					InsideAV.SCREEN_HEIGHT /2 - InsideAV.font.getCharHeight() * 1,
					Color.yellow,
					2,
					true, g);
			}
			
			if (input.isKeyDown(Input.KEY_LSHIFT) && !currentLevel.isWaveActive()) {
				// display weapon upgrade overlay
				InsideAV.font.drawString("Click a Weapon To Upgrade It",
					InsideAV.GAME_SCREEN_WIDTH / 2,
					2,
					Color.white,
					2,
					true, g);
				
				for (WeaponSystem weapon : weapons.getWeapons()) {
					double upgradeCost = weapon.getUpgradeCost();
					if (upgradeCost == -1) {
						// upgrade not available (weapon is already upgraded fully)
						continue;
					}
					
					Color priceColor = (upgradeCost <= playerMoney ? Color.green : Color.red);
					InsideAV.font.drawString(Double.toString(upgradeCost),
						weapon.getX() + InsideAV.TILE_SIZE / 2,
						weapon.getY(),
						priceColor,
						1,
						true, g);
				}
			}
			
			if (!selectingWeapon && mouseX < InsideAV.GAME_SCREEN_WIDTH) {
				/* draw a square where the mouse is, so the player
				 * knows what the grid looks like.
				 * 
				 * taking advantage of integer truncation.
				 */
				g.setColor(Color.red);
				g.drawRect((mouseX / InsideAV.TILE_SIZE) * InsideAV.TILE_SIZE,
					(mouseY / InsideAV.TILE_SIZE) * InsideAV.TILE_SIZE,
					InsideAV.TILE_SIZE,
					InsideAV.TILE_SIZE);
			}
			
			// render start next wave prompt
			if (!nextLevelPromptShowing && !currentLevel.isWaveActive()) {
				InsideAV.font.drawString("Press space to\nstart the next\nwave",
					InsideAV.STATUS_PANEL_START + InsideAV.STATUS_PANEL_SIZE / 2,
					InsideAV.SCREEN_HEIGHT - InsideAV.font.getCharHeight() * 2 * 3,
					Color.yellow,
					2,
					true, g);
			}
			break;
		case Substate.TUTORIAL:
			g.setColor(Color.black);
			g.fillRect(0, 0, InsideAV.SCREEN_WIDTH, InsideAV.SCREEN_HEIGHT);
			
			final int TUT_TEXT_SCALE = 3;
			cursorX = InsideAV.TILE_SIZE + 2;
			cursorY = InsideAV.TILE_SIZE + 2;
			for (int i = 0; i < TUTORIAL_TEXT.length; ++i) {
				InsideAV.font.drawString(TUTORIAL_TEXT[i],
					cursorX,
					cursorY,
					Color.green,
					TUT_TEXT_SCALE,
					false, g);
				cursorY += (TUT_TEXT_SCALE * InsideAV.font.getCharHeight()) * 3;
			}
			break;
		case Substate.GAME_OVER:
			g.setColor(Color.black);
			g.fillRect(0, 0, InsideAV.SCREEN_WIDTH, InsideAV.SCREEN_HEIGHT);
			
			cursorY = 100;
			InsideAV.font.drawString("Game Over!",
				InsideAV.SCREEN_WIDTH / 2,
				cursorY,
				Color.red,
				10,
				true, g);
			cursorY += (InsideAV.font.getCharHeight() + 10) * 10;
			InsideAV.font.drawString("Press enter to try again",
				InsideAV.SCREEN_WIDTH / 2,
				cursorY,
				Color.white,
				4,
				true, g);
			cursorY += (InsideAV.font.getCharHeight() + 1) * 6;
			InsideAV.font.drawString("Maybe you should actually apply yourself this time",
				InsideAV.SCREEN_WIDTH / 2,
				cursorY,
				Color.white,
				1, true, g);
			break;
		case Substate.GAME_WON:
			g.setColor(Color.black);
			g.fillRect(0, 0, InsideAV.SCREEN_WIDTH, InsideAV.SCREEN_HEIGHT);
			
			cursorY = 100;
			InsideAV.font.drawString("You win!",
				InsideAV.SCREEN_WIDTH / 2,
				cursorY,
				Color.green,
				10,
				true, g);
			cursorY += (InsideAV.font.getCharHeight() + 10) * 10;
			InsideAV.font.drawString("Good job.",
				InsideAV.SCREEN_WIDTH / 2,
				cursorY,
				Color.white,
				6,
				true, g);
			cursorY += (InsideAV.font.getCharHeight() + 1) * 6;
			InsideAV.font.drawString("Maybe it's time to go outside...",
				InsideAV.SCREEN_WIDTH / 2,
				cursorY,
				Color.white,
				1,
				true, g);
			
			break;
		}
	}
	
	@Override
	public void mousePressed(int button, int x, int y) {
		// don't allow the player to buy weapons after the level is over
		if (nextLevelPromptShowing) return;
		
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
				int wepX = weaponWheelX / InsideAV.TILE_SIZE;
				int wepY = weaponWheelY / InsideAV.TILE_SIZE;
				
				if (offsetX < 0) {
					if (offsetY < 0) {
						weapon = new Tier1BulletWeaponSystem(wepX, wepY);
					} else {
						weapon = new Tier1MortarWeaponSystem(wepX, wepY, enemies);
					}
				} else {
					if (offsetY < 0) {
						weapon = new Tier1LaserWeaponSystem(wepX, wepY, enemies);
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
			// check if the mouse is outside the level area
			if (x >= InsideAV.STATUS_PANEL_START) return;
			
			int tileX = x / InsideAV.TILE_SIZE;
			int tileY = y / InsideAV.TILE_SIZE;
			
			if (currentLevel.isWaveActive() == false) {
				// weapon purchasing or upgrading
				
				// not selecting weapon yet
				
				// check if the player clicked a wall
				if (currentLevel.solidAt(tileX, tileY) == false) return;
				
				if (input.isKeyDown(Input.KEY_LSHIFT) && weapons.tileHasWeapon(tileX, tileY)) {
					// weapon upgrades
					WeaponSystem oldWeapon = weapons.getWeaponAt(tileX, tileY);
					double upgradeCost = oldWeapon.getUpgradeCost();
					
					WeaponSystem upgradedWeapon = oldWeapon.getUpgradedWeapon();
					
					if (upgradedWeapon != null && upgradeCost <= playerMoney) {
						weapons.upgradeWeaponAt(tileX, tileY);
						playerMoney -= upgradeCost;
					}
				} else {
					// weapon select
					weaponWheelX = x;
					weaponWheelY = y;
					
					selectingWeapon = true;
				}
			}
		}
	}
	
	@Override
	public void mouseMoved(int oldX, int oldY, int newX, int newY) {
		// update mouse location
		mouseX = newX / InsideAV.screenScale;
		mouseY = newY / InsideAV.screenScale;
	}
	
	@Override
	public void keyPressed(int key, char c) {
		switch (substate) {
		case Substate.PLAYING:
			if (key == Input.KEY_SPACE) {
				// start the next wave, if it can be started
				if (!currentLevel.isWaveActive()) {
					nextLevelPromptShowing = false;
					
					if (currentLevel.isLevelFinished()) {
						if (currentLevel.getLevelNumber() >= NUM_LEVELS) {
							// all levels finished; game is won
							substate = Substate.GAME_WON;
							return;
						}
						
						// load the new level
						currentLevel = new Level(currentLevel.getLevelNumber() + 1);
						
						// remove all weapon systems
						weapons.clearAll();
					} else {
						currentLevel.setWaveActive(true);
					}
				}
			}
			break;
		case Substate.TUTORIAL:
			if (key == Input.KEY_ENTER) {
				substate = Substate.PLAYING;
				currentLevel.loopMusic();
			}
			break;
		case Substate.GAME_OVER:
			if (key == Input.KEY_ENTER) {
				// restart the game
				substate = Substate.PLAYING;
				reset();
			}
		}
	}
	
	@Override
	public int getID() {
		return 1;
	}
	
	private void applySystemDamage(double damage) {
		systemHealth -= damage;
	}
	
	/**
	 * Resets the game back to how it was when the game started.
	 * 
	 * (useful after a game over)
	 */
	public void reset() {
		weapons.clearAll();
		systemHealth = 1000;
		playerMoney = STARTING_PLAYER_MONEY;
		
		if (InsideAV.DEBUG) {
			playerMoney = 10000;
		}
		
		currentLevel = new Level(level = 1);
		currentLevel.loopMusic();
		enemies = new ArrayList<Enemy>();
		particleSystems = new ArrayList<ParticleSystem>();
		explosionQueue = new LinkedList<QuadraticDamageSource>();
	}
	
	/**
	 * Add an explosion to be executed on the next game tick.
	 * @param explosion
	 */
	public void queueExplosion(QuadraticDamageSource explosion) {
		explosionQueue.add(explosion);
	}
	
	public void executeAllExplosions() throws SlickException {
		while (!explosionQueue.isEmpty()) {
			QuadraticDamageSource explosion = explosionQueue.poll();
			
			// apply damage to all enemies
			for (Enemy enemy : enemies) {
				explosion.damageEnemy(enemy);
			}
			
			// add particle emitter
			ParticleSystem newPs = explosionSystem.duplicate();
			newPs.setPosition(explosion.getX(), explosion.getY());
			ConfigurableEmitter emitter = (ConfigurableEmitter) newPs.getEmitter(0);
			newPs.addEmitter(emitter);
			particleSystems.add(newPs);
			
			explosionSound.play(1f, 0.02f);
		}
	}
	
}
