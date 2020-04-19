package listeners;

import java.nio.file.ClosedWatchServiceException;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import aIPlaylist.AIPlaylist;

/**
 * @author Alexander Johnston
 * @since  Copyright 2020
 *         A custom Thread used to poll for WatchService events triggered by FileSystem modification.
 */
public class WatchSerciveThread extends Thread {
	
	// Reference for callback
	final AIPlaylist playlist;
	 
	// WatchService to poll for events
	final WatchService watchService;
	
	// Whether or not sub-directories get checked
	final boolean subDirectories;
	
	/**       Creates a custom Thread used to poll for WatchService events triggered by FileSystem modification.
	 * @param playlist as the AIPlayist that contains the callback method for updating.
	 * @param watchService as the watchService registered with the folder used by playlist.
	 * @param subDirectories as whether or not subDirectories are checked.
	 */
	public WatchSerciveThread(AIPlaylist playlist, WatchService watchService, boolean subDirectories){
		this.watchService = watchService;
		this.subDirectories = subDirectories;
		this.playlist = playlist;
	}

	@Override
	public void run() {
		while(!isInterrupted()) {
			WatchKey wk = null;
			try {
				wk = watchService.take();
				for (WatchEvent<?> event: wk.pollEvents()) {
			        if(event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
						playlist.checkPlaylistFiles();
			        }
				}
			} catch (ClosedWatchServiceException e) {
				Thread.currentThread().interrupt();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		Thread.currentThread().interrupt();
		playlist.watchServiceThreadInterrupted();
	}

}