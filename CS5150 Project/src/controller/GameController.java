package controller;

import java.io.File;

import model.game.Game;
import model.game.player.Action;
import model.neuralnet.NeuralNet;
import view.GamePanel;

public class GameController extends Thread {
	protected static int delay = 50;
	protected GamePanel gamePanel;
	protected Game game;
	protected NeuralNet net;
	protected Action action;
	protected boolean visibility;

	public void createGame(int width, int height, int marks, double water, double density, double spread, boolean visibility, File path) {
		game = new Game(width, height, marks, water, density, spread);
		gamePanel.draw(game, visibility);
		this.visibility = visibility;
		net = NeuralNet.load(path.getName());
		start();
	}
	
	public void setGamePanel(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
	}
	
	public void run() {
		while(!game.finished) {
			if(action != null) {
				boolean valid = game.play(action);
				action = null;
				gamePanel.draw(game, visibility);
				
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if(game.finished)
					endGame();
				
				if(valid) {
					valid = false;
					
					playNeuralNet();
			
					gamePanel.draw(game, visibility);
					
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} else
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		}
		
		endGame();
	}
	
	private void playNeuralNet() {
		boolean valid = game.play(getNeuralNetAction(game.players.get(game.turn).getNeuralNetInput(game.board, game.players.get((game.turn + 1) % 2))));

		if(valid)
			System.out.println("Valid");
		else {
			while(!valid)
				valid = game.play(getRandomAction());
			
			System.out.println("Invalid");
		}
	}

	//Called when the game is finished, pushing a message to the user
	private void endGame() {
		if(game.players.get(0).points > game.players.get(1).points)
			gamePanel.postMessage("Player 1 beat Player 2!\n" + game.players.get(0).points + " points to " + game.players.get(1).points);
		else if(game.players.get(0).points < game.players.get(1).points)
			gamePanel.postMessage("Player 2 beat Player 1!\n" + game.players.get(1).points + " points to " + game.players.get(0).points);
		else
			gamePanel.postMessage("It was a tie! Try setting the X's to an odd number");
	}
	
	//Creates a new neural network, trains it using parameters given and saves it to path given
	public void trainNetwork(int games, int display, int delay, int moves, int layers, int layerSize, double alpha, String path) {
		new TrainingController(games, display, delay, moves, layers, layerSize, alpha, path, gamePanel).start();
	}
	
	//Returns the Action for the given input
	protected Action getNeuralNetAction(double[] input) {
		int act = net.classify(input);
		
		switch(act) {
		case 0: return Action.ATTACK;
		case 1: return Action.DIG;
		case 2: return Action.LEFT;
		case 3: return Action.RIGHT;
		default: return Action.MOVE;
		}
	}
	
	public void setAction(Action action) {
		this.action = action;
	}
	
	public boolean getVisibility() {
		return visibility;
	}

	//Draws the board with the new visibility setting
	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
		gamePanel.draw(game, visibility);
	}

	//Gets a random action, weighted towards movement
	private  Action getRandomAction() {
		double rnd = Math.random();
		Action action = Action.MOVE;
		
		if(rnd < 0.05)
			action = Action.ATTACK;
		else if(rnd < 0.1)
			action = Action.DIG;
		else if(rnd < 0.3)
			action = Action.LEFT;
		else if(rnd < 0.5)
			action = Action.RIGHT;
		
		return action;
	}
}
