package aIPlaylist;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import uk.co.caprica.vlcj.media.MediaRef;
import uk.co.caprica.vlcj.media.TrackType;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;
import uk.co.caprica.vlcj.player.list.MediaListPlayerEventListener;
import uk.co.caprica.vlcj.player.list.PlaybackMode;

/**
 * @author Alexander Johnston
 *         Copyright 2019
 *         A class for AIVideoPlayer where a group of media is picked from randomly to decide the media to play
 *
 *         AIPlaylist-1.0
 *         ==============
 *         
 *         Known Issues
 *         ------------
 *         1) You have to click the media player for it to take in keyboard events
 *         2) Window size resets on start
 *         
 *         Tutorial
 *         --------
 *         Pick a folder with supported media files (see vlcj4.0 documentation for supported file extensions)
 *         Use period(.) to increase the probability of the current media playing in the future
 *         Use comma(,) to decrease the probability of the current media playing in the future
 *         Use right(->) to play the next media
 *         Use left(<-) to play the previous media
 *         Use (p) to reset probabilities
 *         Use (l) to loop 
 *         Use (r) to repeat 
 *         
 *         Support
 *         -------
 *         Development of AIPlaylist is carried out by me.
 *         Free support for Open Source and non-commercial projects is generally provided.
 *         You can use Github issues at https://github.com/aljohnston112/AIPlaylist/issues
 *         or search by "AIPlaylist Github issues" for this purpose.
 *         Support for commercial projects is provided exclusively on commercial terms.
 *         
 *         License
 *         -------
 *         The AIPlaylist framework is provided under the GPL, version 3.
 *         If you want to consider a commercial license for AIPlaylist that allows you to use 
 *         and redistribute AIPlaylist without complying with the GPL 
 *         then send an email to the address below:
 *         > aljohnston112[]gm@il[]c0m
 *   
 *         Contributions
 *         -------------
 *         Contributions are welcome and will always be licensed 
 *         according to the Open Source license terms of the project (currently GPL).
 *         However, for a contribution to be accepted you must agree to transfer any copyright 
 *         so that your contribution does not impede the ability to provide commercial licenses for AIPlaylist.
 *
 *         Feel free to contact me with job offers.
 */
public class AIVideoPlayer {

	private final static double ADAPTION_PERCENTAGE = 0.5;
	
	private final JFrame jFrame = new JFrame("#1");
	private final JMenuBar jMenuBar = new JMenuBar();

	// Embedded in jFrame
	private EmbeddedMediaPlayerComponent embeddedMediaPlayerComponent = new EmbeddedMediaPlayerComponent();
	// For playing media playlists
	private MediaListPlayer mediaListPlayer = embeddedMediaPlayerComponent.mediaPlayerFactory().mediaPlayers().newMediaListPlayer();
	// Contains the media to play
	private MediaList mediaList = embeddedMediaPlayerComponent.mediaPlayerFactory().media().newMediaList();

	// Stores settings
	private File rootFile = new File(".f");
	private String currentDirectory;
	private File folder;
	private boolean subDirectories = false;
	
	// Contains the media Playlist probability function
	private Playlist playlist;
	
	// Keeps track of the video queue to allow for adapting of the probability function
	private ArrayList<Integer> indicesQueue = new ArrayList<Integer>();
	private int queuePosition = -1;
	private boolean looping = false;
	private boolean repeating = false;

	/**       Instantiates the AIVideoPlayer
	 * @param args
	 */
	public static void main(String[] args) {
		new AIVideoPlayer();
	}

	/** Sets up the GUI and starts the Playlist if a directory has been specified
	 * 
	 */
	public AIVideoPlayer() {
		loadPlaylist();
		setUpJMenu();
		setUpJFrame();
		setUpAndStartEmbeddedMediaComponent();
	}

	/** Loads a Playlist from the current directory
	 * 
	 */
	private void loadPlaylist() {
		// Get last file directory and settings
		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;
		rootFile = new File(".f");
		try {
			if(rootFile.canRead()) {
				fileInputStream = new FileInputStream(rootFile);
			}
			if(fileInputStream != null && fileInputStream.available() > 0) {
				objectInputStream = new ObjectInputStream(fileInputStream);
			}
			if(objectInputStream!=null) {
				if(objectInputStream.available() > 0)
					subDirectories = objectInputStream.readBoolean();
				StringBuffer stringBuffer = new StringBuffer();
				while(objectInputStream.available()>0) {
					stringBuffer.append(objectInputStream.readUTF());
				}
				currentDirectory = stringBuffer.toString();
				folder = new File(currentDirectory);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Gets the Playlist from the playlist file
		File file = null;
		if(currentDirectory != null) {
			file = FileAlorigthms.findFile(".playlist", new File(currentDirectory));
		}
		if(file != null) {
			fileInputStream = null;
			objectInputStream = null;
			try {
				fileInputStream = new FileInputStream(file);
				if(fileInputStream != null) {
					objectInputStream = new ObjectInputStream(fileInputStream);
				}
				if(objectInputStream!=null) {
					playlist = (Playlist) objectInputStream.readObject();
					if(playlist == null) {
						playlist = new Playlist(folder, subDirectories);
					}
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			// or makes a new Playlist if one doesn't exist
		} else if(folder != null) {
			playlist = new Playlist(folder, subDirectories);
		}
	}

	/** Saves the current directory and settings to the root file
	 * 
	 */
	private void saveDirectory() {
		FileOutputStream fileOutputStream = null;
		ObjectOutputStream objectOutputStream = null;
		rootFile.delete();
		rootFile = new File(".f");
		try {
			fileOutputStream = new FileOutputStream(rootFile);
			if(fileOutputStream != null) {
				objectOutputStream = new ObjectOutputStream(fileOutputStream);
			}
			if(objectOutputStream != null && currentDirectory != null) {
				objectOutputStream.writeBoolean(subDirectories);
				objectOutputStream.writeUTF(currentDirectory);
				objectOutputStream.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/** Saves the Playlist in the current directory
	 * 
	 */
	private void savePlaylist() {
		FileOutputStream fileOutputStream = null;
		ObjectOutputStream objectOutputStream = null;
		try {
			if(folder != null) {
				fileOutputStream = new FileOutputStream(folder.getAbsoluteFile()+"\\.playlist");
			}
			if(fileOutputStream != null) {
				objectOutputStream = new ObjectOutputStream(fileOutputStream);
			}
			if(objectOutputStream != null) {
				objectOutputStream.writeObject(playlist);
				objectOutputStream.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Sets up and starts the embedded media component
	 * 
	 */
	private void setUpAndStartEmbeddedMediaComponent() {
		if(playlist != null) {
			// Needed to get focus to work according to VLCJ documentation
			embeddedMediaPlayerComponent.mediaPlayer().input().enableKeyInputHandling(false);
			// TODO embeddedMediaPlayerComponent.grabFocus(); doesn't work
			mediaListPlayer.mediaPlayer().setMediaPlayer(embeddedMediaPlayerComponent.mediaPlayer());
			Object output = playlist.fun();
			if(output != null) {
				indicesQueue.add(playlist.getLastReturnedIndex());
				queuePosition++;
				mediaList.media().add(((File)output).getAbsolutePath(), "");
			}
			mediaListPlayer.list().setMediaList(mediaList.newMediaListRef());
			mediaListPlayer.controls().playNext();
			mediaListPlayer.events().addMediaListPlayerEventListener(new MediaListPlayerEventListener() {
				
				@Override
				public void stopped(MediaListPlayer arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void nextItem(MediaListPlayer arg0, MediaRef arg1) {
					// TODO Auto-generated method stub
					
				}
				
				/* (non-Javadoc)
				 * @see uk.co.caprica.vlcj.player.list.MediaListPlayerEventListener#mediaListPlayerFinished(uk.co.caprica.vlcj.player.list.MediaListPlayer)
				 */
				@Override
				public void mediaListPlayerFinished(MediaListPlayer arg0) {
					// Plays next picked media
					if(indicesQueue.size()-1 == queuePosition) {
						mediaListPlayer.list().media().add(((File)playlist.fun()).getAbsolutePath(), "");
						indicesQueue.add(playlist.getLastReturnedIndex());
					}
					queuePosition++;
					mediaListPlayer.controls().play(queuePosition);
				}
				
			});
			embeddedMediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventListener() {

				@Override
				public void volumeChanged(MediaPlayer arg0, float arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void videoOutput(MediaPlayer arg0, int arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void titleChanged(MediaPlayer arg0, int arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void timeChanged(MediaPlayer arg0, long arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void stopped(MediaPlayer arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void snapshotTaken(MediaPlayer arg0, String arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void seekableChanged(MediaPlayer arg0, int arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void scrambledChanged(MediaPlayer arg0, int arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void positionChanged(MediaPlayer arg0, float arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void playing(MediaPlayer arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void paused(MediaPlayer arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void pausableChanged(MediaPlayer arg0, int arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void opening(MediaPlayer arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void muted(MediaPlayer arg0, boolean arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mediaPlayerReady(MediaPlayer arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mediaChanged(MediaPlayer arg0, MediaRef arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void lengthChanged(MediaPlayer arg0, long arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void forward(MediaPlayer arg0) {
					// TODO Auto-generated method stub

				}

				/* (non-Javadoc)
				 * @see uk.co.caprica.vlcj.player.base.MediaPlayerEventListener#finished(uk.co.caprica.vlcj.player.base.MediaPlayer)
				 */
				@Override
				public void finished(MediaPlayer arg0) {
					if(looping && !repeating) {
						if(queuePosition != indicesQueue.size()-1) {
							queuePosition++;
						} else {
							queuePosition = 0;
						}
					} 
				}

				@Override
				public void error(MediaPlayer arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void elementaryStreamSelected(MediaPlayer arg0, TrackType arg1, int arg2) {
					// TODO Auto-generated method stub

				}

				@Override
				public void elementaryStreamDeleted(MediaPlayer arg0, TrackType arg1, int arg2) {
					// TODO Auto-generated method stub

				}

				@Override
				public void elementaryStreamAdded(MediaPlayer arg0, TrackType arg1, int arg2) {
					// TODO Auto-generated method stub

				}

				@Override
				public void corked(MediaPlayer arg0, boolean arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void chapterChanged(MediaPlayer arg0, int arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void buffering(MediaPlayer arg0, float arg1) {
					// TODO Auto-generated method stub

				}

				@Override
				public void backward(MediaPlayer arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void audioDeviceChanged(MediaPlayer arg0, String arg1) {
					// TODO Auto-generated method stub

				}
			});
			embeddedMediaPlayerComponent.videoSurfaceComponent().addKeyListener(new KeyListener() {

				@Override
				public void keyTyped(KeyEvent e) {

				}

				/* (non-Javadoc)
				 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
				 */
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_COMMA) {
						// Adjust probabilities so current media doesn't play as often
						playlist.bad(indicesQueue.get(queuePosition), ADAPTION_PERCENTAGE);
						System.out.println();
					} else if(e.getKeyCode() == KeyEvent.VK_PERIOD) {
						// Adjust probabilities so current media plays more often
						playlist.good(indicesQueue.get(queuePosition), ADAPTION_PERCENTAGE);
						System.out.println();
					} else if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
						// Plays next picked media
						if(indicesQueue.size()-1 == queuePosition) {
							mediaListPlayer.list().media().add(((File)playlist.fun()).getAbsolutePath(), "");
							indicesQueue.add(playlist.getLastReturnedIndex());
						}
						queuePosition++;
						mediaListPlayer.controls().play(queuePosition);
					} else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
						// Plays previous media
						mediaListPlayer.controls().playPrevious();
						if(queuePosition == 0) {
							queuePosition = indicesQueue.size()-1;
						} else {
							queuePosition--;
						}
					} else if(e.getKeyCode() == KeyEvent.VK_L) {
						if(looping) {
							mediaListPlayer.controls().setMode(PlaybackMode.DEFAULT);
							if(repeating) {
								mediaListPlayer.controls().setMode(PlaybackMode.REPEAT);
							}
							looping = false;
						} else {
							mediaListPlayer.controls().setMode(PlaybackMode.LOOP);
							looping = true;
						}
					} else if(e.getKeyCode() == KeyEvent.VK_R) {
						if(repeating) {
							mediaListPlayer.controls().setMode(PlaybackMode.DEFAULT);
							if(looping) {
								mediaListPlayer.controls().setMode(PlaybackMode.LOOP);
							}
							repeating = false;
						} else {
							mediaListPlayer.controls().setMode(PlaybackMode.REPEAT);
							repeating = true;
						}
					} else if(e.getKeyCode() == KeyEvent.VK_P) {
						playlist.resetProbabilities();
						savePlaylist();
					}
				} 

				@Override
				public void keyPressed(KeyEvent e) {

				}
			});
		}
	}

	/** Sets up the JMenu
	 * 
	 */
	private void setUpJMenu() {
		JMenu jMenu = new JMenu("New");
		JMenuItem jMenuItemOpenFolder = new JMenuItem("Open Folder");
		JMenuItem jMenuItemCheckSubDirectories = new JCheckBoxMenuItem("Check subdirectories");
		jMenuItemCheckSubDirectories.setSelected(subDirectories);
		jMenu.add(jMenuItemOpenFolder);
		jMenu.add(jMenuItemCheckSubDirectories);
		jMenuBar.add(jMenu);
		jMenuItemCheckSubDirectories.addItemListener(new ItemListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
			 */
			@Override
			public void itemStateChanged(ItemEvent e) {
				Object source = e.getItemSelectable();
				if(source == jMenuItemCheckSubDirectories) {
					if(subDirectories) {
						// Ignore sub-directories and make a new Playlist
						subDirectories = false;
						saveDirectory();
						playlist = null;
						loadPlaylist();
						savePlaylist();
						setUpAndStartEmbeddedMediaComponent();
					} else {
						// Check sub-directories and make a new Playlist
						subDirectories = true;
						File file = null;
						if(currentDirectory != null) {
							file = FileAlorigthms.findFile(".playlist", new File(currentDirectory));
						}
						if(file != null) {
							file.delete();
						}
						saveDirectory();
						playlist = null;
						loadPlaylist();
						savePlaylist();
						setUpAndStartEmbeddedMediaComponent();
					}
				}
			}
		});
		jMenuItemOpenFolder.addActionListener(new ActionListener() {

			/* (non-Javadoc)
			 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				// Checks current directory for media to play and then starts playing
				JFileChooser jFileChooser = new JFileChooser();
				jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(currentDirectory!=null) {
					jFileChooser.setCurrentDirectory(new File(currentDirectory));
				}
				int openDialogReturnValue = jFileChooser.showOpenDialog(jMenuItemOpenFolder);
				if(openDialogReturnValue == JFileChooser.APPROVE_OPTION) {
					currentDirectory = jFileChooser.getSelectedFile().getAbsolutePath();
					folder = jFileChooser.getSelectedFile();
					playlist = null;
					saveDirectory();
					loadPlaylist();
					savePlaylist();
					setUpAndStartEmbeddedMediaComponent();
				}
			}
		});
	}

	/** Sets up the JFrame
	 * 
	 */
	private void setUpJFrame() {
		jFrame.setContentPane(embeddedMediaPlayerComponent);
		jFrame.setBounds(100, 100, 600, 400);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setJMenuBar(jMenuBar);
		jFrame.setVisible(true);
		jFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent we) {
				// Save files and releases embeddedMediaPlayerComponents
				savePlaylist();
				mediaList.release();
				mediaListPlayer.release();
				embeddedMediaPlayerComponent.release();
				System.exit(0);
			}
		});

	}
}