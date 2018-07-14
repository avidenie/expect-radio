package ro.expectations.radio.service.extensions

import android.support.v4.media.MediaMetadataCompat


inline val MediaMetadataCompat.id get() = getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)