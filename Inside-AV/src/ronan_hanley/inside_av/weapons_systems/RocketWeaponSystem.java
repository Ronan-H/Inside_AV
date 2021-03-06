package ronan_hanley.inside_av.weapons_systems;

import org.newdawn.slick.Image;

public abstract class RocketWeaponSystem extends WeaponSystem {
	public static final double COST = 400;
	
	public RocketWeaponSystem(int x, int y, Image sprite) {
		super(x, y, sprite);
	}
	
	@Override
	public double getCost() {
		return COST;
	}
	
	@Override
	public void fire() {
		// fires a rocket
		addProjectile(new Rocket(getCentreX(),
			getCentreY(),
			0.2,
			this));
		
		playShootSound();
	}
	
}
