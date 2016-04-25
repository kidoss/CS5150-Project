package model.game;

import java.util.ArrayList;

import model.game.board.BlockType;
import model.game.board.BoardGenerator;
import model.game.player.Action;
import model.game.player.Player;

//Models the game class, including the board, players and current progress
public class Game {
	private static int markPoints = 10, killPoints = 25;
	public ArrayList<ArrayList<BlockType>> board;
	public ArrayList<Player> players;
	public boolean finished;
	public int turn;
	
	//Instantiates the game according to given parameters
	public Game(int w, int h, int marks, double water, double blockDensity, double blockSpread) {
		board = BoardGenerator.generateBoard(w, h, marks, water, blockDensity, blockSpread);
		players = new ArrayList<Player>();
		finished = false;
		turn = 0;
		int id = 0;
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(i).size(); j++)
				if(board.get(i).get(j) == BlockType.SPAWN)
					players.add(new Player(id++, i, j, 3));
	}
	
	//Progresses the game using the given move
	public boolean play(Action move) {
		if(finished)
			return false;
		
		Player player = getPlayer(turn);
		boolean valid = true;
		
		switch(move) {
		case MOVE: valid = move(player); break;
		case LEFT: valid = left(player); break;
		case RIGHT: valid = right(player); break;
		case DIG: valid = dig(player); break;
		case ATTACK: valid = attack(player); break;
		default: valid = false; break;
		}
		
		if(valid)
			turn = (turn + 1) % players.size();
		
		return valid;
	}
	
	//Executes the ATTACK action for the given player
	//Returns false if action is not possible, true otherwise
	private boolean attack(Player player) {
		int x1 = player.x, x2 = player.x,  y1 = player.y, y2 = player.y;
		
		switch(player.direction) {
		case 0: y1--; y2 -= 2; break;
		case 1: x1++; x2 += 2; break;
		case 2: y1++; y2 += 2; break;
		case 3: x1--; x2 -= 2; break;
		}

		for(Player other : players)
			if(player.id != other.id && (other.x == x1 && other.y == y1 || other.x == x2 && other.y == y2)) {
				player.points += killPoints;
				gameOver();
				return true;
			}
		
		return false;
	}

	//Executes the DIG action for the given player
	//Returns false if action is not possible, true otherwise
	private boolean dig(Player player) {
		if(board.get(player.x).get(player.y) == BlockType.MARK) {
			player.points += markPoints;
			board.get(player.x).set(player.y, BlockType.SAND);
			
			if(getMarks() == 0)
				gameOver();
			
			return true;
		}
		
		return false;
	}

	//Counts and returns the number of marks left on the board
	private int getMarks() {
		int marks = 0;
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(i).size(); j++)
				if(board.get(i).get(j) == BlockType.MARK)
					marks++;
				
		return marks;
	}

	//Executes the RIGHT action for the given player
	//Returns false if action is not possible, true otherwise
	private boolean right(Player player) {
		player.direction = (player.direction + 1) % 4;
		
		return true;
	}

	//Executes the LEFT action for the given player
	//Returns false if action is not possible, true otherwise
	private boolean left(Player player) {
		player.direction--;
		
		if(player.direction < 0)
			player.direction = 3;
		
		return true;
	}

	//Executes the MOVE action for the given player
	//Returns false if action is not possible, true otherwise
	private boolean move(Player player) {
		int x = player.x, y = player.y;
		
		switch(player.direction) {
		case 0: y--; break;
		case 1: x++; break;
		case 2: y++; break;
		case 3: x--; break;
		}
		
		if(isMoveValid(x, y)) {
			player.x = x;
			player.y = y;
			
			return true;
		}
		
		return false;
	}
	
	//Checks whether a MOVE action is valid
	private boolean isMoveValid(int x, int y) {
		if(x < 0 || x >= board.size() || y <= 0 || y >= board.get(x).size())
			return false;
		
		if(board.get(x).get(y) == BlockType.WATER || board.get(x).get(y) == BlockType.BLOCK)
			return false;
		
		for(Player player : players)
			if(player.x == x && player.y == y)
				return false;
		
		return true;
	}

	//Fetches a player with the given ID
	private Player getPlayer(int id) {
		for(Player player : players)
			if(player.id == id)
				return player;
		
		return null;
	}
	
	//Called when a game ends, prints information about the game
	private void gameOver() {
		finished = true;
		System.out.println("The game has ended!");
		
		for(Player player : players)
			System.out.println("Player " + player.id +": " + player.points);
	}
}
