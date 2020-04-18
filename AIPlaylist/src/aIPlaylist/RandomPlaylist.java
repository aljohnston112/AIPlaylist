package aIPlaylist;

import java.io.File;
import java.io.Serializable;

import java.util.HashSet;
import java.util.Set;

import algorithms.FileAlorigthms;
import tree.ProbFunTree;

/**
 * @author Alexander Johnston
 * @since  Copyright 2019
 *         A playlist where a group of media files are picked from randomly.
 */
public class RandomPlaylist implements Serializable {

	private static final long serialVersionUID = 2323326608918863420L;

	// The ProbFun that randomly picks the media to play
	private ProbFunTree<File> probabilityFunction;

	/**        Creates a random playlist.
	 * @param  folder as the folder to get media files from.
	 * @param  subDirectories as whether or not to check all the sub-directories for media.
	 * @throws IllegalArgumentException if there are not at least two media files in folder.
	 * @throws IllegalArgumentException if folder is not a directory.
	 */
	public RandomPlaylist(File folder, boolean subDirectories) throws Exception{
		if(!folder.isDirectory())
			throw new IllegalArgumentException("File folder must be a directory");
		Set<File> files = new HashSet<File>(FileAlorigthms.getMediaFiles(folder, subDirectories));
		if(files.size() < 1) {
			throw new IllegalArgumentException("At least one media file was not found in " + folder.getAbsolutePath());
		} else { 
			// invariants secured
			int layers = 1;
			probabilityFunction = new ProbFunTree<File>(files, layers);
		}
	}

	/**
	 * @return the ProbFunTree that controls the probability of which media gets played.
	 *         
	 */
	public ProbFunTree<File> getProbFun() {
		return probabilityFunction;
	}

}