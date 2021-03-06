package ronan_hanley.inside_av;

/**
 * Represents an entity that can rotate, ie. it has
 * an angle.
 * @author Ronan
 */
public class RotationalEntity extends Entity {
	// angle, in radians;
	private double angle;
	
	public RotationalEntity(int x, int y) {
		super(x, y);
	}
	
	public void setAngle(double angle) {
		this.angle = angle;
		
		// remove full rotations
		// ie. 365 degrees can be simplified to 5 degrees
		angle %= (Math.PI * 2);
	}
	
	public double getAngle() {
		return angle;
	}
	
	public void changeAngle(double change) {
		setAngle(getAngle() + change);
	}
	
}
