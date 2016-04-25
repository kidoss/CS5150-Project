package model.game.player;

import java.util.ArrayList;

import model.game.board.BlockType;

//Represents a player in the game
public class Player {
	public boolean[][] vision;
	public int x, y, id, direction;
	public double points;
	public Action nextAction;
	
	//Initializations
	public Player(int id, int x, int y, int direction) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.direction = direction;
		points = 0;
		setVision();
		nextAction = null;
	}
	
	//Returns the input for a neural network using the given board, opponent player and the vision defined below
	public double[] getNeuralNetInput(ArrayList<ArrayList<model.game.board.BlockType>> board, Player opponent) {
		ArrayList<Double> tempInput = new ArrayList<Double>();
		double span = getManhattanDistance(0, 0, board.size(), board.get(0).size());
		
		for(int i = 0; i < vision.length; i++)
			for(int j = 0; j < vision[i].length; j++)
				if(vision[i][j]) {
					int yOffset = 5 - i;
					int xOffset = 4 - j;
					int yOff, xOff;
					
					if(direction == 0) {
						yOff = y - yOffset;
						xOff = x - xOffset;
					} else if (direction == 1) {
						yOff = y - xOffset;
						xOff = x + yOffset;
					} else if (direction == 2) {
						yOff = y + yOffset;
						xOff = x + xOffset;
					} else {
						yOff = y + xOffset;
						xOff = x - yOffset;
					}
					
					if(opponent.x == xOff && opponent.y == yOff) {
						tempInput.add(-1.0);
						tempInput.add(-1.0);
						tempInput.add(-1.0);
					} else if(xOff < 0 || xOff >= board.size() || yOff <= 0 || yOff >= board.get(x).size()) {
						tempInput.add(1.0);
						tempInput.add(0.0);
						tempInput.add(0.0);
					} else if(board.get(xOff).get(yOff) == BlockType.SAND) {
						tempInput.add(0.0);
						tempInput.add(1.0);
						tempInput.add(0.0);
					} else if(board.get(xOff).get(yOff) == BlockType.MARK) {
						tempInput.add(0.0);
						tempInput.add(0.0);
						tempInput.add(1.0);
					} else {
						tempInput.add(1.0);
						tempInput.add(0.0);
						tempInput.add(0.0);
						
					}
				}
		
		if(direction - opponent.direction == 0) {
			tempInput.add(1.0);
			tempInput.add(0.0);
			tempInput.add(0.0);
		} else if(Math.abs(direction - opponent.direction) == 2) {
			tempInput.add(-1.0);
			tempInput.add(-1.0);
			tempInput.add(-1.0);
		} else if(direction != 0 && direction - opponent.direction == 1 || direction == 0 && opponent.direction == 3) {
			tempInput.add(0.0);
			tempInput.add(1.0);
			tempInput.add(0.0);
		} else {
			tempInput.add(0.0);
			tempInput.add(0.0);
			tempInput.add(1.0);
		}
		
		tempInput.add(getManhattanDistance(x, y, opponent.x, opponent.y) / span);
		
		double closestMark = Double.MAX_VALUE;
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(i).size(); j++)
				if(board.get(i).get(j) == BlockType.MARK) {
					double dist = getManhattanDistance(x, y, i, j);
					
					closestMark = dist < closestMark ? dist : closestMark;
				}
		
		tempInput.add(closestMark / span);
		
		double[] input = new double[tempInput.size()];
		
		for(int i = 0; i < tempInput.size(); i++)
			input[i] = tempInput.get(i);
		
		return input;
	}
	
	//Checks whether a coordinate is in sight of the player
	public boolean inSight(int obsX, int obsY) {
		obsX -= x;
		obsY -= y;
		int relX = -obsX, relY = -obsY;
		int centerX = (vision[0].length - 1) / 2 - 1;
		int centerY = (vision.length - 1) / 2;
		
		if(direction == 1) {
			relX = -obsY;
			relY = obsX;
		} else if(direction == 2) {
			relX = obsX;
			relY = obsY;
		} else if(direction == 3) {
			relX = obsY;
			relY = -obsX;
		}
		
		obsX = centerX - relX;
		obsY = centerY - relY;
		
		if(obsX < 0 || obsX >= vision.length)
			return false;
		else if(obsY < 0 || obsY >= vision[0].length)
			return false;
		
		return vision[obsX][obsY];	
	}

	//Gets the manhattan distance from one point to the other
	private static double getManhattanDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
	}

	//Sets which spaces around a player that they can see
	private void setVision() {
		vision = new boolean[9][11];
		vision[0] = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
		vision[1] = new boolean[]{false, false, false, true, true, false, false, false, false, false, false};
		vision[2] = new boolean[]{false, false, true, true, true, true, false, false, false, false, false};
		vision[3] = new boolean[]{false, true, true, true, true, true, true, false, false, false, false};
		vision[4] = new boolean[]{true, true, true, true, true, true, true, false, false, false, false};
		vision[5] = new boolean[]{false, true, true, true, true, true, true, false, false, false, false};
		vision[6] = new boolean[]{false, false, true, true, true, true, false, false, false, false, false};
		vision[7] = new boolean[]{false, false, false, true, true, false, false, false, false, false, false};
		vision[8] = new boolean[]{false, false, false, false, false, false, false, false, false, false, false};
	}
}
