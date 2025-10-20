package com.example.licky.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licky.R
import com.example.licky.databinding.FragmentHistoryBinding
import com.example.licky.ui.home.ScanHistoryAdapter

/**
 * History Fragment - Displays all scan history
 */
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var scanHistoryAdapter: ScanHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        scanHistoryAdapter = ScanHistoryAdapter(
            onItemClick = { scanResult ->
                // Fixed: Use Bundle instead of Safe Args
                val bundle = bundleOf("scanResultId" to scanResult.id)
                findNavController().navigate(
                    R.id.action_history_to_result,
                    bundle
                )
            },
            onDeleteClick = { scanResult ->
                showDeleteConfirmation(scanResult)
            }
        )

        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = scanHistoryAdapter
        }

        binding.buttonClearAll.setOnClickListener {
            showClearAllConfirmation()
        }
    }

    private fun setupObservers() {
        viewModel.allScans.observe(viewLifecycleOwner) { scans ->
            if (scans.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.recyclerViewHistory.visibility = View.GONE
                binding.buttonClearAll.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.recyclerViewHistory.visibility = View.VISIBLE
                binding.buttonClearAll.visibility = View.VISIBLE
                scanHistoryAdapter.submitList(scans)
            }
        }
    }

    private fun showDeleteConfirmation(scanResult: com.example.licky.data.model.ScanResult) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Scan")
            .setMessage("Are you sure you want to delete this scan?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteScanResult(scanResult)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearAllConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All History")
            .setMessage("Are you sure you want to delete all scan history? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                viewModel.deleteAllScans()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}