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

	/**       Creates a playlist
	 * @param folder as the folder to get media files from
	 * @param subDirectories as whether or not to check all the sub-directories for media 
	 */
	public Playlist(File folder, boolean subDirectories){
		File[] files = FileAlorigthms.getMediaFiles(folder, subDirectories);
		choices = files.clone();
	}
	
}