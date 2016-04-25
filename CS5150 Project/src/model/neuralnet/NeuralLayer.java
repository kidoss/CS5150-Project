package model.neuralnet;
import java.io.Serializable;

//Represents a single layer in a neural network
//Contains a number of neurons
//Implements Serializable so it can be saved/loaded
public class NeuralLayer implements Serializable {
	private static final long serialVersionUID = -8564170402522783187L;
	public Neuron[] neurons;
	
	//Length is the number of neurons in the layer
	//Incoming is the number of neurons in the previous layer
	public NeuralLayer(int length, int incoming) {
		neurons = new Neuron[length];
		
		for(int i = 0; i < length; i++)
			neurons[i] = new Neuron(incoming);
	}
}
