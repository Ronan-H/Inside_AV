package ronan_hanley.inside_av.weapons_systems;

public abstract class BulletWeaponSystem extends WeaponSystem {
	public static final double COST = 100;
	
	public BulletWeaponSystem(int x, int y, String spritePath) {
		super(x, y, spritePath);
	}
	
	public double getCost() {
		return COST;
	}
	
}
