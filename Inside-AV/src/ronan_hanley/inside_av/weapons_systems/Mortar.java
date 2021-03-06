package ronan_hanley.inside_av.weapons_systems;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import ronan_hanley.inside_av.InsideAV;
import ronan_hanley.inside_av.enemy.Enemy;

public final class Mortar extends Projectile {
	public static final Image SPRITE;
	// where this mortar was fired from
	private int srcX;
	private int srcY;
	// where this mortar should land
	private int destX;
	private int destY;
	// distance from src to dest
	private double flightDistance;
	private boolean reachedDestination;
	
	static {
		Image sprite = null;
		try {
			sprite = new Image("res/images/weapons/mortar/mortar.png", false, Image.FILTER_NEAREST);
		} catch (SlickException e) {
			e.printStackTrace();
		}
		
		SPRITE = sprite;
	}
	
	public Mortar(int x, int y, double angle, double speed, int destX, int destY) {
		super(x, y, angle, speed, SPRITE);
		
		srcX = x;
		srcY = y;
		
		this.destX = destX;
		this.destY = destY;
		
		// calculate flight distance
		flightDistance = Math.sqrt(Math.pow(destX - srcX, 2) + Math.pow(destY - srcY, 2));
	}
	
	public void update() {
		super.update();
		
		if (!reachedDestination) {
			// check if this mortar has reached it's destination
			double distTravelled = Math.sqrt(
				Math.pow(srcX - getXExact(), 2) + Math.pow(srcY - getYExact(), 2));
			reachedDestination = distTravelled >= flightDistance;
		}
	}
	
	public void render(Graphics g) {
		/* mortars change size depending on where they're fired from
		 * and where their destination is.
		 * 
		 * they get bigger and then smaller to make it appear like they're
		 * being fired high up in the air.
		 */
		
		// calculate distance from mortar to target
		double currentDistance = Math.sqrt(
			Math.pow(destX - getXExact(), 2) + Math.pow(destY - getYExact(), 2));
		
		double distFraction =
			1 - Math.abs(((flightDistance - currentDistance) / flightDistance) * 2 - 1);
		
		if (InsideAV.DEBUG) {
			InsideAV.font.drawString(String.format("f: %3f", distFraction),
				getX(),
				getY() - 15,
				Color.white,
				1,
				false, g);
		}
		
		/* making it so that at it's smallest, the mortar is normal size.
		 * at it's peak, it's enlarged by some factor.
		 * 
		 * using a quadratic relationship.
		 */
		int newSize = SPRITE.getWidth()
			+ (int) Math.round(SPRITE.getWidth() * 3 * (-Math.pow(distFraction, 2) + 2 * distFraction));
		
		g.drawImage(SPRITE,
			getX(),
			getY(),
			getX() + newSize,
			getY() + newSize,
			0, 0,
			SPRITE.getWidth(), SPRITE.getHeight());
	}
	
	/**
	 * Disables collision detection for this object.
	 */
	@Override
	public boolean touchingEnemy(Enemy enemy) {
		return false;
	}
	
	@Override
	public int getHalfWidth() {
		return 5;
	}

	@Override
	public int getHalfHeight() {
		return 5;
	}

	@Override
	public int getDamage() {
		return 100;
	}
	
	public boolean reachedDestination() {
		return reachedDestination;
	}
	
}
