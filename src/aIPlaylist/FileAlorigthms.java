package aIPlaylist;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Alexander Johnston
 *         Copyright 2019
 *         A class for file algorithms
 */
public class FileAlorigthms {

	/**        Gets all the supported media files from the folder
	 * @param  folder as the File containing the folder
	 * @param  subDirectories as whether or not to check all the sub-directories for media
	 * @return All the supported media files from the folder
	 */
	public static File[] getMediaFiles(File folder, boolean subDirectories) {
		File[] files = folder.listFiles();
		ArrayList<File> mediafiles = new ArrayList<File>();
		File[] tempMediaFiles;
		if(files!=null) {
			for(int i = 0; i < files.length; i++) {
				if(files[i].isFile()) {
					if(".mp4".equals(getExt(files[i]))||".3gp".equals(getExt(files[i]))||".asf".equals(getExt(files[i]))
							||".wmv".equals(getExt(files[i]))||".au".equals(getExt(files[i]))||".avi".equals(getExt(files[i]))
							||".flv".equals(getExt(files[i]))||".mov".equals(getExt(files[i]))||".ogm".equals(getExt(files[i]))
							||".ogg".equals(getExt(files[i]))||".mkv".equals(getExt(files[i]))||".mka".equals(getExt(files[i]))
							||".ts".equals(getExt(files[i]))||".mpg".equals(getExt(files[i]))||".mp3".equals(getExt(files[i]))
							||".mp2".equals(getExt(files[i]))||".nsc".equals(getExt(files[i]))||".nsv".equals(getExt(files[i]))
							||".nut".equals(getExt(files[i]))||".ra".equals(getExt(files[i]))||".ram".equals(getExt(files[i]))
							||".rm".equals(getExt(files[i]))||".rv".equals(getExt(files[i]))||".rmbv".equals(getExt(files[i]))
							||".a52".equals(getExt(files[i]))||".dts".equals(getExt(files[i]))||".aac".equals(getExt(files[i]))
							||".flac".equals(getExt(files[i]))||".dv".equals(getExt(files[i]))||".vid".equals(getExt(files[i]))
							||".tta".equals(getExt(files[i]))||".tac".equals(getExt(files[i]))||".ty".equals(getExt(files[i]))
							||".wav".equals(getExt(files[i]))||".dts".equals(getExt(files[i]))||".xa".equals(getExt(files[i]))) {
						mediafiles.add(files[i]);
					} 
				} else if(files[i].isDirectory() && subDirectories) {
					tempMediaFiles = (getMediaFiles(files[i], subDirectories));
					for(int k = 0; k < tempMediaFiles.length; k++) {
						mediafiles.add(tempMediaFiles[k]);
					}
				}
			}
		}
		return mediafiles.toArray(new File[mediafiles.size()]);
	}

	/**        Gets the file extension from a file
	 * @param  file as the file to get the extension from
	 * @return A string containing the extension
	 */
	public static String getExt(File file) {
		String name = file.getName();
		int lastPeriodIndex = name.lastIndexOf(".");
		if(lastPeriodIndex == -1) {
			return null;
		}
		return name.substring(lastPeriodIndex);
	}

	/**        Finds a File from a folder
	 * @param  name as the name of the file
	 * @param  folder as the File as the folder
	 * @return A File if one with the name exists in the folder, else null
	 */
	public static File findFile(String name, File folder) {
		File[] files = null;
		if(folder.isDirectory()) {
		files = folder.listFiles();
		}
		if(files!=null) {
			for(int i = 0; i < files.length; i++) {
				if(name.equalsIgnoreCase(files[i].getName())) {
					return files[i];
				}
			}
		}
		return null;
	}

}