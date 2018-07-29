package fr.smarquis.appstore

import android.text.format.DateUtils
import android.view.View
import android.view.View.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.smarquis.appstore.Version.Status.*
import kotlinx.android.synthetic.main.item_version.view.*

class VersionViewHolder(
        v: View,
        private val callback: VersionAdapter.Callback
) : RecyclerView.ViewHolder(v), View.OnClickListener, View.OnLongClickListener {

    private val name: TextView = v.textView_version_name
    private val timestamp: TextView = v.textView_version_timestamp
    private val description: TextView = v.textView_version_description
    private val size: TextView = v.textView_version_size
    private val progress: ProgressBar = v.progressBar_version
    private val type: ImageView = v.imageView_version_type

    private var version: Version? = null

    init {
        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
    }

    companion object {
        const val UNKNOWN_SIZE = "⋯" /*•••*/ /*∙∙∙*/ /*···*/
    }

    fun bind(version: Version) {
        this.version = version
        renderTitleAndDescription()
        renderProgress()
    }

    private fun renderTitleAndDescription() {
        name.text = version?.name
        description.apply {
            text = version?.descriptionToHtml
            visibility = if (version?.descriptionToHtml.isNullOrBlank()) GONE else VISIBLE
            BetterLinkMovementMethod.applyTo(this, itemView)
        }
    }

    fun renderProgress() {
        size.text = version?.apkSizeBytesDisplay ?: UNKNOWN_SIZE

        val resourceId = when {
            version?.status == DOWNLOADING -> R.drawable.ic_cloud_sync_16dp
            version?.hasApkUrl() ?: false -> R.drawable.ic_cloud_search_16dp
            version?.hasApkRef() ?: false && version?.apkFileAvailable?.not() ?: false -> R.drawable.ic_cloud_download_16dp
            version?.hasApkRef() ?: false && version?.apkFileAvailable ?: false -> R.drawable.ic_cloud_check_16dp
            else -> R.drawable.ic_cloud_alert_16dp
        }
        type.setImageResource(resourceId)

        when (version?.status) {
            DEFAULT -> {
                progress.progress = 0
                progress.isIndeterminate = false
                progress.visibility = if (version?.descriptionToHtml.isNullOrBlank()) GONE else INVISIBLE
                val now = System.currentTimeMillis()
                val time = version?.timestamp ?: now
                timestamp.text = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
            }
            DOWNLOADING -> {
                progress.progress = version?.progress ?: 0
                progress.isIndeterminate = version?.progress == 0
                progress.visibility = VISIBLE
                timestamp.setText(R.string.item_version_downloading)
            }
            INSTALLING -> {
                progress.progress = 0
                progress.isIndeterminate = true
                progress.visibility = VISIBLE
                timestamp.setText(R.string.item_version_installing)
            }
            OPENING -> {
                progress.progress = 0
                progress.isIndeterminate = true
                progress.visibility = VISIBLE
                timestamp.setText(R.string.item_version_opening)
            }
            else -> {
                progress.visibility = GONE
                timestamp.text = null
            }
        }
    }

    fun unbind() {
        BetterLinkMovementMethod.clear(description)
        version = null
    }

    override fun onClick(v: View?) {
        version?.let {
            callback.onItemClicked(it, this)
        }
    }

    override fun onLongClick(v: View?): Boolean {
        version?.let {
            return callback.onItemLongClicked(it, this)
        }
        return false
    }

}