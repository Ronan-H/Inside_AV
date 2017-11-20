package ronan_hanley.inside_av.weapons_systems;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

public final class Tier1LaserWeaponSystem extends LaserWeaponSystem {
	private static final Image SPRITE;
	private static final Sound SHOOT_SOUND;
	
	static {
		Image sprite = null;
		try {
			sprite = new Image("res/images/weapons/laser/tier1.png", false, Image.FILTER_NEAREST);
		} catch (SlickException e) {
			e.printStackTrace();
		}
		
		SPRITE = sprite;
		
		Sound sound = null;
		try {
			sound = new Sound("res/sound/sfx/laser_shoot.ogg");
		} catch (SlickException e) {
			e.printStackTrace();
		}
		SHOOT_SOUND = sound;
	}
	
	public Tier1LaserWeaponSystem(int x, int y) {
		super(x, y, SPRITE);
	}
	
	@Override
	public int getFireInterval() {
		return 0;
	}

	@Override
	protected Sound getShootSound() {
		return SHOOT_SOUND;
	}
	
}
