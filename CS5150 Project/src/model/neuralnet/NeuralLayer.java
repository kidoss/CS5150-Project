package model.neuralnet;
import java.io.Serializable;

public class NeuralLayer implements Serializable {
	private static final long serialVersionUID = -8564170402522783187L;
	public Neuron[] neurons;
	
	public NeuralLayer(int length, int incoming) {
		neurons = new Neuron[length];
		
		for(int i = 0; i < length; i++)
			neurons[i] = new Neuron(incoming);
	}
}
