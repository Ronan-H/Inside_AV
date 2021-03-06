package ronan_hanley.inside_av.weapons_systems;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Sound;

import ronan_hanley.inside_av.InsideAV;
import ronan_hanley.inside_av.RotationalEntity;
import ronan_hanley.inside_av.enemy.Enemy;

public abstract class WeaponSystem extends RotationalEntity {
	private Image sprite;
	protected Enemy target = null;
	// ticks since this weapon picked an enemy to target
	protected int ticksSinceTarget;
	// ticks since this weapon has fired
	protected int ticksSinceFire;
	private ArrayList<Projectile> projectiles;
	
	public WeaponSystem(int x, int y, Image sprite) {
		super(x * InsideAV.TILE_SIZE, y * InsideAV.TILE_SIZE);
		this.sprite = sprite;
		projectiles = new ArrayList<Projectile>();
	}
	
	public void update(ArrayList<Enemy> enemies) {
		if (target == null || target.isDead() || ticksSinceTarget >= getMinTargetTime()) {
			// target the closest enemy
			double shortestDistance = Double.MAX_VALUE;
			for (Enemy enemy : enemies) {
				// using the pythagoras' theorem to get the distance
				double distance = Math.sqrt(Math.pow(getXExact() - enemy.getXExact(), 2)
										  + Math.pow(getYExact() - enemy.getYExact(), 2));
				
				if (distance < shortestDistance) {
					target = enemy;
					shortestDistance = distance;
				}
			}
			
			ticksSinceTarget = 0;
		}
		
		/* calling a method here for the rest of the targeting, as
		 * different subclasses point to the enemy differently
		 * (eg. the bullet weapon is offset slightly because of motion
		 * prediction, the laser weapon is pointed right at the enemy,...) 
		 */
		pointTowardsTarget();
		
		++ticksSinceTarget;
		
		// fire this weapon if it's time to
		if (ticksSinceFire > getFireInterval() && target != null && !target.isDead()) {
			fire();
			ticksSinceFire = 0;
		} else {
			++ticksSinceFire;
		}
		
		// update all projectiles for this weapon
		for (Projectile projectile : projectiles)
			projectile.update();
	}
	
	public void render(Graphics g) {
		// render all projectiles for this weapon
		for (Projectile projectile : projectiles)
			projectile.render(g);
		
		sprite.setRotation((float) Math.toDegrees(getAngle()));
		g.drawImage(sprite, getX(), getY());
	}
	
	public void pointTowardsTarget() {
		if (target == null) return;
		
		// default behaviour: just point right at the enemy, no offset
		
		// use inverse tan to convert 2 points into an angle
		double opp = target.getYExact() - getYExact();
		double adj = target.getXExact() - getXExact();
		setAngle(Math.atan2(opp, adj));
	}
	
	/**
	 * 
	 * @return How much this weapon costs to buy
	 */
	public abstract double getCost();
	
	/**
	 * The minimum amount of time an enemy can be target for.
	 * 
	 * This is to stop a weapon from rapidly switching between enemies.
	 * @return Minimum time, in game ticks
	 */
	public int getMinTargetTime() {
		// 3 seconds
		return 3 * InsideAV.FPS;
	}
	
	public abstract int getFireInterval();
	
	public abstract void fire();
	
	protected void addProjectile(Projectile projectile) {
		projectiles.add(projectile);
	}
	
	public ArrayList<Projectile> getProjectiles() {
		return projectiles;
	}
	
	protected abstract Sound getShootSound();
	
	public void playShootSound() {
		getShootSound().play(1f, 0.01f);
	}
	
	/**
	 * Gets the average x and y coordinate for an enemy, then returns
	 * the enemy closest to that.
	 * @param enemies
	 * @return The closest enemy to the mean point.
	 */
	public static Enemy findCentralEnemy(ArrayList<Enemy> enemies) {
		Enemy centralEnemy;
		
		if (enemies.size() == 0) {
			centralEnemy = null;
		} else {
			double totalX = 0;
			double totalY = 0;
			
			double avgX, avgY;
			
			for (Enemy enemy : enemies) {
				totalX += enemy.getXExact();
				totalY += enemy.getYExact();
			}
			
			avgX = totalX / enemies.size();
			avgY = totalY / enemies.size();
			
			// find the closest enemy to that point
			Enemy closestEnemy = null;
			double shortestDistance = Double.MAX_VALUE;
			double distance;
			for (Enemy enemy : enemies) {
				distance = Math.sqrt(
					Math.pow(enemy.getXExact() - avgX, 2) + Math.pow(enemy.getYExact() - avgY, 2));
				if (distance < shortestDistance) {
					closestEnemy = enemy;
					shortestDistance = distance;
				}
			}
			
			centralEnemy = closestEnemy;
		}
		
		return centralEnemy;
	}
	
	public abstract double getUpgradeCost();
	
	/**
	 * Returns the next tier of this weapon (a new object).
	 * @return The new, upgraded weapon.
	 */
	public abstract WeaponSystem getUpgradedWeapon();
	
}
