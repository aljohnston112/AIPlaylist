package listeners;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import aIPlaylist.AIPlaylist;

/**
 * @author Alexander Johnston
 * @since  Copyright 2019
 *         A custom class used to listen for WindowAdapter events triggered from a JFrame.
 */
public final class AIPWindowAdapter extends WindowAdapter {

	// The playlist that contains the methods needed to be called when events are triggered
	AIPlaylist playlist;

	/**       Creates a custom class used to listen for WindowAdapter events triggered from a JFrame.
	 * @param playlist as the playlist that registers this listener with a JFrame.
	 */
	public AIPWindowAdapter(AIPlaylist playlist) {
		this.playlist = playlist;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent we) {
		playlist.close();
	}
	
}