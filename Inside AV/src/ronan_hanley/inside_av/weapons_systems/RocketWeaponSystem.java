package ronan_hanley.inside_av.weapons_systems;

public abstract class RocketWeaponSystem extends WeaponSystem {
	public static final double COST = 400;
	
	public RocketWeaponSystem(int x, int y, String spritePath) {
		super(x, y, spritePath);
	}
	
	public double getCost() {
		return COST;
	}
	
}
