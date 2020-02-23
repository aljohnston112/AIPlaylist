package cycle;

import java.io.Serializable;
import java.util.stream.DoubleStream;

/**
 * @author Alexander Johnston
 *         Copyright 2019
 *         A class for ProbabilityFunctions where a group of Objects are picked from randomly to decide the Object output
 */
public class ProbabilityFunction implements Serializable{

	private static final long serialVersionUID = 879228293146606500L;

	protected int lastReturnedIndex;

	// The group of Objects to be picked from
	protected Object[] choices;

	// The probabilities of picking each Object
	protected  double[] probabilities;

	/**
	 * @return the number of Objects in this ProbabilityFunction
	 */
	public int size() {
		if(choices == null) {
			return 0;
		}
		return choices.length;
	}

	/**
	 * @return the index from the choices array that was chosen last time fun() was called
	 */
	public int getLastReturnedIndex() {
		return lastReturnedIndex;
	}

	/**        Gets the Object from the specified index
	 * @param  index as the index of the Object in this ProbabilityFunction
	 * @return the Object from the specified index
	 */
	public Object getObject(int index) {
		return choices[index];
	}

	/**
	 * @return an array with the probabilities of getting any one of the choices from this ProbabilityFunction
	 */
	public double[] getProbabilities() {
		double[] probabilities;
		if(this.probabilities != null) {
			probabilities = new double[this.probabilities.length];
			for(int i = 0; i < probabilities.length; i++) {
				probabilities[i] = this.probabilities[i];
			}
		} else {
			probabilities = new double[this.choices.length];
			for(int i = 0; i < probabilities.length; i++) {
				probabilities[i] = 1.0/this.choices.length;
			}
			this.probabilities = probabilities.clone();
		}
		return probabilities;
	}

	/**        Update the probabilities of getting any one of the choices from this ProbabilityFunction
	 * @param  probabilities as the new probabilities of getting any one of the choices from this ProbabilityFunction
	 * @throws Exception if the probabilities don't add up to 1
	 */
	public void setProbabilities(double[] probabilities) throws Exception {
		if(choices.length != probabilities.length) {
			throw ProbabilityFunctionException("Wrong number of probabilities");
		}
		if((DoubleStream.of(probabilities).sum()) != 1.0) {
			throw ProbabilityFunctionException("Probabilities don't add up to 1");	
		}
		for(int i = 0; i < probabilities.length; i++) {
			this.probabilities[i] = probabilities[i];
		}
	}

	/**       Adds an object to this ProbabilityFunction making the probability equal to 1.0/n where n is the number of objects contained in this ProbabilityFunction
	 * @param object as the Object to add
	 */
	public void add(Object object) {
		// TODO
	}

	/**       Removes an object to this ProbabilityFunction 
	 * @param object as the Object to remove
	 */
	public void remove(Object object) {
		// TODO
	}

	/** Resets the probabilities back to there being an equal chance of getting any object from this ProbabilityFunction
	 * 
	 */
	public void resetProbabilities() {
		this.probabilities = null;
		getProbabilities();
	}

	/**
	 * @return a randomly picked Object from this ProbabilityFunction
	 */
	public Object fun() {
		int indexChoice = 0;
		double choice;
		double sumOfProbabilities = 0;
		choice = Math.random();
		sumOfProbabilities = 0;
		if(probabilities != null) {
			for(int k = 0; (choice > sumOfProbabilities) && (k) < probabilities.length; k++) {
				indexChoice = k;
				sumOfProbabilities += probabilities[k];
			} 
		} else {
			indexChoice = (int)Math.round(Math.random()*(choices.length-1));
		}
		lastReturnedIndex = indexChoice;
		return choices[lastReturnedIndex];
	}


	/**        Adjust the probabilities to make the Object at the specified index 
	 *         more likely to be returned when fun() is called
	 * @param  percent as the percentage (0.0-1.0; non inclusive) 
	 *         of the probability of getting the Object at the specified index
	 *         to add to the probability 
	 * @param  index as the index to make appear more often
	 */
	public synchronized void good(int index, double percent) {
		if(choices.length == 1) {
			return;
		}
		if(percent >= 1.0 || percent <= 0.0) {
			try {
				throw ProbabilityFunctionException("Percentage passed to good is not between 0.0 and 1.0 (non-inclusive)");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		double[] lastProbabilities = getProbabilities();
		double[] probabilities = lastProbabilities.clone();
		double add;
		if(lastProbabilities[index] > 0.5) {
			add = ((1.0-lastProbabilities[index])*percent);
		} else {
			add = (lastProbabilities[index]*percent);
		}
		probabilities[index] = lastProbabilities[index]+add;
		double leftover = 1.0-probabilities[index];
		double sumOfLeftovers = 0;
		for(int i = 0; i < probabilities.length; i++) {
			if(i != index) {
				sumOfLeftovers += lastProbabilities[i];
			}
		}
		double leftoverScale = leftover/sumOfLeftovers;
		for(int i = 0; i < probabilities.length; i++) {
			if(i != index) {
				if((index == probabilities.length-1 && i == probabilities.length-2) || 
						(index != probabilities.length-1 && i == probabilities.length-1)) {
					probabilities[i] = 0;
					probabilities[i] = 1.0-DoubleStream.of(probabilities).sum();
				} else {
					probabilities[i] = lastProbabilities[i]*leftoverScale;
				}
			}
		}
		try {
			setProbabilities(probabilities);
		} catch (Exception e) {
			for(int i = 0; i < probabilities.length; i++) {
				System.out.print(probabilities[i]);
				System.out.print("\n");
			}
			e.printStackTrace();
		}
	}

	/**        Adjust the probabilities to make the Object at the specified index 
	 *         less likely to be returned when fun() is called
	 * @param  percent as the percentage (0.0-1.0; non inclusive) 
	 *         of the probability of getting the Object at the specified index
	 *         to subtract from the probability 
	 * @param  index as the index to make appear less often
	 */
	public synchronized void bad(int index, double percent) {
		if(choices.length == 1) {
			return;
		}
		if(percent > 1.0 || percent <= 0.0) {
			try {
				throw ProbabilityFunctionException("Percentage passed to good is not between 0.0 and 1.0 (non-inclusive)");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		double[] lastProbabilities = getProbabilities();
		double[] probabilities = lastProbabilities.clone();
		double sub;
		if(lastProbabilities[index] > 0.5) {
			sub = (1.0-(lastProbabilities[index]*percent));
		} else {
			sub = (lastProbabilities[index]*percent);
		}
		probabilities[index] = lastProbabilities[index]-sub;
		double leftover = 1.0-probabilities[index];
		double sumOfLeftovers = 0;
		for(int i = 0; i < probabilities.length; i++) {
			if(i != index) {
				sumOfLeftovers += lastProbabilities[i];
			}
		}
		double leftoverScale = leftover/sumOfLeftovers;
		for(int i = 0; i < probabilities.length; i++) {
			if(i != index) {
				if((index == probabilities.length-1 && i == probabilities.length-2) || 
						(index != probabilities.length-1 && i == probabilities.length-1)) {
					probabilities[i] = 0;
					probabilities[i] = 1.0-DoubleStream.of(probabilities).sum();
				} else {
					probabilities[i] = lastProbabilities[i]*leftoverScale;
				}
			}
		}
		if(probabilities.length == 1) {
			probabilities[0] = 1.0;
		}
		try {
			setProbabilities(probabilities);
		} catch (Exception e) {
			for(int i = 0; i < probabilities.length; i++) {
				System.out.print(probabilities[i]);
				System.out.print("\n");
			}
			e.printStackTrace();
		}
	}

	/**        An exception for when a ProbabilityFunction can't be made
	 * @param  string as the message explaining the error
	 * @return The exception
	 */
	private Exception ProbabilityFunctionException(String string) {
		return null;
	}

}