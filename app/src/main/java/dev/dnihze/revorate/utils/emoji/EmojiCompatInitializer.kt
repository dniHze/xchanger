package dev.dnihze.revorate.utils.emoji

import android.content.Context
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import dev.dnihze.revorate.R
import timber.log.Timber
import javax.inject.Inject

class EmojiCompatInitializer @Inject constructor(
    private val appContext: Context
) {

    fun init() {
        // Use a downloadable font for EmojiCompat
        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            R.array.com_google_android_gms_fonts_certs
        )
        val config: EmojiCompat.Config = FontRequestEmojiCompatConfig(appContext, fontRequest)
            .setReplaceAll(true)
            .registerInitCallback(object : EmojiCompat.InitCallback() {
                override fun onInitialized() {
                    Timber.tag("Emoji").i("EmojiCompat initialized")
                }

                override fun onFailed(throwable: Throwable?) {
                    Timber.tag("Emoji").e(throwable, "EmojiCompat initialized")
                }
            })
        EmojiCompat.init(config)
    }
}
