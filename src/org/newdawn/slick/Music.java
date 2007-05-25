package org.newdawn.slick;

import java.net.URL;
import java.util.ArrayList;

import org.newdawn.slick.openal.InternalSound;
import org.newdawn.slick.openal.SoundStore;
import org.newdawn.slick.util.Log;

/**
 * A piece of music loaded and playable within the game. Only one piece of music can
 * play at any given time and a channel is reserved so music will always play. 
 *
 * @author kevin
 */
public class Music {
	/** The music currently being played or null if none */
	private static Music currentMusic;
	
	/**
	 * Poll the state of the current music. This causes streaming music
	 * to stream and checks listeners. Note that if you're using a game container
	 * this will be auto-magically called for you.
	 * 
	 * @param delta The amount of time since last poll
	 */
	public static void poll(int delta) {
		if (currentMusic != null) {
			SoundStore.get().poll(delta);
			if (!SoundStore.get().isMusicPlaying()) {
				currentMusic.fireMusicEnded();
				currentMusic = null;
			}
		}
	}
	
	/** The sound from FECK representing this music */
	private InternalSound sound;
	/** True if the music is playing */
	private boolean playing;
	/** The list of listeners waiting for notification that the music ended */
	private ArrayList listeners = new ArrayList();
	
	/**
	 * Create and load a piece of music (either OGG or MOD/XM)
	 * 
	 * @param ref The location of the music
	 * @throws SlickException
	 */
	public Music(String ref) throws SlickException {
		this(ref, false);
	}

	/**
	 * Create and load a piece of music (either OGG or MOD/XM)
	 * 
	 * @param ref The location of the music
	 * @throws SlickException
	 */
	public Music(URL ref) throws SlickException {
		this(ref, false);
	}
	
	/**
	 * Create and load a piece of music (either OGG or MOD/XM)
	 * 
	 * @param url The location of the music
	 * @param streamingHint A hint to indicate whether streaming should be used if possible
	 * @throws SlickException
	 */
	public Music(URL url, boolean streamingHint) throws SlickException {
		SoundStore.get().init();
		String ref = url.getFile();
		
		try {
			if (ref.toLowerCase().endsWith(".ogg")) {
				if (streamingHint) {
					sound = SoundStore.get().getOggStream(url);
				} else {
					sound = SoundStore.get().getOgg(url.openStream());
				}
			} else if (ref.toLowerCase().endsWith(".wav")) {
				sound = SoundStore.get().getWAV(url.openStream());
			} else if (ref.toLowerCase().endsWith(".xm") || ref.toLowerCase().endsWith(".mod")) {
				sound = SoundStore.get().getMOD(url.openStream());
			} else if (ref.toLowerCase().endsWith(".aif") || ref.toLowerCase().endsWith(".aiff")) {
				sound = SoundStore.get().getAIF(url.openStream());
			} else {
				throw new SlickException("Only .xm, .mod, .ogg, and .aif/f are currently supported.");
			}
		} catch (Exception e) {
			Log.error(e);
			throw new SlickException("Failed to load sound: "+url);
		}
	}
	
	/**
	 * Create and load a piece of music (either OGG or MOD/XM)
	 * 
	 * @param ref The location of the music
	 * @param streamingHint A hint to indicate whether streaming should be used if possible
	 * @throws SlickException
	 */
	public Music(String ref, boolean streamingHint) throws SlickException {
		SoundStore.get().init();
		
		try {
			if (ref.toLowerCase().endsWith(".ogg")) {
				if (streamingHint) {
					sound = SoundStore.get().getOggStream(ref);
				} else {
					sound = SoundStore.get().getOgg(ref);
				}
			} else if (ref.toLowerCase().endsWith(".wav")) {
				sound = SoundStore.get().getWAV(ref);
			} else if (ref.toLowerCase().endsWith(".xm") || ref.toLowerCase().endsWith(".mod")) {
				sound = SoundStore.get().getMOD(ref);
			} else if (ref.toLowerCase().endsWith(".aif") || ref.toLowerCase().endsWith(".aiff")) {
				sound = SoundStore.get().getAIF(ref);
			} else {
				throw new SlickException("Only .xm, .mod, .ogg, and .aif/f are currently supported.");
			}
		} catch (Exception e) {
			Log.error(e);
			throw new SlickException("Failed to load sound: "+ref);
		}
	}

	/**
	 * Add a listener to this music
	 * 
	 * @param listener The listener to add
	 */
	public void addListener(MusicListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener from this music
	 * 
	 * @param listener The listener to remove
	 */
	public void removeListener(MusicListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Fire notifications that this music ended
	 */
	private void fireMusicEnded() {
		playing = false;
		for (int i=0;i<listeners.size();i++) {
			((MusicListener) listeners.get(i)).musicEnded(this);
		}
	}

	/**
	 * Fire notifications that this music was swapped out
	 * 
	 * @param newMusic The new music that will be played
	 */
	private void fireMusicSwapped(Music newMusic) {
		playing = false;
		for (int i=0;i<listeners.size();i++) {
			((MusicListener) listeners.get(i)).musicSwapped(this, newMusic);
		}
	}
	/**
	 * Loop the music
	 */
	public void loop() {
		loop(1.0f,1.0f);
	}
	
	/**
	 * Play the music
	 */
	public void play() {
		play(1.0f,1.0f);
	}

	/**
	 * Play the music at a given pitch and volume
	 * 
	 * @param pitch The pitch to play the music at (1.0 = default)
	 * @param volume The volume to play the music at (1.0 = default)
	 */
	public void play(float pitch, float volume) {
		if (currentMusic != null) {
			currentMusic.stop();
			currentMusic.fireMusicSwapped(this);
		}
		
		currentMusic = this;
		sound.playAsMusic(pitch, volume, false);
		playing = true;
	}

	/**
	 * Loop the music at a given pitch and volume
	 * 
	 * @param pitch The pitch to play the music at (1.0 = default)
	 * @param volume The volume to play the music at (1.0 = default)
	 */
	public void loop(float pitch, float volume) {
		if (currentMusic != null) {
			currentMusic.stop();
			currentMusic.fireMusicSwapped(this);
		}
		
		currentMusic = this;
		sound.playAsMusic(pitch, volume, true);
		playing = true;
	}
	
	/**
	 * Pause the music playback
	 */
	public void pause() {
		playing = false;
		InternalSound.pauseMusic();
	}
	
	/**
	 * Stop the music playing
	 */
	public void stop() {
		sound.stop();
	}
	
	/**
	 * Resume the music playback
	 */
	public void resume() {
		playing = true;
		InternalSound.restartMusic();
	}
	
	/**
	 * Check if the music is being played
	 * 
	 * @return True if the music is being played
	 */
	public boolean playing() {
		return (currentMusic == this) && (playing);
	}
	
	/**
	 * Set the volume of the music
	 * 
	 * @param volume The volume to play music at. 0 - 1, 1 is Max
	 */
	public void setVolume(float volume) {
		// Bounds check
		if(volume > 1) {
			volume = 1;
		} else if(volume < 0) {
			volume = 0;
		}
		
		// This sound is being played as music
		SoundStore.get().setMusicVolume(volume);
	}
}
