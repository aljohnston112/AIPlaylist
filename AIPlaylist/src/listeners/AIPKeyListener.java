package listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import aIPlaylist.AIPlaylist;

/**
 * @author Alexander Johnston
 * @since  Copyright 2019
 *         A custom KeyListener used to listen for key events triggered from an EmbeddedMediaPlayerComponent's VideoSurfaceComponent
 */
public final class AIPKeyListener implements KeyListener {

	// The playlist that contains the methods needed to be called when events are triggered
	AIPlaylist playlist;

	/**       Creates a custom KeyListener used to listen for key events triggered from an EmbeddedMediaPlayerComponent's VideoSurfaceComponent.
	 * @param playlist as the playlist that registers this listener with an embeddedMediaPlayerComponent's videoSurfaceComponent.
	 */
	public AIPKeyListener(AIPlaylist playlist){
		this.playlist = playlist;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	/* (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_COMMA) {
			playlist.bad();
		} else if(e.getKeyCode() == KeyEvent.VK_PERIOD) {
			playlist.good();
		} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
			playlist.getLogger().finest("Right");
			playlist.playNext();
		} else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
			playlist.getLogger().finest("Left");
			playlist.playPrevious();
		} else if(e.getKeyCode() == KeyEvent.VK_L) {
			playlist.loopSwitch();
		}
		else if(e.getKeyCode() == KeyEvent.VK_R) {
			playlist.repeatSwitch();
		} else if(e.getKeyCode() == KeyEvent.VK_P) {
			playlist.getLogger().finest("Resetting probabilities");
			playlist.resetProbabilities();
		}
	} 

	@Override
	public void keyPressed(KeyEvent e) {
		
	}
	
}