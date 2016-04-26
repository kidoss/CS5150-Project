package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import model.game.Game;
import model.game.board.BlockType;
import model.game.board.BoardGenerator;
import model.game.player.Action;
import model.game.player.Player;
import model.neuralnet.NeuralNet;
import view.GamePanel;

//Handles the training of a new neural network
//Draws the games according to the user-provided settings
public class TrainingController extends GameController {
	private static int killReward = 50, markReward = 50, deathPenalty = 25;
	private static double rewardDecay = 0.5;
	private int games, display, moves, drawDelay;
	private String path;

	//Initializes the neural network and sets the parameters
	public TrainingController(int games, int display, int delay, int moves, int layers, int layerSize, double alpha,
			String path, GamePanel gamePanel) {
		this.games = games;
		this.display = display;
		this.drawDelay = delay;
		this.moves = moves;
		this.path = path;
		this.gamePanel = gamePanel;
		int[] netLayers = new int[layers + 2];

		netLayers[0] = new Player(0, 0, 0, 0).getNeuralNetInput(BoardGenerator.generateBoard(25, 25, 5, 1.5, 0.3, 0.3),
				new Player(0, 0, 0, 0)).length;

		for (int i = 0; i < layers; i++)
			netLayers[i + 1] = layerSize;

		netLayers[layers + 1] = 5;

		net = new NeuralNet(netLayers, alpha);
	}

	//Begins the training
	public void run() {
		reinforceSingleActions(10000);
		trainGames(games, display, drawDelay, moves, path);
		reinforceSingleActions(100);
		net.save(path);
	}
	
	// Trains a neural network according to the parameters set by the user
	private void trainGames(int games, int display, int delay, int moves, String path) {
		for (int i = 0; i < games; i++) {
			int w = (int) (10 + (((double) i) / games) * 10);
			int h = (int) (10 + (((double) i) / games) * 10);
			int marks = (int) (20 + (((double) i) / games) * 10);
			double water = 3.0 - (((double) i) / games) * 1.25;
			double density = 0.1 + (((double) i) / games) * 0.2;
			double spread = 0.1 + (((double) i) / games) * 0.25;
			boolean show = false;
	
			if ((i + 1) % display == 0)
				show = true;
	
			System.out.println("Game " + (i + 1));
			trainGame(w, h, marks, moves, water, density, spread, show, delay);
			reinforceSingleActions(50);
		}
	}

	// Plays through a game and trains the neural network on it
	// The neural network plays against itself
	// Returns the training data used
	private ArrayList<ArrayList<Double>> trainGame(int w, int h, int marks, int moves, double water, double density,
			double spread, boolean show, int delay) {
		ArrayList<ArrayList<Double>> trainingData = new ArrayList<ArrayList<Double>>();
		ArrayList<Integer> rewards = new ArrayList<Integer>();
		game = new Game(w, h, marks, water, density, spread);
		int move = 0, oneTurn = 0, twoTurn = 0;
		int dig = 0, moved = 0, turn = 0, attack = 0;
		gamePanel.draw(game, true);
	
		while (!game.finished && move++ < moves) {
			if (show) {
				gamePanel.draw(game, true);
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
	
			Action action = makeMove(0, trainingData, rewards);
			if (action == Action.LEFT || action == Action.RIGHT)
				oneTurn++;
			else
				oneTurn = 0;
	
			switch (action) {
			case DIG:
				dig++;
				break;
			case ATTACK:
				attack++;
				break;
			case MOVE:
				moved++;
				break;
			default:
				turn++;
				break;
			}
	
			if (oneTurn == 3) {
				for(int i = 0; i < 25; i++)
					net.backpropagate(game.players.get(0).getNeuralNetInput(game.board, game.players.get(1)),
							getRandomOutput(action));
				//rewards.set(rewards.size() - 1, -2);
				oneTurn = 0;
			}
	
			if (game.finished)
				break;
	
			if (show) {
				gamePanel.draw(game, true);
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			action = makeMove(1, trainingData, rewards);
			if (action == Action.LEFT || action == Action.RIGHT)
				twoTurn++;
			else
				twoTurn = 0;
	
			switch (action) {
			case DIG:
				dig++;
				break;
			case ATTACK:
				attack++;
				break;
			case MOVE:
				moved++;
				break;
			default:
				turn++;
				break;
			}
	
			if (twoTurn == 3) {
				for(int i = 0; i < 25; i++)
					net.backpropagate(game.players.get(1).getNeuralNetInput(game.board, game.players.get(0)),
							getRandomOutput(action));
				//rewards.set(rewards.size() - 1, -2);
				twoTurn = 0;
			}
		}
	
		if (game.players.get(0).points % 10 == 5 || game.players.get(1).points % 10 == 5)
			rewards.set(rewards.size() - 2, rewards.get(rewards.size() - 2) - deathPenalty);
	
		System.out.println("Dig: " + dig);
		System.out.println("Move: " + moved);
		System.out.println("Attack: " + attack);
		System.out.println("Turn: " + turn + "\n");
	
		distributeRewards(rewards);
		clearEmptyRewards(trainingData, rewards);
		trainNetwork(trainingData, rewards);
		expandTrainingData(trainingData, rewards);
	
		return trainingData;
	}

	// Gets the next move for the given player by running the input through the
	// neural network
	// Updates the training and reward data
	private Action makeMove(int player, ArrayList<ArrayList<Double>> data, ArrayList<Integer> rewards) {
		Action action = null;
		boolean valid = false;
	
		while (!valid) {
			ArrayList<Double> training = new ArrayList<Double>();
			double[] input = game.players.get(player).getNeuralNetInput(game.board, game.players.get((player + 1) % 2));
			double reward = game.players.get(player).points;
			int act;
			
			if(Math.random() < 0.8) {
				action = getNeuralNetAction(input);
				act = net.classify(input);
			} else {
				action = getRandomAction();
				act = getActionInt(action);
			}
			
	
			for (int i = 0; i < input.length; i++)
				training.add(input[i]);
	
			for (int i = 0; i < 5; i++)
				if (i == act)
					training.add(1.0);
				else
					training.add(0.0);
	
			data.add(training);
	
			valid = game.play(action);
	
			if (valid) {
				reward = game.players.get(player).points - reward;
	
				if (reward == 10)
					rewards.add(markReward);
				else if (reward == 25)
					rewards.add(killReward);
				//else if (action == Action.MOVE)
				//	rewards.add(1);
				else
					rewards.add(0);
			} else {
				net.backpropagate(input, getRandomOutput(act));
				rewards.add(-1);
			}
		}
	
		return action;
	}

	// Removes any data points whose reward is 0
	private void clearEmptyRewards(ArrayList<ArrayList<Double>> data, ArrayList<Integer> rewards) {
		for (int i = 0; i < rewards.size(); i++)
			if (rewards.get(i) == 0) {
				data.remove(i);
				rewards.remove(i);
				i--;
			}
	}

	// Distributes the reward backwards so that actions leading to a reward
	// are rewarded as well as the final action that gets the reward
	private void distributeRewards(ArrayList<Integer> rewards) {
		boolean last = true;
		int lastReward = 0, nextReward = 0;
	
		for (int i = rewards.size() - 1; i >= 0; i--) {
			if (last) {
				rewards.set(i, rewards.get(i) + lastReward);
				lastReward = (int) (rewards.get(i) * rewardDecay);
			} else {
				rewards.set(i, rewards.get(i) + nextReward);
				nextReward = (int) (rewards.get(i) * rewardDecay);
			}
	
			while (i - 1 > 0 && rewards.get(i - 1) == -1)
				i--;
	
			last = !last;
		}
	}

	// Copies data points according to their reward value to reflect how many
	// times they're traind
	// on the network
	// Also reverses the expected output for data points with negative rewards
	private void expandTrainingData(ArrayList<ArrayList<Double>> data, ArrayList<Integer> rewards) {
		int added = 0;
	
		for (int i = 0; i < rewards.size(); i++) {
			if (rewards.get(i) < 0)
				for (int j = data.get(i + added).size() - 5; j < data.get(i + added).size(); j++)
					data.get(i + added).set(j, -data.get(i + added).get(j));
	
			for (int j = 0; j < Math.abs(rewards.get(i)); j++) {
				data.add(i + added, data.get(i + added++));
			}
		}
	}

	// Trains the network with the given set of data (including input and
	// output), using the
	// rewards for each data point to tell it how much to train
	private void trainNetwork(ArrayList<ArrayList<Double>> data, ArrayList<Integer> rewards) {
		if(data.size() < 1)
			return;
		
		int inputSize = data.get(0).size() - 5;
	
		for (int i = 0; i < data.size(); i++) {
			double[] input = new double[inputSize];
			double[] output = new double[5];
	
			for (int j = 0; j < inputSize; j++)
				input[j] = data.get(i).get(j);
	
			for (int j = inputSize; j < data.get(i).size(); j++)
				output[j - inputSize] = data.get(i).get(j);
	
			if (rewards.get(i) < 0)
				for (int j = 0; j < 5; j++)
					output[j] = output[j] == 1.0 ? 0.0 : 1.0;
	
			for (int j = 0; j < Math.abs(rewards.get(i)); j++)
				if (rewards.get(i) != -1)
					net.backpropagate(input, output);
		}
	}

	//Returns a random block, weighted towards SAND
	private BlockType getRandomBlock() {
		double rnd = Math.random();
	
		if (rnd < 0.10)
			return BlockType.WATER;
		else if (rnd < 0.25)
			return BlockType.MARK;
		else if (rnd < 0.40)
			return BlockType.BLOCK;
		else
			return BlockType.SAND;
	}
	
	//Returns a random action
	private Action getRandomAction() {
		double rnd = Math.random();
		Action action = Action.MOVE;

		if (rnd < 0.05)
			action = Action.ATTACK;
		else if (rnd < 0.1)
			action = Action.DIG;
		else if (rnd < 0.3)
			action = Action.LEFT;
		else if (rnd < 0.5)
			action = Action.RIGHT;

		return action;
	}
	
	//Returns the associated integer with the given action
	private int getActionInt(Action action) {
		switch(action) {
		case DIG: return 1;
		case LEFT: return 2;
		case RIGHT: return 3;
		case MOVE: return 4;
		default: return 0;
		}
	}

	//Returns a random neural network output that is not act
	private double[] getRandomOutput(int act) {
		double[] output = new double[5];
		output[0] = 0;
		output[1] = 0;
		output[2] = 0;
		output[3] = 0;
		output[4] = 0;
		int out;
	
		do {
			out = (int) (Math.random() * 5);
		} while (out == act);
	
		output[out] = 1;
	
		return output;
	}

	//Return a random neural network output that is not action
	private double[] getRandomOutput(Action action) {
		int act = 0;
		
		switch(action) {
		case DIG: act = 1; break;
		case LEFT: act = 2; break;
		case RIGHT: act = 3; break;
		case MOVE: act = 4; break;
		default: break;
		}
		
		return getRandomOutput(act);
	}

	//Prints a list of training data used
	//Unable to use because of memory limitations
	private void printTrainingData(ArrayList<ArrayList<Double>> trainingData, String path) {
		try {
			PrintWriter pw = new PrintWriter(new File(path + "-training.dat"));
	
			for (int i = 0; i < trainingData.size(); i++) {
				for (int j = 0; j < trainingData.get(i).size(); j++)
					pw.print(trainingData.get(i).get(j) + j == trainingData.get(i).size() - 1 ? "" : " ");
	
				pw.println();
			}
	
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	//Reinforces actions so that it knows when to perform an action
	//i.e. dig when on top of X
	private void reinforceSingleActions(int n) {
		for(int j = 0; j < n; j++) {
			double rnd = Math.random();
			
			if(rnd < .2)
				trainMove();
			else if(rnd >= .2 && rnd < 0.4)
				trainLeft();
			else if(rnd >= .4 && rnd < 0.6)
				trainRight();
			else if(rnd >= .6 && rnd < 0.8)
				trainDig();
			else if(rnd >= .8)
				trainAttack();
		}
	}

	//Generates a random board where a MOVE action is appropriate and backpropagates MOVE
	private Action trainMove() {
		double[] output = {0, 0, 0, 0, 1};
		ArrayList<ArrayList<BlockType>> board = new ArrayList<ArrayList<BlockType>>();
		Player player = new Player(0, 12, 12, 0);

		for (int j = 0; j < 25; j++) {
			ArrayList<BlockType> row = new ArrayList<BlockType>();

			for (int k = 0; k < 25; k++)
				row.add(getRandomBlock());

			board.add(row);
		}

		board.get(12).set(12, BlockType.SAND);

		if(Math.random() < 0.75)
			board.get(12).set(11, BlockType.SAND);
		else
			board.get(12).set(11, BlockType.MARK);
		
		double[] input = player.getNeuralNetInput(board, new Player(1, ((int) (Math.random() * 25)),
				((int) (Math.random() * 25)), ((int) (Math.random() * 5))));

		net.backpropagate(input, output);
		//System.out.println("Move " + getNeuralNetAction(input));
		return getNeuralNetAction(input);
	}
	
	//Generates a random board where a LEFT action is appropriate and backpropagates LEFT
	private Action trainLeft() {
		double[] output = {0, 0, 1, 0, 0};
		ArrayList<ArrayList<BlockType>> board = new ArrayList<ArrayList<BlockType>>();
		Player player = new Player(0, 12, 12, 0);

		for (int j = 0; j < 25; j++) {
			ArrayList<BlockType> row = new ArrayList<BlockType>();

			for (int k = 0; k < 25; k++)
				row.add(getRandomBlock());

			board.add(row);
		}

		board.get(12).set(12, BlockType.SAND);
		
		if(Math.random() < 0.75)
			board.get(11).set(12, BlockType.SAND);
		else
			board.get(11).set(12, BlockType.MARK);
		
		if(Math.random() < 0.2)
			board.get(12).set(11, BlockType.WATER);
		else
			board.get(12).set(11, BlockType.BLOCK);
		
		if(Math.random() < 0.2)
			board.get(13).set(12, BlockType.WATER);
		else
			board.get(13).set(12, BlockType.BLOCK);
		
		double[] input = player.getNeuralNetInput(board, new Player(1, ((int) (Math.random() * 25)),
				((int) (Math.random() * 25)), ((int) (Math.random() * 5))));
		
		net.backpropagate(input, output);
		//System.out.println("Left " + getNeuralNetAction(input));
		return getNeuralNetAction(input);
	}
	
	//Generates a random board where a RIGHT action is appropriate and backpropagates RIGHT
	private Action trainRight() {
		double[] output = {0, 0, 0, 1, 0};
		ArrayList<ArrayList<BlockType>> board = new ArrayList<ArrayList<BlockType>>();
		Player player = new Player(0, 12, 12, 0);

		for (int j = 0; j < 25; j++) {
			ArrayList<BlockType> row = new ArrayList<BlockType>();

			for (int k = 0; k < 25; k++)
				row.add(getRandomBlock());

			board.add(row);
		}

		board.get(12).set(12, BlockType.SAND);

		if(Math.random() < 0.75)
			board.get(13).set(12, BlockType.SAND);
		else
			board.get(13).set(12, BlockType.MARK);
		
		if(Math.random() < 0.2)
			board.get(12).set(11, BlockType.WATER);
		else
			board.get(12).set(11, BlockType.BLOCK);
		
		if(Math.random() < 0.2)
			board.get(11).set(12, BlockType.WATER);
		else
			board.get(11).set(12, BlockType.BLOCK);
		
		double[] input = player.getNeuralNetInput(board, new Player(1, ((int) (Math.random() * 25)),
				((int) (Math.random() * 25)), ((int) (Math.random() * 5))));
		
		net.backpropagate(input, output);
		//System.out.println("Right " + getNeuralNetAction(input));
		return getNeuralNetAction(input);
	}

	//Generates a random board where a ATTACK action is appropriate and backpropagates ATTACK
	private Action trainAttack() {
		double[] output = { 1, 0, 0, 0, 0 };
		ArrayList<ArrayList<BlockType>> board = new ArrayList<ArrayList<BlockType>>();
		Player player = new Player(0, 12, 12, 0);

		for (int j = 0; j < 25; j++) {
			ArrayList<BlockType> row = new ArrayList<BlockType>();

			for (int k = 0; k < 25; k++)
				row.add(getRandomBlock());

			board.add(row);
		}

		board.get(12).set(12, BlockType.SAND);
		board.get(12).set(11, BlockType.SAND);
		board.get(12).set(10, BlockType.SAND);
		
		double[] input = player.getNeuralNetInput(board, new Player(1, 12,
				11 - ((int) Math.random() * 2), ((int) (Math.random() * 5))));

		net.backpropagate(input, output);
		//System.out.println("Attack " + getNeuralNetAction(input));
		return getNeuralNetAction(input);
	}

	//Generates a random board where a DIG action is appropriate and backpropagates DIG
	private Action trainDig() {
		double[] output = { 0, 1, 0, 0, 0 };
		ArrayList<ArrayList<BlockType>> board = new ArrayList<ArrayList<BlockType>>();
		Player player = new Player(0, 12, 12, 0);

		for (int j = 0; j < 25; j++) {
			ArrayList<BlockType> row = new ArrayList<BlockType>();

			for (int k = 0; k < 25; k++)
				row.add(getRandomBlock());

			board.add(row);
		}

		board.get(12).set(12, BlockType.MARK);
		
		double[] input = player.getNeuralNetInput(board, new Player(1, ((int) (Math.random() * 25)),
				((int) (Math.random() * 25)), ((int) (Math.random() * 5))));
		
		net.backpropagate(input, output);
		//System.out.println("Dig " + getNeuralNetAction(input));
		return getNeuralNetAction(input);
	}
}
