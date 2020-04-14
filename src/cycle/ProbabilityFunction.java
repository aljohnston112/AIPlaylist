package cycle;

import java.io.Serializable;
import java.util.stream.DoubleStream;

/**
 * @author Alexander Johnston
 *         Copyright 2019
 *         A class for ProbabilityFunctions where a group of Objects are picked from randomly to decide the Object output
 */
public class ProbabilityFunction implements Serializable {

	// The group of Objects that makes a split in the directed graph
	protected Object[] choices;

	// The probabilities of picking each Object
	protected  double[] probabilities;

	private static final long serialVersionUID = 879228293146606500L;

	protected int lastReturnedIndex;

	/**
	 * @return the number of Objects in this ProbabilityFunction
	 */
	public int length() {
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

	/**       Sets this ProbabilityFunction to choose from a new array of Objects
	 *        with an equal probability of getting any particular Object when fun() is called
	 * @param choices as the new choices to choose from
	 */
	public void setChoices(Object[] choices) {
		this.choices = choices.clone();
		this.probabilities = null;
	}

	/**       Adds a new Object to this ProbabilityFunction, 
	 *        scaling the probabilities of the Objects already in this ProbabilityFunction
	 *        to make room for the added probability
	 * @param choice as the new Object to add to this ProbabilityFunction
	 * @param probability as the probability of getting the new choices when fun() is called
	 */
	public void add(Object choice, double probability) {
		// Add the new Object
		Object[] newChoices;
		if(choices != null) {
			newChoices = new Object[choices.length+1];
			for(int i = 0; i < choices.length; i++) {
				newChoices[i] = choices[i];
			}
		} else {
			newChoices = new Object[1];
		}
		newChoices[newChoices.length-1] = choice;
		choices = newChoices.clone();
		// Update the probabilities
		double probabilityScale = 1.0/(1.0+probability);
		double[] newProbabilities;
		if(probabilities != null) {
			newProbabilities = new double[probabilities.length+1];
			if(probability == 1.0) {
				newProbabilities[newProbabilities.length-1] = probability/newProbabilities.length;
			} else {
				newProbabilities[newProbabilities.length-1] = probability;
			}
			for(int i = 0; i < probabilities.length; i++) {
				if(i == probabilities.length-1) {
					newProbabilities[i] = 0;
					newProbabilities[i] = 1.0-DoubleStream.of(newProbabilities).sum();
				} else {
					newProbabilities[i] = probabilityScale*probabilities[i];
				}
			}
		} else {
			newProbabilities = new double[1];
			newProbabilities[newProbabilities.length-1] = 1.0;
		}
		try {
			setProbabilities(newProbabilities);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**        Removes an Object from this ProbabilityFunction and updates the probabilities 
	 * @param  index as the index to remove from this ProbabilityFunction
	 * @return the probability of the removed Object
	 */
	public double remove(int index) {
		// Remove the Object
		Object[] newChoices = new Object[choices.length-1];
		if(newChoices.length < 1) {
			choices = null;
			probabilities = null;
			return 1.0;
		}
		for(int i = 0, j = 0; i < choices.length; i++) {
			if(i != index) {
				newChoices[j] = choices[i];
				j++;
			}
		}
		choices = newChoices.clone();
		// Update the probabilities
		double[] newProbabilities = new double[probabilities.length-1];
		double probabilityScale = 1.0/(1.0-probabilities[index]);
		double removedProbability = probabilities[index];
		for(int i = 0, j = 0; i < probabilities.length; i++) {
			if(i != index) {
				if((index == probabilities.length-1 && i == probabilities.length-2) || 
						(index != probabilities.length-1 && i == probabilities.length-1)) {
					newProbabilities[j] = 0;
					newProbabilities[j] = 1.0-DoubleStream.of(newProbabilities).sum();
				} else {
					newProbabilities[j] = probabilityScale*probabilities[i];
					j++;
				}
			}
		}
		try {
			setProbabilities(newProbabilities);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return removedProbability;
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
			System.out.print(DoubleStream.of(probabilities).sum());
			throw ProbabilityFunctionException("Probabilities don't add up to 1");	
		}
		for(int i = 0; i < probabilities.length; i++) {
			this.probabilities = probabilities.clone();
		}
	}

	/** Resets the probabilities back to there being an equal chance of getting any object from this ProbabilityFunction
	 * 
	 */
	public void resetProbabilities() {
		this.probabilities = null;
		getProbabilities();
	}

	/**
	 * @return one of the Objects that make up this cycle
	 */
	public Object fun() {
		int indexChoice = 0;
		boolean nnull = true;
		if(probabilities != null) {
			do{
				indexChoice = getIndexChoice();
				if((choices[indexChoice] instanceof ProbabilityFunction)
						&& ((ProbabilityFunction)choices[indexChoice]).length() > 0) {
					nnull = false;
				}
			} while(nnull && (choices[indexChoice] instanceof ProbabilityFunction));
		} else {
			indexChoice = (int)Math.round(Math.random()*(choices.length-1));
		}
		lastReturnedIndex = indexChoice;
		if(choices[lastReturnedIndex] instanceof ProbabilityFunction) {
			return ((ProbabilityFunction)choices[lastReturnedIndex]).fun();
		}
		return choices[lastReturnedIndex];
	}

	private int getIndexChoice() {
		int indexChoice = 0;
		double choice;
		double sumOfProbabilities = 0;
		choice = Math.random();
		for(int k = 1; (choice > sumOfProbabilities) && (k-1) < probabilities.length; k++) {
			indexChoice = k-1;
			sumOfProbabilities += probabilities[k-1];
		} 
		return indexChoice;
	}

	/**        Adjust the probabilities to make the Object at the specified index 
	 *         more likely to be returned when fun() is called
	 * @param  percent as the percentage (0.0-1.0; non inclusive)
	 *         to add to the probability of getting the Object at the specified index
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
		double add = ((1.0-lastProbabilities[index])*percent);
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
			e.printStackTrace();
		}
	}

	/**        Adjust the probabilities to make the Object at the specified index 
	 *         less likely to be returned when fun() is called
	 * @param  percent as the percentage (0.0-1.0; non inclusive)
	 *         to subtract from the probability of getting the Object at the specified index
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
		probabilities[index] = lastProbabilities[index]-(lastProbabilities[index]*percent);
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