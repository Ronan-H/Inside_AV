package ronan_hanley.inside_av.weapons_systems;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

import ronan_hanley.inside_av.InsideAV;

public final class Tier1MortarWeaponSystem extends MortarWeaponSystem {
	private static final Image SPRITE;
	private static final Sound SHOOT_SOUND;
	
	static {
		Image sprite = null;
		try {
			sprite = new Image("res/images/weapons/mortar/tier1.png", false, Image.FILTER_NEAREST);
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
	
	public Tier1MortarWeaponSystem(int x, int y) {
		super(x, y, SPRITE);
	}
	
	public int getFireInterval() {
		return 5 * InsideAV.FPS;
	}

	@Override
	protected Sound getShootSound() {
		return SHOOT_SOUND;
	}
	
}
