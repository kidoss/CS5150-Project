package model.game;

import java.util.ArrayList;

import model.game.board.BlockType;
import model.game.board.BoardGenerator;
import model.game.player.Action;
import model.game.player.Player;

public class Game {
	private static int markPoints = 10, killPoints = 25;
	public ArrayList<ArrayList<BlockType>> board;
	public ArrayList<Player> players;
	public boolean finished;
	public int turn;
	
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
	
	public ArrayList<ArrayList<BlockType>> getBoard() {
		return board;
	}
	
	public ArrayList<Player> getPlayers() {
		return players;
	}
	
	public Player getTurn() {
		return players.get(turn);
	}
	
	public boolean isFinished() {
		return finished;
	}
	
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

	private int getMarks() {
		int marks = 0;
		
		for(int i = 0; i < board.size(); i++)
			for(int j = 0; j < board.get(i).size(); j++)
				if(board.get(i).get(j) == BlockType.MARK)
					marks++;
				
		return marks;
	}

	private boolean right(Player player) {
		player.direction = (player.direction + 1) % 4;
		
		return true;
	}

	private boolean left(Player player) {
		player.direction--;
		
		if(player.direction < 0)
			player.direction = 3;
		
		return true;
	}

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

	private Player getPlayer(int id) {
		for(Player player : players)
			if(player.id == id)
				return player;
		
		return null;
	}
	
	private void gameOver() {
		finished = true;
		System.out.println("The game has ended!");
		
		for(Player player : players)
			System.out.println("Player " + player.id +": " + player.points);
	}
}
