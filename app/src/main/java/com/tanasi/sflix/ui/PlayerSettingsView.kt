package com.tanasi.sflix.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Tracks
import com.tanasi.sflix.R
import com.tanasi.sflix.databinding.ItemSettingBinding
import com.tanasi.sflix.databinding.ViewPlayerSettingsBinding

class PlayerSettingsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val binding = ViewPlayerSettingsBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    var player: ExoPlayer? = null


    init {
        Setting.Main.adapter.playerSettingsView = this
    }

    fun onBackPressed() {
        when (binding.rvSettings.adapter) {
            is SettingsAdapter -> hide()
            else -> displaySetting(Setting.Main)
        }
    }


    fun show() {
        this.visibility = View.VISIBLE

        displaySetting(Setting.Main)
    }

    private fun displaySetting(setting: Setting) {
        binding.tvSettingsHeader.text = when (setting) {
            Setting.Main -> "Settings"
            Setting.Quality -> "Quality for current video"
            Setting.Subtitle -> "Subtitles/closed captions"
            Setting.Speed -> "Video speed"
        }

        binding.rvSettings.adapter = when (setting) {
            Setting.Main -> Setting.Main.adapter
            Setting.Quality -> TODO()
            Setting.Subtitle -> TODO()
            Setting.Speed -> TODO()
        }
        binding.rvSettings.requestFocus()
    }

    fun hide() {
        this.visibility = View.GONE
    }


    private sealed class Setting {

        object Main : Setting() {
            val adapter = SettingsAdapter(listOf(Quality, Subtitle, Speed))
        }

        object Quality : Setting()

        object Subtitle : Setting()

        object Speed : Setting()
    }


    private class SettingsAdapter(
        private val settings: List<Setting>,
    ) : RecyclerView.Adapter<SettingViewHolder>() {

        lateinit var playerSettingsView: PlayerSettingsView

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
            return SettingViewHolder(
                ItemSettingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
            val setting = settings[position]

            holder.binding.root.setOnClickListener {
                playerSettingsView.displaySetting(setting)
            }

            holder.binding.ivSettingIcon.apply {
                when (setting) {
                    Setting.Quality -> setImageDrawable(
                        ContextCompat.getDrawable(context, R.drawable.ic_settings_quality)
                    )
                    Setting.Subtitle -> setImageDrawable(
                        ContextCompat.getDrawable(context, R.drawable.ic_settings_subtitle_off)
                    )
                    Setting.Speed -> setImageDrawable(
                        ContextCompat.getDrawable(context, R.drawable.ic_settings_playback_speed)
                    )
                    else -> {}
                }
                visibility = View.VISIBLE
            }

            holder.binding.tvSettingMainText.text = when (setting) {
                Setting.Quality -> "Quality"
                Setting.Subtitle -> "Captions"
                Setting.Speed -> "Speed"
                else -> ""
            }

            holder.binding.tvSettingSubText.apply {
                text = ""
                visibility = when (text) {
                    "" -> View.GONE
                    else -> View.VISIBLE
                }
            }

            holder.binding.ivSettingCheck.visibility = View.GONE
        }

        override fun getItemCount() = settings.size
    }


    private class VideoTrackInformation(
        val name: String,
        val width: Int,
        val height: Int,
        val bitrate: Int,

        val player: ExoPlayer,
        val trackGroup: Tracks.Group,
        val trackIndex: Int,
    ) {
        val isSelected: Boolean
            get() = player.videoFormat?.bitrate == bitrate && trackGroup.isTrackSelected(trackIndex)
    }

    private class VideoTrackSelectionAdapter(
        private val tracks: List<VideoTrackInformation>,
    ) : RecyclerView.Adapter<SettingViewHolder>() {

        lateinit var playerSettingsView: PlayerSettingsView
        var selectedIndex = 0

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingViewHolder {
            return SettingViewHolder(
                ItemSettingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: SettingViewHolder, position: Int) {
            if (position == 0) {
                holder.binding.root.setOnClickListener {
                    selectedIndex = 0
                    playerSettingsView.player?.let { player ->
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .setMaxVideoBitrate(Int.MAX_VALUE)
                            .setForceHighestSupportedBitrate(false)
                            .build()
                    }
                    playerSettingsView.hide()
                }

                holder.binding.ivSettingIcon.visibility = View.GONE

                holder.binding.tvSettingMainText.text = when (selectedIndex) {
                    0 -> "Auto • ${tracks.find { it.isSelected }?.height ?: 0}p"
                    else -> "Auto"
                }

                holder.binding.tvSettingSubText.visibility = View.GONE

                holder.binding.ivSettingCheck.visibility = when (selectedIndex) {
                    0 -> View.VISIBLE
                    else -> View.GONE
                }
            } else {
                val track = tracks[position - 1]

                holder.binding.root.setOnClickListener {
                    selectedIndex = holder.bindingAdapterPosition
                    playerSettingsView.player?.let { player ->
                        player.trackSelectionParameters = player.trackSelectionParameters
                            .buildUpon()
                            .setMaxVideoBitrate(track.bitrate)
                            .setForceHighestSupportedBitrate(true)
                            .build()
                    }
                    playerSettingsView.hide()
                }

                holder.binding.ivSettingIcon.visibility = View.GONE

                holder.binding.tvSettingMainText.text = "${track.height}p"

                holder.binding.tvSettingSubText.visibility = View.GONE

                holder.binding.ivSettingCheck.visibility = when (position) {
                    selectedIndex -> View.VISIBLE
                    else -> View.GONE
                }
            }
        }

        override fun getItemCount() = tracks.size + 1
    }


    private class SettingViewHolder(
        val binding: ItemSettingBinding,
    ) : RecyclerView.ViewHolder(binding.root)
}