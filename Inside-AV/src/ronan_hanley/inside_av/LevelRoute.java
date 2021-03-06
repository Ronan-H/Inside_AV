package ronan_hanley.inside_av;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 * Represents the route the enemies will take
 * in the level.
 * @author Ronan
 */
public final class LevelRoute {
	public static final int START_COLOR = Color.GREEN.getRGB();
	public static final int END_COLOR = Color.RED.getRGB();
	public static final int ROUTE_COLOR = Color.BLACK.getRGB();
	public static final int SOLID_COLOR = Color.WHITE.getRGB();
	public static final int[][] OFFSETS = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
	private BufferedImage image;
	private LevelRouteInstruction[] routeInstructions;
	
	public LevelRoute(BufferedImage image) {
		this.image = image;
		createRouteInstructions();
	}
	
	public LevelRoute(int levelNumber) {
		String imgPath = String.format("res/images/level_routes/level_%d_route.png", levelNumber);
		try {
			image = ImageIO.read(new File(imgPath));
		} catch (IOException e) {
			/* This exception should never happen as long as the files
			 * are in the right place, so just print the stack trace.
			 */
			e.printStackTrace();
		}
		
		createRouteInstructions();
	}
	
	private void createRouteInstructions() {
		ArrayList<LevelRouteInstruction> instructionsList = new ArrayList<LevelRouteInstruction>();
		
		// Locate the start and end
		Point startPoint = null;
		Point endPoint = null;
		boolean foundStart = false;
		boolean foundEnd = false;
		
		outerLoop:
		for (int y = 0; y < image.getHeight(); ++y) {
			for (int x = 0; x < image.getWidth(); ++x) {
				if (!foundStart && image.getRGB(x, y) == START_COLOR) {
					startPoint = new Point(x, y);
					foundStart = true;
				}
				
				if (!foundEnd && image.getRGB(x, y) == END_COLOR) {
					endPoint = new Point(x, y);
					foundEnd = true;
				}
				
				if (foundStart && foundEnd) {
					break outerLoop;
				}
			}
		}
		
		instructionsList.add(new LevelRouteInstruction(1, startPoint.x, startPoint.y));
		
		// Create route
		int x = startPoint.x;
		int y = startPoint.y;
		
		int lastX = -1000;
		int lastY = -1000;
		
		outerLoop:
		while ((x == endPoint.x && y == endPoint.y) == false) {
			// Search for adjacent ROUTE_COLOR pixel
			for (int i = 0; i < OFFSETS.length; ++i) {
				int newX = x + OFFSETS[i][0];
				int newY = y + OFFSETS[i][1];
				
				// Check if this pixel is the one we came from
				if (newX == lastX && newY == lastY) {
					continue;
				}
				
				try {
					int pixelColor = image.getRGB(newX, newY);
					if (pixelColor == ROUTE_COLOR || pixelColor == END_COLOR) {
						instructionsList.add(new LevelRouteInstruction(i, newX, newY));
						
						lastX = x;
						lastY = y;
						
						x = newX;
						y = newY;
						continue outerLoop;
					}
				}catch (IndexOutOfBoundsException e) {}
			}
		}
		
		routeInstructions = optimizeInstructions(instructionsList);
	}
	
	/**
	 * Simplifies the set of instructions.
	 * 
	 * Optimises like so (example):
	 * Go right 3 times --> Go right once, for the same distance as going 3 times
	 * i.e, never have the same direction twice in a row.
	 * 
	 * @param instructions The set of instructions to optimise
	 */
	private LevelRouteInstruction[] optimizeInstructions(ArrayList<LevelRouteInstruction> instructions) {
		for (int i = 0; i < instructions.size() -1; ++i) {
			if (instructions.get(i).getDirection() == instructions.get(i + 1).getDirection()) {
				instructions.remove(i--);
			}
		}
		
		LevelRouteInstruction[] returnInstructions = new LevelRouteInstruction[instructions.size()];
		
		for (int i = 0; i < returnInstructions.length; ++i) {
			returnInstructions[i] = instructions.get(i);
		}
		
		return returnInstructions;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public LevelRouteInstruction[] getInstructions() {
		return routeInstructions;
	}
	
	public boolean solidAt(int x, int y) {
		return (image.getRGB(x, y) == SOLID_COLOR);
	}
	
	@Override
	/**
	 * Used in the testing class.
	 */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for (LevelRouteInstruction instruction : routeInstructions) {
			builder.append(instruction.toString() + "\n");
		}
		
		return builder.toString();
	}
	
}
