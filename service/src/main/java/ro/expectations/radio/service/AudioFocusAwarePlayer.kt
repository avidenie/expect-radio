package ro.expectations.radio.service

import android.annotation.TargetApi
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.media.AudioAttributesCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import ro.expectations.radio.common.Logger


class AudioFocusAwarePlayer(
        private val audioAttributes: AudioAttributesCompat,
        private val audioManager: AudioManager,
        private val player: SimpleExoPlayer) : ExoPlayer by player {

    private val eventListeners = mutableListOf<Player.EventListener>()

    private var shouldPlayWhenReady = false

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when(focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (shouldPlayWhenReady || player.playWhenReady) {
                   player.playWhenReady = true
                   player.volume = MEDIA_VOLUME_DEFAULT
                }
                shouldPlayWhenReady = false
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                if (player.playWhenReady) {
                    player.volume = MEDIA_VOLUME_DUCK
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                shouldPlayWhenReady = player.playWhenReady
                player.playWhenReady = false
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                AudioFocusAwarePlayer@playWhenReady = false
            }
        }
    }

    @get:RequiresApi(Build.VERSION_CODES.O)
    private val audioFocusRequest by lazy { buildFocusRequest() }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        if (playWhenReady) {
            requestAudioFocus()
        } else {
            if (shouldPlayWhenReady) {
                shouldPlayWhenReady = false
                playerEventListener.onPlayerStateChanged(false, player.playbackState)
            }
            player.playWhenReady = false
            abandonAudioFocus()
        }
    }

    override fun getPlayWhenReady(): Boolean = player.playWhenReady || shouldPlayWhenReady

    override fun addListener(listener: Player.EventListener?) {
        if (listener != null && !eventListeners.contains(listener)) {
            eventListeners += listener
        }
    }

    override fun removeListener(listener: Player.EventListener?) {
        if (listener != null && eventListeners.contains(listener)) {
            eventListeners -= listener
        }
    }

    private fun requestAudioFocus() {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestAudioFocusOreo()
        } else {
            @Suppress("deprecation")
            audioManager.requestAudioFocus(audioFocusListener,
                    audioAttributes.legacyStreamType,
                    AudioManager.AUDIOFOCUS_GAIN)
        }

        // Call the listener whenever focus is granted - even the first time!
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            shouldPlayWhenReady = true
            audioFocusListener.onAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN)
        } else {
            Logger.i(TAG, "Playback not started: audio focus request denied")
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            abandonAudioFocusOreo()
        } else {
            @Suppress("deprecation")
            audioManager.abandonAudioFocus(audioFocusListener)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocusOreo(): Int = audioManager.requestAudioFocus(audioFocusRequest)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun abandonAudioFocusOreo() = audioManager.abandonAudioFocusRequest(audioFocusRequest)

    @TargetApi(Build.VERSION_CODES.O)
    private fun buildFocusRequest(): AudioFocusRequest =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes.unwrap() as AudioAttributes)
                    .setOnAudioFocusChangeListener(audioFocusListener)
                    .build()

    private val playerEventListener = object : Player.EventListener {

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            eventListeners.forEach { it.onPlaybackParametersChanged(playbackParameters) }
        }

        override fun onSeekProcessed() {
            eventListeners.forEach { it.onSeekProcessed() }
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?,
                                     trackSelections: TrackSelectionArray?) {
            eventListeners.forEach { it.onTracksChanged(trackGroups, trackSelections) }
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
            eventListeners.forEach { it.onPlayerError(error) }
        }

        override fun onLoadingChanged(isLoading: Boolean) {
            eventListeners.forEach { it.onLoadingChanged(isLoading) }
        }

        override fun onPositionDiscontinuity(reason: Int) {
            eventListeners.forEach { it.onPositionDiscontinuity(reason) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            eventListeners.forEach { it.onRepeatModeChanged(repeatMode) }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            eventListeners.forEach { it.onShuffleModeEnabledChanged(shuffleModeEnabled) }
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            eventListeners.forEach { it.onTimelineChanged(timeline, manifest, reason) }
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            val reportPlayWhenReady = getPlayWhenReady()
            eventListeners.forEach { it.onPlayerStateChanged(reportPlayWhenReady, playbackState) }
        }
    }

    init {
        player.addListener(playerEventListener)
    }
}

private const val TAG = "AudioFocusPlayer"
private const val MEDIA_VOLUME_DEFAULT = 1.0f
private const val MEDIA_VOLUME_DUCK = 0.2f