package com.example.licky.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.licky.R
import com.example.licky.data.model.ScanResult
import com.example.licky.databinding.ItemScanHistoryBinding
import com.example.licky.utils.DateUtils

/**
 * Adapter for displaying scan history
 */
class ScanHistoryAdapter(
    private val onItemClick: (ScanResult) -> Unit,
    private val onDeleteClick: (ScanResult) -> Unit
) : ListAdapter<ScanResult, ScanHistoryAdapter.ScanHistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanHistoryViewHolder {
        val binding = ItemScanHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScanHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScanHistoryViewHolder(
        private val binding: ItemScanHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(scanResult: ScanResult) {
            binding.apply {
                // Load thumbnail
                Glide.with(imageViewThumbnail.context)
                    .load(scanResult.imagePath)
                    .placeholder(R.drawable.ic_tongue)
                    .error(R.drawable.ic_tongue)
                    .centerCrop()
                    .into(imageViewThumbnail)

                // Set date
                textViewDate.text = DateUtils.formatDateTime(scanResult.timestamp)

                // Set health status
                textViewHealthStatus.text = scanResult.overallHealth.name.replace("_", " ")

                // Set confidence
                textViewConfidence.text = "${(scanResult.confidenceScore * 100).toInt()}%"

                // Set click listeners
                root.setOnClickListener { onItemClick(scanResult) }
                buttonDelete.setOnClickListener { onDeleteClick(scanResult) }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ScanResult>() {
        override fun areItemsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScanResult, newItem: ScanResult): Boolean {
            return oldItem == newItem
        }
    }
}
