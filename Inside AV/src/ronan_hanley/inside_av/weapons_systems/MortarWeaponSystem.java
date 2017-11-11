package ronan_hanley.inside_av.weapons_systems;

public abstract class MortarWeaponSystem extends WeaponSystem {
	public static final double COST = 300;
	
	public MortarWeaponSystem(int x, int y, String spritePath) {
		super(x, y, spritePath);
	}
	
	public double getCost() {
		return COST;
	}
	
}
