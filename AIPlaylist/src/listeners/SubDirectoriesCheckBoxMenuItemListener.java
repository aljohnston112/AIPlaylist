package listeners;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import aIPlaylist.AIPlaylist;

/**
 * @author Alexander Johnston
 * @since  Copyright 2019
 *         A custom ItemListener used to listen for a subDirectories check box event triggered from a JCheckBoxMenuItem.
 */
public final class SubDirectoriesCheckBoxMenuItemListener implements ItemListener {

	// The playlist that contains the methods needed to be called when events are triggered
	private AIPlaylist playlist;

	/**       Creates a custom ItemListener used to listen for a subDirectories check box event triggered from a JCheckBoxMenuItem.
	 * @param playlist as the AIPlaylist that contains the JCheckBoxMenuItem used to open a folder.
	 */
	public SubDirectoriesCheckBoxMenuItemListener(AIPlaylist playlist){
		this.playlist = playlist;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		playlist.subDirectoriesCheckBoxMenuItemEvent(e);
	}
	
}