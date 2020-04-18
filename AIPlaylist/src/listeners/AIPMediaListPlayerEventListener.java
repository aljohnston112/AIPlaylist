package listeners;

import aIPlaylist.AIPlaylist;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;
import uk.co.caprica.vlcj.player.list.MediaListPlayerEventListener;

/**
 * @author Alexander Johnston
 * @since  Copyright 2019
 *         A custom MediaListPlayerEventListener used to listen for events triggered from a MediaListPlayer.
 */
public final class AIPMediaListPlayerEventListener implements MediaListPlayerEventListener {

	// The playlist that contains the methods needed to be called when events are triggered
	AIPlaylist playlist;

	/**       Creates a custom MediaListPlayerEventListener used to listen for events triggered from a MediaListPlayer.
	 * @param playlist as the playlist that registers this listener with a MediaListPlayer
	 */
	public AIPMediaListPlayerEventListener(AIPlaylist playlist){
		this.playlist = playlist;
	}

	@Override
	public void stopped(MediaListPlayer arg0) {
	}

	@Override
	public void nextItem(MediaListPlayer arg0, MediaRef arg1) {
	}

	/* (non-Javadoc)
	 * @see uk.co.caprica.vlcj.player.list.MediaListPlayerEventListener#mediaListPlayerFinished(uk.co.caprica.vlcj.player.list.MediaListPlayer)
	 */
	@Override
	public void mediaListPlayerFinished(MediaListPlayer arg0) {
		playlist.mediaListPlayerFinished();
	}
	
}