package ronan_hanley.inside_av.weapons_systems;

import java.util.ArrayList;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

import ronan_hanley.inside_av.InsideAV;
import ronan_hanley.inside_av.enemy.Enemy;

public final class Tier2MortarWeaponSystem extends MortarWeaponSystem {
	private static final Image SPRITE;
	private static final Sound SHOOT_SOUND;
	
	static {
		Image sprite = null;
		try {
			sprite = new Image("res/images/weapons/mortar/tier2.png", false, Image.FILTER_NEAREST);
		} catch (SlickException e) {
			e.printStackTrace();
		}
		
		SPRITE = sprite;
		
		Sound sound = null;
		try {
			sound = new Sound("res/sound/sfx/mortar_shoot.ogg");
		} catch (SlickException e) {
			e.printStackTrace();
		}
		SHOOT_SOUND = sound;
	}
	
	public Tier2MortarWeaponSystem(int x, int y, ArrayList<Enemy> enemies) {
		super(x, y, SPRITE, enemies);
	}
	
	public int getFireInterval() {
		return 1 * InsideAV.FPS;
	}

	@Override
	protected Sound getShootSound() {
		return SHOOT_SOUND;
	}
	
	@Override
	public double getUpgradeCost() {
		return 800;
	}

	@Override
	public WeaponSystem getUpgradedWeapon() {
		return new Tier3MortarWeaponSystem(getTileX(), getTileY(), enemies);
	}
	
}
