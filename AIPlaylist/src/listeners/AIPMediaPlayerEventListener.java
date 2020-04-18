package listeners;

import aIPlaylist.AIPlaylist;
import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;

/**
 * @author Alexander Johnston
 * @since  Copyright 2019
 *         A custom MediaPlayerEventListener used to listen for MediaPlayerEvents triggered from an EmbeddedMediaComponent's MediaPlayer.
 */
public final class AIPMediaPlayerEventListener implements MediaPlayerEventListener  {

	// The playlist that contains the methods needed to be called when events are triggered
	AIPlaylist playlist;

	/**       Creates a custom MediaPlayerEventListener used to listen for MediaPlayerEvents triggered from an EmbeddedMediaComponent's MediaPlayer.
	 * @param playlist as the playlist that registers this listener with an EmbeddedMediaComponent's MediaPlayer.
	 */
	public AIPMediaPlayerEventListener(AIPlaylist playlist){
		this.playlist = playlist;
	}

	/* (non-Javadoc)
	 * @see uk.co.caprica.vlcj.player.base.MediaPlayerEventListener#finished(uk.co.caprica.vlcj.player.base.MediaPlayer)
	 */
	@Override
	public void finished(MediaPlayer arg0) {
		playlist.mediaPlayerFinished();
	}

	@Override
	public void volumeChanged(MediaPlayer arg0, float arg1) {
	}

	@Override
	public void videoOutput(MediaPlayer arg0, int arg1) {
	}

	@Override
	public void titleChanged(MediaPlayer arg0, int arg1) {
	}

	@Override
	public void timeChanged(MediaPlayer arg0, long arg1) {
	}

	@Override
	public void stopped(MediaPlayer arg0) {
	}

	@Override
	public void snapshotTaken(MediaPlayer arg0, String arg1) {
	}

	@Override
	public void seekableChanged(MediaPlayer arg0, int arg1) {
	}

	@Override
	public void scrambledChanged(MediaPlayer arg0, int arg1) {
	}

	@Override
	public void positionChanged(MediaPlayer arg0, float arg1) {
	}

	@Override
	public void playing(MediaPlayer arg0) {
	}

	@Override
	public void paused(MediaPlayer arg0) {
	}

	@Override
	public void pausableChanged(MediaPlayer arg0, int arg1) {
	}

	@Override
	public void opening(MediaPlayer arg0) {
	}

	@Override
	public void muted(MediaPlayer arg0, boolean arg1) {
	}

	@Override
	public void mediaPlayerReady(MediaPlayer arg0) {
	}

	@Override
	public void mediaChanged(MediaPlayer arg0, MediaRef arg1) {
	}

	@Override
	public void lengthChanged(MediaPlayer arg0, long arg1) {
	}

	@Override
	public void forward(MediaPlayer arg0) {
	}

	@Override
	public void error(MediaPlayer arg0) {
	}

	@Override
	public void elementaryStreamSelected(MediaPlayer arg0, TrackType arg1, int arg2) {
	}

	@Override
	public void elementaryStreamDeleted(MediaPlayer arg0, TrackType arg1, int arg2) {
	}

	@Override
	public void elementaryStreamAdded(MediaPlayer arg0, TrackType arg1, int arg2) {
	}

	@Override
	public void corked(MediaPlayer arg0, boolean arg1) {
	}

	@Override
	public void chapterChanged(MediaPlayer arg0, int arg1) {
	}

	@Override
	public void buffering(MediaPlayer arg0, float arg1) {
	}

	@Override
	public void backward(MediaPlayer arg0) {
	}

	@Override
	public void audioDeviceChanged(MediaPlayer arg0, String arg1) {
	}
	
}