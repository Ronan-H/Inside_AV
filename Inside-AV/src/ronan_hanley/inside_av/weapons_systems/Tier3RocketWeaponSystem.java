package ronan_hanley.inside_av.weapons_systems;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

import ronan_hanley.inside_av.InsideAV;

public final class Tier3RocketWeaponSystem extends RocketWeaponSystem {
	private static final Image SPRITE;
	private static final Sound SHOOT_SOUND;
	
	static {
		Image sprite = null;
		try {
			sprite = new Image("res/images/weapons/rocket/tier3.png", false, Image.FILTER_NEAREST);
		} catch (SlickException e) {
			e.printStackTrace();
		}
		
		SPRITE = sprite;
		
		Sound sound = null;
		try {
			sound = new Sound("res/sound/sfx/rocket_shoot.ogg");
		} catch (SlickException e) {
			e.printStackTrace();
		}
		SHOOT_SOUND = sound;
	}
	
	public Tier3RocketWeaponSystem(int x, int y) {
		super(x, y, SPRITE);
	}
	
	@Override
	public void fire() {
		// the distance from the centre of this weapon to it's barrels
		int barrelDistance = 9;
		
		int offsetX, offsetY;
		
		offsetX = offsetY = 0;
		
		addProjectile(new Rocket(getCentreX() + offsetX,
			getCentreY() + offsetY,
			0.2,
			this));
		
		offsetX = (int) (Math.cos(getAngle() + (Math.PI /2)) * barrelDistance);
		offsetY = (int) (Math.sin(getAngle() + (Math.PI /2)) * barrelDistance);
		
		addProjectile(new Rocket(getCentreX() + offsetX,
			getCentreY() + offsetY,
			0.2,
			this));
		
		offsetX = (int) (Math.cos(getAngle() - (Math.PI /2)) * barrelDistance);
		offsetY = (int) (Math.sin(getAngle() - (Math.PI /2)) * barrelDistance);
		
		addProjectile(new Rocket(getCentreX() + offsetX,
			getCentreY() + offsetY,
			0.2,
			this));
		
		playShootSound();
	}
	
	public int getFireInterval() {
		return 2 * InsideAV.FPS;
	}

	@Override
	protected Sound getShootSound() {
		return SHOOT_SOUND;
	}
	
	@Override
	public double getUpgradeCost() {
		// can not upgrade
		return -1;
	}

	@Override
	public WeaponSystem getUpgradedWeapon() {
		return null;
	}
	
}
