package model.neuralnet;
import java.io.Serializable;

//Represents an individual Neuron in a neural network
//Implements serializable so it can be saved/loaded
public class Neuron implements Serializable {
	private static final long serialVersionUID = 9153575119047184047L;
	public double[] weights;
	public double out, bias, delta;
	
	//noWeights is the number of incoming inputs
	public Neuron(int noWeights) {
		weights = new double[noWeights];
		out = Math.random() / 1000000000.0;
		bias = Math.random() / 1000000000.0;
		delta = Math.random() / 1000000000.0;
		
		for(int i = 0; i < weights.length; i++)
			weights[i] = Math.random() / 1000000000.0;
	}
}
