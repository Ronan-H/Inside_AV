package ronan_hanley.inside_av;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import ronan_hanley.inside_av.enemy.Enemy;

public final class Level {
	private Image solidImage;
	private LevelRoute route;
	private Wave[] waves;
	private int currentWave;
	// True if enemies are being spawned
	private boolean waveActive = true;
	
	public Level(int levelNumber) {
		route = new LevelRoute(levelNumber);
		
		String solidImgPath = String.format("res/images/level_textures/level_%d_solid_tile.png", levelNumber);
		try {
			solidImage = new Image(solidImgPath, false, Image.FILTER_NEAREST);
		} catch (SlickException e) {
			/* This exception should never happen as long as the files
			 * are in the right place, so just print the stack trace.
			 */
			e.printStackTrace();
		}
		
		// Load in the wave info
		File[] waveFiles = new File("res/wave_info/level_" + levelNumber).listFiles();
		int numWaves = waveFiles.length;
		waves = new Wave[numWaves];
		
		for (int i = 0; i < numWaves; ++i) {
			waves[i] = new Wave(waveFiles[i].getPath(), route);
		}
		
		currentWave = 0;
	}
	
	public void update(ArrayList<Enemy> enemies) {
		if (waveActive) {
			boolean lastEnemy = waves[currentWave].updateWave(enemies);
			waveActive = !lastEnemy;
		}
	}
	
	public void render(Graphics g) {
		// draw the solid tiles
		BufferedImage levelImage = route.getImage();
		for (int y = 0; y < levelImage.getHeight(); ++y) {
			for (int x = 0; x < levelImage.getWidth(); x++) {
				if (levelImage.getRGB(x, y) == LevelRoute.SOLID_COLOR) {
					g.drawImage(solidImage, x * InsideAV.TILE_SIZE, y * InsideAV.TILE_SIZE);
				}
			}
		}
	}
	
}