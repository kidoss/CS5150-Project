package model.neuralnet;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

//Represents a feedforward multilayer perceptron
//Implements Serializable so it can be saved/loaded
//Learns using the backpropagation method
public class NeuralNet implements Serializable {
	private static final long serialVersionUID = -7124773562644032800L;
	private double alpha;
	private NeuralLayer[] layers;

	//Initializes the the network with the number of layers
	//and the number of neurons in each layer described in layers
	//Alpha sets the learning rate
	public NeuralNet(int[] layers, double alpha) {
		this.alpha = alpha;
		this.layers = new NeuralLayer[layers.length];

		for (int i = 0; i < layers.length; i++)
			if (i == 0)
				this.layers[i] = new NeuralLayer(layers[i], 0);
			else
				this.layers[i] = new NeuralLayer(layers[i], layers[i - 1]);
	}

	//Backpropagates the error through the network using gradient descent
	//Error is calculated by comparing the actual output to the expected output
	//Returns the average error
	public double backpropagate(double[] input, double[] output) {
		double processed[] = process(input);
		double error = 0.0;

		for (int i = 0; i < layers[layers.length - 1].neurons.length; i++) {
			error = output[i] - processed[i];
			layers[layers.length - 1].neurons[i].delta = error * derivative(processed[i]);
		}

		for (int i = layers.length - 2; i >= 0; i--) {
			for (int j = 0; j < layers[i].neurons.length; j++) {
				error = 0.0;

				for (int k = 0; k < layers[i + 1].neurons.length; k++)
					error += layers[i + 1].neurons[k].delta * layers[i + 1].neurons[k].weights[j];

				layers[i].neurons[j].delta = error * derivative(layers[i].neurons[j].out);
			}

			for (int j = 0; j < layers[i + 1].neurons.length; j++) {
				for (int k = 0; k < layers[i].neurons.length; k++)
					layers[i + 1].neurons[j].weights[k] += alpha * layers[i + 1].neurons[j].delta * layers[i].neurons[k].out;

				layers[i + 1].neurons[j].bias += alpha * layers[i + 1].neurons[i].delta;
			}
		}

		error = 0.0;

		for (int i = 0; i < output.length; i++)
			error += Math.abs(output[i] - processed[i]);

		return error / output.length;
	}

	//Classifies a given input, returning an integer representing the class
	//Returned integer is the output with the highest value
	public int classify(double[] input) {
		double output[] = process(input);
		int highest = 0;

		for (int i = 0; i < layers[layers.length - 1].neurons.length; i++) {
			if (output[i] > output[highest])
				highest = i;
		}
		

		return highest;
	}

	//Returns the value of the output neurons after processing the given input
	public double[] process(double[] input) {
		double output[] = new double[layers[layers.length - 1].neurons.length];
		double value;

		for (int i = 0; i < layers[0].neurons.length; i++)
			layers[0].neurons[i].out = input[i];

		for (int i = 1; i < layers.length; i++) {
			for (int j = 0; j < layers[i].neurons.length; j++) {
				value = 0.0;

				for (int k = 0; k < layers[i - 1].neurons.length; k++)
					value += layers[i].neurons[j].weights[k] * layers[i - 1].neurons[k].out;

				value += layers[i].neurons[j].bias;

				layers[i].neurons[j].out = sigmoid(value);
			}
		}

		for (int i = 0; i < layers[layers.length - 1].neurons.length; i++)
			output[i] = layers[layers.length - 1].neurons[i].out;

		return output;
	}

	public double getAlpha() {
		return alpha;
	}

	public int getInputSize() {
		return layers[0].neurons.length;
	}

	public int getOutputSize() {
		return layers[layers.length - 1].neurons.length;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	//Saves the neural network to the path provided
	public void save(String path) {
		try {
			FileOutputStream fout = new FileOutputStream(path + ".nn");
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(this);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//Loads a neural network from the path provided
	public static NeuralNet load(String path) {
		try {
			NeuralNet net;

			FileInputStream fin = new FileInputStream(path);
			ObjectInputStream oos = new ObjectInputStream(fin);
			net = (NeuralNet) oos.readObject();
			oos.close();

			return net;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	//Activation function for the network
	private double sigmoid(double x) {
		return 1 / (1 + Math.pow(Math.E, -x));
	}

	//Calculates the derivative used in backpropagation
	private double derivative(double x) {
		return x - Math.pow(x, 2);
	}
}
