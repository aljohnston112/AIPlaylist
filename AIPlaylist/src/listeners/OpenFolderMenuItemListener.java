package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import aIPlaylist.AIPlaylist;

/**
 * @author Alexander Johnston
 * @since  Copyright 2019
 *         A custom ActionListener used to listen for an open folder event triggered from a JMenuItem.
 */
public final class OpenFolderMenuItemListener implements ActionListener {

	// The playlist that contains the methods needed to be called when events are triggered
	private AIPlaylist playlist;
	
	/**       Creates a custom ActionListener used to listen for an open folder event triggered from a JMenuItem.
	 * @param playlist as the AIPlaylist that contains the JMenuItem used to open a folder.
	 */
	public OpenFolderMenuItemListener(AIPlaylist playlist){
		this.playlist = playlist;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		playlist.openFolderMenuItemEvent(e);
	}
	
}