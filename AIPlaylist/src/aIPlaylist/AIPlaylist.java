package aIPlaylist;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.nio.file.FileSystems;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import aIPlaylist.RandomPlaylist;

import algorithms.FileAlorigthms;

import listeners.AIPKeyListener;
import listeners.AIPMediaListPlayerEventListener;
import listeners.AIPMediaPlayerEventListener;
import listeners.AIPWindowAdapter;
import listeners.OpenFolderMenuItemListener;
import listeners.SubDirectoriesCheckBoxMenuItemListener;
import listeners.WatchSerciveThread;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.medialist.MediaList;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.list.MediaListPlayer;
import uk.co.caprica.vlcj.player.list.PlaybackMode;

/**
 * @author Alexander Johnston
 * @since  Copyright 2019
 *         AIPlaylist picks from a group of media files using a probability function, that can be adjusted based on feedback.
 *
 *         AIPlaylist-1.0
 *         ==============
 *         
 *         Known Issues
 *         ------------
 *         1) You have to click the media player for it to take in keyboard events.
 *         2) If you change the file structure, the program will not update until you delete the .playlist file.
 *         
 *         Tutorial
 *         --------
 *         Pick a folder with supported media files (mp4, 3gp, asf, wmv, au, avi, flv, mov, ogm, ogg, 
 *                                                   mkv, mka, ts, mpg, mp3, mp2, nsc, nsv, nut, ra, 
 *                                                   ram, rm, rv, rmbv, a52, dts, aac, flac, dv, vid, 
 *                                                   tta, tac, ty, wav, dts, xa).
 *         Use period(.) to increase the probability of the current media playing in the future.
 *         Use comma(,) to decrease the probability of the current media playing in the future.
 *         Use right(->) to play the next media.
 *         Use left(<-) to play the previous media.
 *         Use (p) to reset probabilities.
 *         Use (l) to loop. 
 *         Use (r) to repeat. 
 *         
 *         Support
 *         -------
 *         Development of AIPlaylist is carried out by Alexander Johnston.
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
 */
public class AIPlaylist {

	// TODO put up toast for key presses

	// For debugging
	private Logger aIPlaylistLogger = Logger.getLogger("aIPlaylist.AIPlaylist");

	/**
	 * @return the Logger for this AIPlaylist
	 */
	public Logger getLogger() {
		return aIPlaylistLogger;
	}

	// The percentage of the probability of the last played media appearing getting adjusted by on feedback.
	// If there is a greater than 50% chance, the increase becomes ADAPTION_PERCENTAGE*(100%-probability).
	private final static double ADAPTION_PERCENTAGE = .5;

	// For UI
	private final JFrame jFrame = new JFrame("AIPlaylist");
	private final JMenuBar jMenuBar = new JMenuBar();
	private final JMenu jMenu = new JMenu("New");
	private final JMenuItem jMenuItemOpenFolder = new JMenuItem("Open Folder");
	private final JMenuItem jMenuItemCheckSubDirectories = new JCheckBoxMenuItem("Check subdirectories");

	// UI listeners
	private final OpenFolderMenuItemListener openFolderButtonListener = new OpenFolderMenuItemListener(this);
	private final SubDirectoriesCheckBoxMenuItemListener subDirectoriesCheckBoxListener = new SubDirectoriesCheckBoxMenuItemListener(this);
	private final AIPWindowAdapter aIPWindowAdapter = new AIPWindowAdapter(this);
	private final AIPKeyListener keyListener = new AIPKeyListener(this);
	private WatchService watchService;
	private ExecutorService executionService = Executors.newSingleThreadExecutor();

	// Embedded in jFrame
	private final EmbeddedMediaPlayerComponent embeddedMediaPlayerComponent = new EmbeddedMediaPlayerComponent();
	// For playing MediaLists
	private final MediaListPlayer mediaListPlayer = embeddedMediaPlayerComponent.mediaPlayerFactory().mediaPlayers().newMediaListPlayer();
	// Contains the media to play
	private final MediaList mediaList = embeddedMediaPlayerComponent.mediaPlayerFactory().media().newMediaList();

	// VLCJ listeners
	private final AIPMediaPlayerEventListener mediaPlayerEventListener = new AIPMediaPlayerEventListener(this);
	private final AIPMediaListPlayerEventListener mediaListPlayerEventListener = new AIPMediaListPlayerEventListener(this);

	// Unsaved setting
	private boolean started = false;

	// Stores settings
	private final File rootFile = new File(".f");

	// Saved settings
	private boolean subDirectories = false;
	private boolean looping = false;
	private boolean repeating = false;
	private int jFrameX = 0;
	private int jFrameY = 0;
	private int jFrameHeight = 720;
	private int jFrameWidth = 960;
	private File folder;

	// Contains the Playlist 
	private RandomPlaylist playlist;

	// Keeps track of the media queue to allow for adapting the probability function
	private LinkedList<File> previousFiles = new LinkedList<File>();

	// For iterating over the File queue
	private ListIterator<File> previousFilesIterator = previousFiles.listIterator();

	/**       Instantiates the AIPlaylist.
	 * @param args is unused.
	 */
	public static void main(String[] args) {
		new AIPlaylist();
	}

	/** Sets up the GUI and starts the Playlist if a folder has been specified.
	 * 
	 */
	public AIPlaylist() {
		try {
			aIPlaylistLogger.addHandler(new FileHandler(".log"));
		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
		aIPlaylistLogger.getHandlers()[0].setFormatter(new SimpleFormatter());
		aIPlaylistLogger.setLevel(Level.FINEST);
		new NativeDiscovery().discover();
		loadSettings();
		loadPlaylist(); 
		getTopMedia();
		setUpJMenu();
		setUpJFrame();
		setUpMediaPlayer();
		if(playlist != null)
			startMediaPlayer();
	}

	/** Prints the media that has the greatest probability of appearing to the console.
	 * 
	 */
	private void getTopMedia() {
		double max = 0;
		File f = null;
		for(Entry<File, Double> e : playlist.getProbFun().getParentMap().entrySet()) {
			if(e.getValue() > max) {
				max = e.getValue();
				f = e.getKey();
			}
		}
		if(f != null) {
			System.out.print(f.getAbsolutePath());
			System.out.print("\n");
			System.out.print(max);
			System.out.print("\n");
		}
	}

	/** Saves the settings to the root file.
	 * 
	 */
	private void saveSettings() {
		try {
			rootFile.delete();
			rootFile.createNewFile();
		} catch (IOException e1) {
			System.out.print(String.format("Unable to save settings in directory: " + rootFile.getParent() + "\n"));
			e1.printStackTrace();
		}
		try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(rootFile))){
			aIPlaylistLogger.finest("Saving settings");
			objectOutputStream.writeBoolean(subDirectories);
			objectOutputStream.writeBoolean(looping);
			objectOutputStream.writeBoolean(repeating);
			objectOutputStream.writeInt(jFrame.getX());
			objectOutputStream.writeInt(jFrame.getY());
			objectOutputStream.writeInt(jFrame.getHeight());
			objectOutputStream.writeInt(jFrame.getWidth());
			objectOutputStream.writeObject(folder);
			objectOutputStream.flush();
		} catch (FileNotFoundException e) {
			System.out.print(String.format("Unable to create file: " + rootFile.getAbsolutePath() + "\n"));
			e.printStackTrace();
		} catch (IOException e) {
			System.out.print(String.format("Problem writing to " + rootFile.getAbsolutePath() + "\n"));
			e.printStackTrace();
		}		
	}

	/** Saves the Playlist in folder.
	 * 
	 */
	private void savePlaylist() {
		try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(folder.getAbsoluteFile()+"\\.playlist"));) {
			aIPlaylistLogger.finest("Saving playlist");
			objectOutputStream.writeObject(playlist);
			objectOutputStream.flush();
		} catch (FileNotFoundException e) {
			System.out.print(String.format("Unable to find folder: " + folder.getAbsolutePath() + "\n"));
			e.printStackTrace();
		} catch (IOException e) {
			System.out.print(String.format("Problem writing to " + folder.getAbsolutePath() + "\n"));
			e.printStackTrace();
		}
	}

	/** Loads the settings from the root file.
	 * 
	 */
	private void loadSettings() {
		if(rootFile.canRead()) {
			try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(rootFile))) {
				if(objectInputStream.available() > 0) {
					aIPlaylistLogger.finest("Loading settings");
					subDirectories = objectInputStream.readBoolean();
					looping = objectInputStream.readBoolean();
					repeating = objectInputStream.readBoolean();
					jFrameX = objectInputStream.readInt();
					jFrameY = objectInputStream.readInt();
					jFrameHeight = objectInputStream.readInt();
					jFrameWidth = objectInputStream.readInt();
					try {
						folder = (File) objectInputStream.readObject();
					} catch (ClassNotFoundException e) {
						System.out.print("Unable to read settings file: " + folder.getAbsolutePath() + "\n");
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e) {
				System.out.print(String.format("Unable to create file: " + rootFile.getAbsolutePath() + "\n"));
				e.printStackTrace();
			} catch (IOException e) {
				System.out.print(String.format("Problem reading from " + rootFile.getAbsolutePath() + "\n"));
				e.printStackTrace();
			}
		}
	}

	/** Registers the folder, and it's sub-directories if required.
	 * 
	 */
	private void registerFileListener() {
		try {
			watchService = FileSystems.getDefault().newWatchService();
			executionService.execute(new WatchSerciveThread(this, watchService, subDirectories));
			addWatchService(folder);
		} catch (IOException e) {
			System.out.print(String.format("Problem setting up WatchService on " + folder.getAbsolutePath() + "\n"));
			e.printStackTrace();
		}
	}

	/**        Registers a WatchService with a folder.
	 * @param  folder as the folder to watch.
	 * @throws IOException if there is a problem creating the WatchService.
	 */
	private void addWatchService(File folder) {
		try {
			folder.toPath().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			System.out.print(String.format("Problem adding WatchService to " + folder.getAbsolutePath() + "\n"));
			e.printStackTrace();
		}
		if(subDirectories) {
			List<File> files = Arrays.asList(folder.listFiles());
			for(File f : files) {
				if(f.isDirectory()) {
					addWatchService(f);
				}
			}
		}		
	}

	/**        Re-instantiates the WatchService. Used when subDirectories is changed.
	 * @param  watchService as the WatchService to re-instantiate.
	 * @param  folder as the folder to add the WatchService to.
	 */
	private void resetWatchService() {
		try {
			executionService.shutdownNow();
			try {
				executionService.awaitTermination(50, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				System.out.print(String.format("Problem shutting down WatchService thread\n"));
				e.printStackTrace();
			}
			executionService = Executors.newSingleThreadExecutor();
			watchService.close();
			watchService = FileSystems.getDefault().newWatchService();
			executionService.execute(new WatchSerciveThread(this, watchService, subDirectories));
		} catch (IOException e) {
			System.out.print(String.format("Problem re-instantiating WatchService on " + folder.getAbsolutePath() + "\n"));
			e.printStackTrace();
		}
		addWatchService(folder);
	}

	public void watchServiceThreadInterrupted() {
		new InterruptedException("WatchService thread was interrupted\n").printStackTrace();
	}

	/** Loads the playlist from the .playlist file in folder.
	 * 
	 */
	private void loadPlaylist() {
		if(folder!= null && folder.canRead()) {
			// Gets the Playlist from the playlist file, if it exists, else makes a new Playlist
			File file = FileAlorigthms.findFile(".playlist", folder).orElse(null);
			if(file != null) {
				try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
					aIPlaylistLogger.finest("Loading playlist");
					playlist = (RandomPlaylist) objectInputStream.readObject();
					// Makes sure the number of files in the directory matches the number of files in the playlist
					checkPlaylistFiles();	
				} catch (FileNotFoundException e) {
					System.out.print(String.format("Unable to find file: " + rootFile.getAbsolutePath() + "\n"));
					e.printStackTrace();
				} catch (IOException e) {
					System.out.print(String.format("Problem reading from " + rootFile.getAbsolutePath() + "\n"));
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					System.out.print(String.format("Corrupt file: " + rootFile.getAbsolutePath())+"\n Deleting the file" + "\n");
					file.delete();
					e.printStackTrace();
				}
			} else if(playlist == null) {
				aIPlaylistLogger.finest("Creating new playlist");
				try {
					playlist = new RandomPlaylist(folder, subDirectories);
				} catch (Exception e) {

				}
			}
			registerFileListener();
		}
	}

	/**       Updates this AIPlaylist to reflect changes in the file structure.
	 *
	 */
	public void checkPlaylistFiles() {
		List<File> files = FileAlorigthms.getMediaFiles(folder, subDirectories);
		for(File f : files) {
			if(!playlist.getProbFun().getParentMap().containsKey(f)) {
				playlist.getProbFun().addToAll(f);
			}
		}
		Iterator<File> it = playlist.getProbFun().getParentMap().keySet().iterator();
		ArrayList<File> al = new ArrayList<File>();
		while(it.hasNext()) {
			File f = it.next();
			if(!files.contains(f)) {
				al.add(f);
			}
		}
		for(File f : al) {
			playlist.getProbFun().removeFromAll(f);
		}
	}

	/** Sets up the VLCJ components.
	 * 
	 */
	private void setUpMediaPlayer() {
		aIPlaylistLogger.finest("Setting up media player");
		// Needed to get focus to work according to VLCJ documentation
		embeddedMediaPlayerComponent.mediaPlayer().input().enableKeyInputHandling(false);
		embeddedMediaPlayerComponent.requestFocusInWindow();
		mediaListPlayer.mediaPlayer().setMediaPlayer(embeddedMediaPlayerComponent.mediaPlayer());
		mediaListPlayer.list().setMediaList(mediaList.newMediaListRef());
		mediaListPlayer.events().addMediaListPlayerEventListener(mediaListPlayerEventListener);
		embeddedMediaPlayerComponent.mediaPlayer().events().addMediaPlayerEventListener(mediaPlayerEventListener);
		embeddedMediaPlayerComponent.videoSurfaceComponent().addKeyListener(keyListener);
		if(looping) {
			aIPlaylistLogger.finest("Looping");
			mediaListPlayer.controls().setMode(PlaybackMode.LOOP);
		}
		if(repeating) {
			aIPlaylistLogger.finest("Repeating");
			mediaListPlayer.controls().setMode(PlaybackMode.REPEAT);
		}

	}

	/** Starts the media player.
	 * 
	 */
	private void startMediaPlayer() {
		aIPlaylistLogger.finest("Media player started");
		started = true;
		addMedia();
		mediaListPlayer.controls().play(0);
		printPreviousIndex();
	}

	/** Plays the next media.
	 * 
	 */
	public void playNext() {
		if(!previousFilesIterator.hasNext() && !looping && !repeating) 
			addMedia();
		else  
			iteratorNext();
		aIPlaylistLogger.finest("Playing next");
		mediaListPlayer.controls().playNext();
		printPreviousIndex();
	}

	/** Plays the previous media.
	 * 
	 */
	public void playPrevious() {
		if(previousFilesIterator.previousIndex() == 0 && !looping && !repeating) {
			aIPlaylistLogger.finest("Playing first");
			mediaListPlayer.controls().play(0);
		} else {
			aIPlaylistLogger.finest("Playing previous");
			mediaListPlayer.controls().playPrevious();
			iteratorPrevious();
		}
		printPreviousIndex();
	}

	/** Adds one new media to the queue.
	 * 
	 */
	private void addMedia() {
		aIPlaylistLogger.finest("Adding media");
		File f = playlist.getProbFun().fun();
		mediaList.media().add((f).getAbsolutePath(), "");
		previousFilesIterator.add(f);		
	}

	/** Decrements the iterator.
	 * 
	 */
	private void iteratorPrevious() {
		if(!repeating) {
			if(previousFilesIterator.hasPrevious()) {
				aIPlaylistLogger.finest("Decrementing iterator");
				previousFilesIterator.previous();
			}
			else if(looping) {
				aIPlaylistLogger.finest("Looping iterator to back");
				while(previousFilesIterator.hasNext() && previousFilesIterator.nextIndex() != previousFiles.size())
					previousFilesIterator.next();
			}	
		}
	}

	/** Increments the iterator.
	 * 
	 */
	private void iteratorNext() {
		if(!repeating) {
			if(previousFilesIterator.hasNext()) {
				aIPlaylistLogger.finest("Incrementing iterator");
				previousFilesIterator.next();
			} else if(looping) {
				aIPlaylistLogger.finest("Looping iterator to front");
				while(previousFilesIterator.hasPrevious() && previousFilesIterator.previousIndex() != 0)
					previousFilesIterator.previous();
			}
		}
	}


	/** Prints the index in the queue and the file name of the previous media.
	 * 
	 */
	private void printPreviousIndex() {
		if(previousFilesIterator.hasPrevious()) {
			aIPlaylistLogger.finest(String.format("Index: %s and File: %s", previousFilesIterator.previousIndex(), previousFilesIterator.previous().getName()));
			previousFilesIterator.next();		
		} else {
			previousFilesIterator.next();	
			aIPlaylistLogger.finest(String.format("Index: %s and File: %s", previousFilesIterator.previousIndex(), previousFilesIterator.previous().getName()));
		}
	}

	/** Sets up the JMenu.
	 * 
	 */
	private void setUpJMenu() {
		aIPlaylistLogger.finest("Setting up JMenu");
		jMenuBar.add(jMenu);
		jMenu.add(jMenuItemOpenFolder);
		jMenu.add(jMenuItemCheckSubDirectories);
		jMenuItemCheckSubDirectories.setSelected(subDirectories);
		jMenuItemCheckSubDirectories.addItemListener(subDirectoriesCheckBoxListener);
		jMenuItemOpenFolder.addActionListener(openFolderButtonListener);
	}

	/** Sets up the JFrame.
	 * 
	 */
	private void setUpJFrame() {
		aIPlaylistLogger.finest("Setting up JFrame");
		jFrame.setBounds(jFrameX, jFrameY, jFrameWidth, jFrameHeight);
		jFrame.setJMenuBar(jMenuBar);
		jFrame.setContentPane(embeddedMediaPlayerComponent);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setVisible(true);
		jFrame.addWindowListener(aIPWindowAdapter);
	}

	/**       Opens a folder the user picks, loads media files, and starts playing them when the OpenFolderMenuItemListener is triggered.
	 * @param e as the ActionEvent that was triggered.
	 */
	public void openFolderMenuItemEvent(ActionEvent e) {
		Object source = e.getSource();
		if(source == jMenuItemOpenFolder) {
			JFileChooser jFileChooser = new JFileChooser();
			jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if(folder!= null && folder.canRead()) {
				jFileChooser.setCurrentDirectory(folder);
			}
			int openDialogReturnValue = jFileChooser.showOpenDialog(jMenuItemOpenFolder);
			// Checks current directory for media to play and then starts playing
			if(openDialogReturnValue == JFileChooser.APPROVE_OPTION) {
				folder = jFileChooser.getSelectedFile();
				registerFileListener();
				playlist = null;
				loadPlaylist();
				if(started) {
					aIPlaylistLogger.finest("Clearing mediaList and iterator");
					mediaList.media().clear();
					previousFiles = new LinkedList<File>();
					previousFilesIterator = previousFiles.listIterator();	
				} 	
				if(playlist != null) {
					startMediaPlayer();
				}
			}
		}
	}

	/**       Will switched whether sub-directories are checked or not and then update the playlist to reflect the change 
	 *        when the SubDirectoriesCheckBoxMenuItemListener is triggered.
	 * @param e as the ItemEvent that was triggered.
	 */
	public void subDirectoriesCheckBoxMenuItemEvent(ItemEvent e) {
		Object source = e.getItemSelectable();
		if(source == jMenuItemCheckSubDirectories) {
			if(subDirectories) {
				aIPlaylistLogger.finest("Removing subdirectories");
				// Ignore sub-directories and make a new Playlist
				subDirectories = false;
				playlist = null;
				loadPlaylist();
				resetWatchService();
			} else {
				aIPlaylistLogger.finest("Adding subdirectories");
				// Check sub-directories and make a new Playlist
				subDirectories = true;
				playlist = null;
				loadPlaylist();
			}
		}		
	}	

	/** Called from the AIPMediaPlayerEventListener when the MediaPlayer is finished.
	 * 
	 */
	public void mediaPlayerFinished() {
		if(!previousFilesIterator.hasNext() && !looping && !repeating) {
			aIPlaylistLogger.finest("MediaPlayer Finished");
			addMedia();
			printPreviousIndex();
		} else {
			iteratorNext();
		}

	}

	/** Called when the MediaListPlayer is finished.
	 * 
	 */
	public void mediaListPlayerFinished() {
		aIPlaylistLogger.finest("MediaListPlayer Finished");
		if(!previousFilesIterator.hasNext() && looping) {
			aIPlaylistLogger.finest("Playing 1st");
			mediaListPlayer.controls().play(0);
		} else {
			playNext();		
		}
	}

	/** Makes the currently playing media less likely to appear in the future.
	 * 
	 */
	public void bad() {
		// Adjust probabilities so current media doesn't play as often
		if(previousFilesIterator.hasPrevious()) {
			StringBuilder sb = new StringBuilder();
			for(File i : previousFiles) {
				sb.append(i.getName());
				sb.append(",");
			}
			sb.append("\n");
			aIPlaylistLogger.finest(sb.toString());
			File f = previousFilesIterator.previous();
			aIPlaylistLogger.finest("Decreasing probability of file: " + f.getName());
			aIPlaylistLogger.finest(String.format("%f", playlist.getProbFun().getParentMap().get(f)));
			playlist.getProbFun().bad(previousFilesIterator.next(), ADAPTION_PERCENTAGE);
		} else {
			if(looping) {
				aIPlaylistLogger.finest("Decreasing probability of file: " + previousFiles.getLast());
				aIPlaylistLogger.finest(String.format("%f", playlist.getProbFun().getParentMap().get(previousFiles.getLast())));
				playlist.getProbFun().bad(previousFiles.getLast(), ADAPTION_PERCENTAGE);
			} else {
				aIPlaylistLogger.finest("Decreasing probability of file: " + previousFiles.getFirst());
				aIPlaylistLogger.finest(String.format("%f", playlist.getProbFun().getParentMap().get(previousFiles.getFirst())));
				playlist.getProbFun().bad(previousFiles.getFirst(), ADAPTION_PERCENTAGE);
			}
		}		
	}

	/** Makes the currently playing media more likely to appear in the future.
	 * 
	 */
	public void good() {
		// Adjust probabilities so current media plays more often
		if(previousFilesIterator.hasPrevious()) {
			StringBuilder sb = new StringBuilder();
			for(File i : previousFiles) {
				sb.append(i.getName());
				sb.append(",");
			}
			sb.append("\n");
			aIPlaylistLogger.finest(sb.toString());
			File f = previousFilesIterator.previous();
			aIPlaylistLogger.finest("Increasing probability of file: " + f.getName());
			aIPlaylistLogger.finest(String.format("%f", playlist.getProbFun().getParentMap().get(f)));
			playlist.getProbFun().good(previousFilesIterator.next(), ADAPTION_PERCENTAGE);
		} else {
			if(looping) {
				aIPlaylistLogger.finest("Increasing probability of file: " + previousFiles.getLast());
				aIPlaylistLogger.finest(String.format("%f", playlist.getProbFun().getParentMap().get(previousFiles.getLast())));
				playlist.getProbFun().good(previousFiles.getLast(), ADAPTION_PERCENTAGE);
			} else {
				aIPlaylistLogger.finest("Increasing probability of file: " + previousFiles.getFirst());
				aIPlaylistLogger.finest(String.format("%f", playlist.getProbFun().getParentMap().get(previousFiles.getFirst())));
				playlist.getProbFun().good(previousFiles.getFirst(), ADAPTION_PERCENTAGE);
			}
		}		
	}

	/** Switches whether looping is enabled or not.
	 * 
	 */
	public void loopSwitch() {
		if(looping) {
			aIPlaylistLogger.finest("Turning loop off");
			mediaListPlayer.controls().setMode(PlaybackMode.DEFAULT);
			if(repeating) {
				mediaListPlayer.controls().setMode(PlaybackMode.REPEAT);
			}
			looping = false;
		} else {
			aIPlaylistLogger.finest("Turning loop on");
			mediaListPlayer.controls().setMode(PlaybackMode.LOOP);
			looping = true;
		}		
	}

	/** Switches whether repeat is enabled or not.
	 * 
	 */
	public void repeatSwitch() {
		if(repeating) {
			aIPlaylistLogger.finest("Turning repeat off");
			mediaListPlayer.controls().setMode(PlaybackMode.DEFAULT);
			if(looping) {
				aIPlaylistLogger.finest("Turning loop on");
				mediaListPlayer.controls().setMode(PlaybackMode.LOOP);
			}
			repeating = false;
		} else {
			aIPlaylistLogger.finest("Turning repeat on");
			mediaListPlayer.controls().setMode(PlaybackMode.REPEAT);
			repeating = true;
		}		
	}

	/** Resets the probabilities so all the media files have the same chance of appearing.
	 * 
	 */
	public void resetProbabilities() {
		playlist.getProbFun().clearProbs();
	}

	/** Saves resources and closes the application.
	 * 
	 */
	public void close() {
		if(playlist != null) {
			savePlaylist();
		}
		saveSettings();
		executionService.shutdownNow();
		try {
			executionService.awaitTermination(50, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			System.out.print(String.format("Problem shutting down WatchService thread\n"));
			e.printStackTrace();
		}
		try {
			watchService.close();
		} catch (IOException e) {
			System.out.print(String.format("Problem shutting down WatchService\n"));
			e.printStackTrace();
		}
		mediaList.release();
		mediaListPlayer.release();
		embeddedMediaPlayerComponent.release();
		aIPlaylistLogger.finest("Closing application");
		System.exit(0);
	}

}