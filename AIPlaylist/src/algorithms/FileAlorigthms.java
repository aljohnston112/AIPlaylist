package algorithms;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Alexander Johnston
 * @since  Copyright 2019
 *         File algorithms.
 */
public final class FileAlorigthms {

	/** Enforces non-instantibility
	 * 	@throws AssertionError to enforce non-instantibility
	 */
	private FileAlorigthms() {
		throw new AssertionError("FileAlgorithms is non-instantiable");
	}

	/**        Gets all the supported media files from folder.
	 * @param  folder as the File containing a folder.
	 * @param  subDirectories as whether or not to check all the sub-directories for media.
	 * @return a List of the supported media files from the folder.
	 * @throws NullPointerException if folder is null.
	 * @throws IllegalArgumentException if folder is not a directory.
	 */
	public static List<File> getMediaFiles(File folder, boolean subDirectories) {
		Objects.requireNonNull(folder);
		if(!folder.isDirectory())
			throw new IllegalArgumentException("folder passed to getMediaFiles() must be a directory");
		// Invariants secured
		List<File> files = Arrays.asList(folder.listFiles());
		List<File> mediafiles = new ArrayList<File>();
		for(File f : files) {
			if(f.isFile()) {
				if(".mp4".equals(getExt(f).orElse(""))||".3gp".equals(getExt(f).orElse(""))||".asf".equals(getExt(f).orElse(""))
						||".wmv".equals(getExt(f).orElse(""))||".au".equals(getExt(f).orElse(""))||".avi".equals(getExt(f).orElse(""))
						||".flv".equals(getExt(f).orElse(""))||".mov".equals(getExt(f).orElse(""))||".ogm".equals(getExt(f).orElse(""))
						||".ogg".equals(getExt(f).orElse(""))||".mkv".equals(getExt(f).orElse(""))||".mka".equals(getExt(f).orElse(""))
						||".ts".equals(getExt(f).orElse(""))||".mpg".equals(getExt(f).orElse(""))||".mp3".equals(getExt(f).orElse(""))
						||".mp2".equals(getExt(f).orElse(""))||".nsc".equals(getExt(f).orElse(""))||".nsv".equals(getExt(f).orElse(""))
						||".nut".equals(getExt(f).orElse(""))||".ra".equals(getExt(f).orElse(""))||".ram".equals(getExt(f).orElse(""))
						||".rm".equals(getExt(f).orElse(""))||".rv".equals(getExt(f).orElse(""))||".rmbv".equals(getExt(f).orElse(""))
						||".a52".equals(getExt(f).orElse(""))||".dts".equals(getExt(f).orElse(""))||".aac".equals(getExt(f).orElse(""))
						||".flac".equals(getExt(f).orElse(""))||".dv".equals(getExt(f).orElse(""))||".vid".equals(getExt(f).orElse(""))
						||".tta".equals(getExt(f).orElse(""))||".tac".equals(getExt(f).orElse(""))||".ty".equals(getExt(f).orElse(""))
						||".wav".equals(getExt(f).orElse(""))||".dts".equals(getExt(f).orElse(""))||".xa".equals(getExt(f).orElse(""))) {
					mediafiles.add(f);
				} 
			} else if(f.isDirectory() && subDirectories) {
				// Recursive call
				List<File> tempMediaFiles = (getMediaFiles(f, subDirectories));
				for(File f2 : tempMediaFiles) 
					mediafiles.add(f2);
			}
		}
		return mediafiles;
	}

	/**        Gets the file extension from a file.
	 * @param  file as the file to get the extension from.
	 * @return an Optional String containing the extension.
	 * @throws NullPointerException if file is null.
	 * @throws IllegalArgumentException if file is a directory.
	 */
	public static Optional<String> getExt(File file) {
		Objects.requireNonNull(file);
		if(file.isDirectory()) {
			throw new IllegalArgumentException("file passed to getExt() must not be a directory");
		}
		String name = file.getName();
		int lastPeriodIndex = name.lastIndexOf(".");
		return (lastPeriodIndex == -1) ? Optional.empty() : Optional.of(name.substring(lastPeriodIndex));
	}

	/**        Finds a File from a folder.
	 * @param  name as the name of the file.
	 * @param  folder as the folder.
	 * @return an Optional File with the name if it exists in folder.
	 * @throws NullPointerException if name or folder is null.
	 * @throws IllegalArgumentException if folder is not a directory.
	 */
	public static Optional<File> findFile(String name, File folder) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(folder);
		File playlistFile = new File(folder.getAbsolutePath()+"\\"+name);
		if(folder.isDirectory()) {
			return (!playlistFile.exists()) ? Optional.empty() : Optional.of(playlistFile);
		} else
			throw new IllegalArgumentException("folder passed to findFile() must be a directory");
	}

}