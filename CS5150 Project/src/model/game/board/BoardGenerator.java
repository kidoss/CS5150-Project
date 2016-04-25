package model.game.board;

import java.util.ArrayList;

//Procedurally generates a game board using given parameters
public class BoardGenerator {
	private static double balanceThreshold = 10.0;
	
	//Returns a copy of the given board
	public static ArrayList<ArrayList<BlockType>> copyBoard(ArrayList<ArrayList<BlockType>> board) {
		ArrayList<ArrayList<BlockType>> newBoard = new ArrayList<ArrayList<BlockType>>();
		
		for(int i = 0; i < board.size(); i++) {
			ArrayList<BlockType> col = new ArrayList<BlockType>();
			
			for(int j = 0; j < board.get(i).size(); j++)
				col.add(board.get(i).get(j));
			
			newBoard.add(col);
		}
		
		return newBoard;
	}

	//Generates a new game board with the given parameters
	public static ArrayList<ArrayList<BlockType>> generateBoard(int w, int h, int marks, double water, double block, double spread) {
		ArrayList<ArrayList<BlockType>> board = getBlank(w, h);
		
		placeWater(board, water);
		cleanWater(board);
		makeContiguous(board);
		placeBlocks(board, block, spread);
		placeSpawns(board);
		placeMarks(board, marks);
		
		return board;
	}
	
	//Prints the given game board to console
	public static void printBoard(ArrayList<ArrayList<BlockType>> board) {
		for(ArrayList<BlockType> blocks : board) {
			for(BlockType block : blocks) {
				switch(block) {
				case SAND: System.out.print(" "); break;
				case WATER: System.out.print("W"); break;
				case BLOCK: System.out.print("L"); break;
				case SPAWN: System.out.print("S"); break;
				default: System.out.print("X"); break;
				}
			}
			
			System.out.println();
		}
		
		System.out.println();
	}

	//Returns a blank game board with the given dimensions
	private static ArrayList<ArrayList<BlockType>> getBlank(int w, int h) {
		ArrayList<ArrayList<BlockType>> board = new ArrayList<ArrayList<BlockType>>();
		
		for(int i = 0; i < w + 2; i++) {
			ArrayList<BlockType> temp = new ArrayList<BlockType>();
			
			for(int j = 0; j < h + 2; j++)
				temp.add(BlockType.SAND);
			
			board.add(temp);
		}
		
		return board;
	}

	//Places the water on the board using the given parameter waterProgression
	//Starts at the outer surrounding rectangle, and adds water tiles as it progresses inwards
	//Each layer has a descending chance of creating water tiles
	private static void placeWater(ArrayList<ArrayList<BlockType>> board, double waterProgression) {
		for(int i = 0; i < (board.size() > board.get(0).size() ? board.get(0).size() : board.size()) / 2; i++) {
			for(int j = i; j < (board.size() < board.get(0).size() ? board.get(0).size() : board.size()) - i; j++) {
				if(Math.random() < 1.0 / Math.pow(waterProgression, i))
					if(board.size() < board.get(0).size())
						board.get(i).set(j, BlockType.WATER);
					else
						board.get(j).set(i, BlockType.WATER);
				
				if(Math.random() < 1.0 / Math.pow(waterProgression, i))
					if(board.size() < board.get(0).size())
						board.get((board.size() - 1) - i).set(j, BlockType.WATER);
					else
						board.get(j).set((board.get(0).size() - 1) - i, BlockType.WATER);
			}
		}
		
		for(int i = 0; i < (board.size() > board.get(0).size() ? board.get(0).size() : board.size()) / 2 - 1; i++) {
			for(int j = i + 1; j < (board.size() > board.get(0).size() ? board.get(0).size() : board.size()) - (i + 1); j++) {
				if(Math.random() < 1.0 / Math.pow(waterProgression, i))
					if(board.size() < board.get(0).size())
						board.get(j).set(i, BlockType.WATER);
					else
						board.get(i).set(j, BlockType.WATER);
				
				if(Math.random() < 1.0 / Math.pow(waterProgression, i))
					if(board.size() < board.get(0).size())
						board.get(j).set((board.get(0).size() - 1) - i, BlockType.WATER);
					else
						board.get((board.size() - 1) - i).set(j, BlockType.WATER);
			}
		}
	}
	
	//Calculates the number of blocks to place and calls placeBlock until that number is reached
	private static void placeBlocks(ArrayList<ArrayList<BlockType>> board, double blockDensity, double blockSpread) {
		int sand = 0, blocks = 0, placed = 0;
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(0).size(); j++)
				if(board.get(i).get(j) == BlockType.SAND)
					sand++;
		
		blocks = (int) (sand * blockDensity);
		
		while(placed < blocks)
			placed += placeBlock(board, blockSpread);
	}
	
	//Places a block randomly, and expands it by chance according to the blockSpread parameter
	private static int placeBlock(ArrayList<ArrayList<BlockType>> board, double blockSpread) {
		int x = 0, y = 0, blocks = 1;
		
		do {
			x = (int) (Math.random() * board.size() - 2) + 1;
			y = (int) (Math.random() * board.get(0).size() - 2) + 1;
		} while(!isBlockValid(board, x, y));
		
		board.get(x).set(y, BlockType.BLOCK);
		
		if(Math.random() < blockSpread && isBlockValid(board, x - 1, y)) {
			board.get(x - 1).set(y, BlockType.BLOCK);
			blocks++;
		}
		if(Math.random() < blockSpread && isBlockValid(board, x + 1, y)) {
			board.get(x + 1).set(y, BlockType.BLOCK);
			blocks++;
		}
		if(Math.random() < blockSpread && isBlockValid(board, x, y - 1)) {
			board.get(x).set(y - 1, BlockType.BLOCK);
			blocks++;
		}
		if(Math.random() < blockSpread && isBlockValid(board, x, y + 1)) {
			board.get(x).set(y + 1, BlockType.BLOCK);
			blocks++;
		}
		
		return blocks;
	}
	
	//Checks whether a placed block is in a valid location
	private static boolean isBlockValid(ArrayList<ArrayList<BlockType>> board, int x, int y) {
		boolean Valid = true;
		
		if(board.get(x).get(y) != BlockType.SAND)
			Valid = false;
		
		BlockType old = board.get(x).get(y);
		board.get(x).set(y, BlockType.BLOCK);
		
		if(getZones(getCoverage(board)) > 1)
			Valid = false;
		
		board.get(x).set(y, old);
			
		return Valid;
	}

	//Places the two spawn points on the map, in the top-left most and bottom-right most corners
	//Only places in valid spawn points
	private static void placeSpawns(ArrayList<ArrayList<BlockType>> board) {
		int x = board.size() - 1, y = board.get(0).size() - 1;
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(0).size(); j++)
				if(isSpawnValid(board, i, j) && i + j < x + y) {
					x = i;
					y = j;
				}
		
		board.get(x).set(y, BlockType.SPAWN);
		
		x = 0;
		y = 0;
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(0).size(); j++)
				if(isSpawnValid(board, i, j) && i + j > x + y) {
					x = i;
					y = j;
				}
		
		board.get(x).set(y, BlockType.SPAWN);
	}
	
	//Checks if a position is a valid spawn point
	private static boolean isSpawnValid(ArrayList<ArrayList<BlockType>> board, int x, int y) {
		if (board.get(x).get(y) != BlockType.SAND)
			return false;
		
		int open = 0;
		
		if(board.get(x - 1).get(y) == BlockType.SAND)
			open++;
		if(board.get(x + 1).get(y) == BlockType.SAND)
			open++;
		if(board.get(x).get(y - 1) == BlockType.SAND)
			open++;
		if(board.get(x).get(y + 1) == BlockType.SAND)
			open++;

		if(open >= 3)
			return true;
		else
			return false;
	}

	//Places the X's on the board, the number given as a parameter
	//Makes sure they are within a certain distance from each player so the board is balanced
	private static void placeMarks(ArrayList<ArrayList<BlockType>> board, int marks) {
		ArrayList<ArrayList<BlockType>> tempBoard = copyBoard(board);
		boolean balanced = false;
		
		while(!balanced) {
			int placed = 0;
			tempBoard = copyBoard(board);
			
			while(placed < marks) {
				int x = (int) (Math.random() * board.size());
				int y = (int) (Math.random() * board.get(0).size());
				
				if(isMarkValid(tempBoard, x, y)) {
					tempBoard.get(x).set(y, BlockType.MARK);
					placed++;
				}
			}
			
			if(Math.abs(getMarkScore(tempBoard)) < balanceThreshold)
				balanced = true;
		}
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(0).size(); j++)
				if(tempBoard.get(i).get(j) == BlockType.MARK)
					board.get(i).set(j, BlockType.MARK);
	}
	
	//Checks if a location is a valid placement for an X
	private static boolean isMarkValid(ArrayList<ArrayList<BlockType>> board, int x, int y) {
		boolean valid = false;
		
		if(board.get(x).get(y) == BlockType.SAND)
			valid = true;
		
		return valid;
	}
	
	//Gets the balance score for a distribution of marks
	private static double getMarkScore(ArrayList<ArrayList<BlockType>> board) {
		double score = 0;
		int firstX = -1, firstY = 0, secondX = 0, secondY = 0;
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(0).size(); j++)
				if(board.get(i).get(j) == BlockType.SPAWN)
					if(firstX == -1) {
						firstX = i;
						firstY = j;
					} else {
						secondX = i;
						secondY = j;
					}
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(0).size(); j++)
				if(board.get(i).get(j) == BlockType.MARK) {
					score += Math.sqrt(Math.pow(i - firstX, 2) + Math.pow(j - firstY, 2));
					score -= Math.sqrt(Math.pow(i - secondX, 2) + Math.pow(j - secondY, 2));
				}
		
		return score;
	}

	//Called after the water is placed, makes sure all sand tiles are reachable
	private static void makeContiguous(ArrayList<ArrayList<BlockType>> board) {
		int[][] coverage = getCoverage(board);
		int zone = 1, zones = getZones(coverage), largestZone = 1;
		
		
		for(int i = 1; i <= zones; i++) {
			int zoneSize = 0;
			
			for(int j = 0; j < board.size(); j++)
				for(int k = 0; k < board.get(0).size(); k++)
					if(coverage[j][k] == i)
						zoneSize++;
			
			if(zoneSize > largestZone) {
				largestZone = zoneSize;
				zone = i;
			}
		}
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(0).size(); j++)
				if(coverage[i][j] == zone)
					board.get(i).set(j, BlockType.SAND);
				else
					board.get(i).set(j, BlockType.WATER);
	}
	
	//Assigns zones of coverage to each section of contiguous sand tiles
	private static int[][] getCoverage(ArrayList<ArrayList<BlockType>> board) {
		int[][] coverage = new int[board.size()][board.get(0).size()];
		int zone = 1;
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(0).size(); j++)
				coverage[i][j] = 0;
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(0).size(); j++)
				if(coverage[i][j] == 0 && board.get(i).get(j) == BlockType.SAND)
					explore(board, coverage, zone++, i, j);
		
		return coverage;
	}
	
	//Gets the number of zones in a coverage instance returned by getCoverage
	private static int getZones(int[][] coverage) {
		int zones = 0;
		
		for(int i = 0; i < coverage.length; i++)
			for(int j = 0; j < coverage[0].length; j++)
				if(coverage[i][j] > zones)
					zones = coverage[i][j];
		
		return zones;
	}
	
	//Cleans the water tiles on the map, turning lone tiles into sand
	private static void cleanWater(ArrayList<ArrayList<BlockType>> board) {
		for(int i = 1; i < board.size() - 1; i++)
			for(int j = 1; j < board.get(0).size() - 1; j++)
				if(board.get(i).get(j) == BlockType.WATER) {
					boolean neighbor = false;
					
					for(int k = -1; k <= 1; k++)
						for(int l = -1; l <= 1; l++)
							if(!(k == 0 && l == 0) && board.get(i + k).get(j + l) == BlockType.WATER)
								neighbor = true;
					
					if(!neighbor)
						board.get(i).set(j, BlockType.SAND);
				}
	}
	
	
	//Assigns sand tiles to a zone, helping make the sand be one contiguous block
	private static void explore(ArrayList<ArrayList<BlockType>> board, int[][] coverage, int zone, int x, int y) {
		if(coverage[x][y] == 0 && board.get(x).get(y) == BlockType.SAND) {
			coverage[x][y] = zone;
			
			if(x != 0)
				explore(board, coverage, zone, x - 1, y);
			if(y != 0)
				explore(board, coverage, zone, x, y - 1);
			if(x < board.size() - 1)
				explore(board, coverage, zone, x + 1, y);
			if(y  < board.get(0).size() - 1)
				explore(board, coverage, zone, x, y + 1);
		}
	}
}
