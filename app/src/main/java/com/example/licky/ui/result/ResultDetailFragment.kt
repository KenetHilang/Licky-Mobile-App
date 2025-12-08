package com.example.licky.ui.result

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.licky.R
import com.example.licky.databinding.FragmentResultDetailBinding
import com.example.licky.utils.DateUtils

class ResultDetailFragment : Fragment() {

    private var _binding: FragmentResultDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResultDetailViewModel by viewModels()
    private var scanResultId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanResultId = arguments?.getString("scanResultId")

        if (scanResultId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Invalid scan result", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        viewModel.loadScanResult(scanResultId!!)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.scanResult.observe(viewLifecycleOwner) { scanResult ->
            scanResult?.let { result ->
                binding.apply {
                    Glide.with(this@ResultDetailFragment)
                        .load(result.imagePath)
                        .placeholder(R.drawable.ic_tongue)
                        .error(R.drawable.ic_tongue)
                        .into(imageViewScannedTongue)

                    textViewScanDate.text = DateUtils.formatDateTime(result.timestamp)

                    val topLabel = result.detectedDiseases.firstOrNull()?.disease?.name
                        ?: result.overallHealth.name.replace("_", " ")
                    textViewHealthStatus.text = topLabel

                    textViewHealthStatusDescription.text = result.detectedDiseases.firstOrNull()?.disease?.description
                        ?: getHealthStatusDescription(result.overallHealth)

                    cardDiseaseInfo.visibility = View.GONE

                    val confidencePercent = (result.confidenceScore * 100).toInt()
                    textViewConfidenceScore.text = "$confidencePercent%"
                    progressBarConfidence.progress = 0
                    ObjectAnimator.ofInt(progressBarConfidence, "progress", 0, confidencePercent).apply {
                        duration = 1000
                        interpolator = DecelerateInterpolator()
                        start()
                    }

                    result.notes?.let { editTextNotes.setText(it) }

                    buttonBack.setOnClickListener { findNavController().navigateUp() }
                    buttonSaveNotes.setOnClickListener {
                        viewModel.updateNotes(result.id, editTextNotes.text.toString())
                    }
                }
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Notes saved successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getHealthStatusDescription(status: com.example.licky.data.model.HealthStatus): String {
        return when (status) {
            com.example.licky.data.model.HealthStatus.HEALTHY ->
                "Your tongue appears healthy with no significant concerns detected."
            com.example.licky.data.model.HealthStatus.MILD_CONCERNS ->
                "Some minor concerns detected. Consider consulting with a healthcare professional."
            com.example.licky.data.model.HealthStatus.MODERATE_CONCERNS ->
                "Moderate concerns detected. We recommend consulting with a doctor."
            com.example.licky.data.model.HealthStatus.SEVERE_CONCERNS ->
                "Significant concerns detected. Please consult with a healthcare professional immediately."
            com.example.licky.data.model.HealthStatus.UNKNOWN ->
                "Unable to determine health status. Please try scanning again."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}