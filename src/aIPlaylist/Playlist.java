package aIPlaylist;

import java.io.File;

import cycle.ProbabilityFunction;

/**
 * @author Alexander Johnston
 *         Copyright 2019
 *         A class for playlists where a group of media files are picked from randomly to decide the next media
 */
public class Playlist extends ProbabilityFunction {

	private static final long serialVersionUID = 2323326608918863420L;

	Object[] allChoices;

	int[] locations;

	int[] indices;

	/**       Creates a playlist
	 * @param folder as the folder to get media files from
	 * @param checkSubDirectories as whether or not to check all the sub-directories for media 
	 */
	public Playlist(File folder, boolean checkSubDirectories){		
		// Create 3 distributions for frequent, non-frequent and rare
		Object[] objectChoices = {new ProbabilityFunction(), new ProbabilityFunction(), new ProbabilityFunction()};
		this.choices = objectChoices.clone();
		double[] probabilities = {0.78, 0.14, 0.8};
		this.probabilities = probabilities.clone();

		// Add all the files to the frequent distribution
		File[] files = FileAlorigthms.getMediaFiles(folder, checkSubDirectories);
		((ProbabilityFunction)choices[0]).setChoices(files);
		allChoices = files.clone();
		locations = new int[files.length];
		indices = new int[files.length];
		for(int i = 0; i < files.length; i++) {
			locations[i] = 0;
			indices[i] = i;
		}
	}

	/* (non-Javadoc)
	 * @see cycle.ProbabilityFunction#good(int, double)
	 */
	@Override
	public synchronized void good(int index, double percent) {
		((ProbabilityFunction)choices[locations[index]]).good(indices[index], percent);
		if(locations[index] != 0) {
			((ProbabilityFunction)choices[locations[index]-1]).add(allChoices[index],
					((ProbabilityFunction)choices[locations[index]]).remove(index));
			for(int i = index; i < indices.length; i++) {
				if(locations[index] == locations[i] && index < i) {
					indices[i] = indices[i]-1;
				}
			}
			locations[index] = locations[index]-1;
			indices[index] = ((ProbabilityFunction)choices[locations[index]]).length()-1;
		}
	}

	/* (non-Javadoc)
	 * @see cycle.ProbabilityFunction#bad(int, double)
	 */
	@Override
	public synchronized void bad(int index, double percent) {
		((ProbabilityFunction)choices[locations[index]]).bad(indices[index], percent);
		if(locations[index] != 2) {
			((ProbabilityFunction)choices[locations[index]+1]).add(allChoices[index],
					((ProbabilityFunction)choices[locations[index]]).remove(index));
			for(int i = index; i < indices.length; i++) {
				if(locations[index] == locations[i] && index < i) {
					indices[i] = indices[i]-1;
				}
			}
			locations[index] = locations[index]+1;
			indices[index] = ((ProbabilityFunction)choices[locations[index]]).length()-1;
		}
	}

	/* (non-Javadoc)
	 * @see cycle.ProbabilityFunction#getLastReturnedIndex()
	 */
	@Override
	public synchronized int getLastReturnedIndex() {
		int lastReturnedLocationIndex = ((ProbabilityFunction)choices[locations[lastReturnedIndex]]).getLastReturnedIndex();
		for(int i = 0; i < allChoices.length; i++) {
			if(locations[i] == lastReturnedIndex && indices[i] == lastReturnedLocationIndex) {
				return i;
			}
		}
		return -1;
	}

}