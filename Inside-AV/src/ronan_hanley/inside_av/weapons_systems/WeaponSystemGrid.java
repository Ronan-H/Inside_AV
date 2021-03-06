package ronan_hanley.inside_av.weapons_systems;

import java.util.ArrayList;

import org.newdawn.slick.Graphics;

import ronan_hanley.inside_av.enemy.Enemy;

public final class WeaponSystemGrid {
	private WeaponSystem[][] grid;
	private ArrayList<WeaponSystem> weaponSystems;
	
	public WeaponSystemGrid(int width, int height) {
		grid = new WeaponSystem[height][width];
		weaponSystems = new ArrayList<WeaponSystem>();
	}
	
	public ArrayList<WeaponSystem> getWeapons() {
		return weaponSystems;
	}
	
	public void addWeaponSystem(WeaponSystem weapon, int x, int y) {
		grid[y][x] = weapon;
		weaponSystems.add(weapon);
	}
	
	public boolean tileHasWeapon(int x, int y) {
		return (grid[y][x] != null);
	}
	
	public WeaponSystem getWeaponAt(int x, int y) {
		return grid[y][x];
	}
	
	public void updateAll(ArrayList<Enemy> enemies) {
		for (WeaponSystem weapon : weaponSystems)
			weapon.update(enemies);
	}
	
	public void renderAll(Graphics g) {
		for (WeaponSystem weapon : weaponSystems)
			weapon.render(g);
	}
	
	/**
	 * Removes all weapons from the grid and list.
	 */
	public void clearAll() {
		weaponSystems = new ArrayList<WeaponSystem>();
		
		grid = new WeaponSystem[grid.length][grid[0].length];
	}
	
	/**
	 * Replaces the weapon at a place in the grid with it's upgraded
	 * version.
	 * @param x
	 * @param y
	 */
	public void upgradeWeaponAt(int x, int y) {
		WeaponSystem oldWeapon = grid[y][x];
		WeaponSystem upgradedWeapon = oldWeapon.getUpgradedWeapon();
		
		weaponSystems.remove(oldWeapon);
		
		grid[y][x] = upgradedWeapon;
		weaponSystems.add(upgradedWeapon);
	}
	
}
