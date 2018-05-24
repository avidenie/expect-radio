package ro.expectations.radio.service.playback

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import ro.expectations.radio.service.RadioService
import ro.expectations.radio.utilities.Logger

class LocalPlayback(private val radioService: RadioService) : Playback, Player.EventListener {

    companion object {
        private val TAG = LocalPlayback::class.java.simpleName
    }

    private var player: SimpleExoPlayer? = null

    override fun play(queueItem: MediaSessionCompat.QueueItem) {

        // Start the service and set the media session as active
        radioService.onPlaybackStart()

        // Update metadata
        radioService.onMetadataUpdated(MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, queueItem.description.mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, queueItem.description.title.toString())
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, queueItem.description.subtitle.toString())
                .build())

        // Initialise the player
        if (player == null) {
            val renderersFactory = DefaultRenderersFactory(radioService)
            val bandwidthMeter = DefaultBandwidthMeter()
            val trackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
            val trackSelector = DefaultTrackSelector(trackSelectionFactory)

            player = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector)

            player?.addListener(this)
        }

        // Start the player
        player?.playWhenReady = true

        val streamUrl = queueItem.description.extras?.getString("__SOURCE__")

        Logger.e(TAG, streamUrl ?: "NULL")

        val dataSourceFactory = DefaultDataSourceFactory(radioService, "ExoplayerDemo")
        val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(streamUrl))

        player?.prepare(mediaSource)
    }

    override fun pause() {

        // Update metadata
        // todo: update metadata

        // Pause the player
        player?.playWhenReady = false
    }

    override fun stop() {
        // todo: stop the player

        // Stop the service and set the media session as inactive
        radioService.onPlaybackStop()
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        Logger.e(TAG, "onPlaybackParametersChanged")
    }

    override fun onSeekProcessed() {
        Logger.e(TAG, "onSeekProcessed")
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        Logger.e(TAG, "onTracksChanged")
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        Logger.e(TAG, error as Throwable, "onPlayerError")
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        Logger.e(TAG, "onLoadingChanged => isLoading: $isLoading")
    }

    override fun onPositionDiscontinuity(reason: Int) {
        Logger.e(TAG, "onPositionDiscontinuity")
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        Logger.e(TAG, "onRepeatModeChanged")
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        Logger.e(TAG, "onShuffleModeEnabledChanged")
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        Logger.e(TAG, "onTimelineChanged")
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Logger.e(TAG, "onPlayerStateChanged => playWhenReady: $playWhenReady, playbackState: $playbackState")

        when (playbackState) {
            Player.STATE_READY -> {
                if (playWhenReady) {
                    radioService.onPlaybackStateUpdated(PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PLAYING, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                            .setActions(PlaybackStateCompat.ACTION_PAUSE)
                            .build())
                } else {
                    radioService.onPlaybackStateUpdated(PlaybackStateCompat.Builder()
                            .setState(PlaybackStateCompat.STATE_PAUSED, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP)
                            .build())
                }
            }
        }
    }
}