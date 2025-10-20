package com.example.licky.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.licky.R
import com.example.licky.databinding.FragmentHomeBinding

/**
 * Home Fragment - Dashboard with recent scans and quick actions
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var scanHistoryAdapter: ScanHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        // Setup recent scans RecyclerView
        scanHistoryAdapter = ScanHistoryAdapter(
            onItemClick = { scanResult ->
                // Navigate to result detail
                val bundle = bundleOf("scanResultId" to scanResult.id)
                findNavController().navigate(
                    R.id.action_home_to_result,
                    bundle
                )
            },
            onDeleteClick = { scanResult ->
                viewModel.deleteScanResult(scanResult)
            }
        )

        binding.recyclerViewRecentScans.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = scanHistoryAdapter
        }

        // Scan button click
        binding.buttonScan.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_camera)
        }

        // View all history click
        binding.textViewViewAll.setOnClickListener {
            findNavController().navigate(R.id.navigation_history)
        }
    }

    private fun setupObservers() {
        viewModel.recentScans.observe(viewLifecycleOwner) { scans ->
            if (scans.isEmpty()) {
                binding.recyclerViewRecentScans.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.textViewViewAll.visibility = View.GONE
            } else {
                binding.recyclerViewRecentScans.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
                binding.textViewViewAll.visibility = View.VISIBLE
                scanHistoryAdapter.submitList(scans)
            }
        }

        viewModel.totalScans.observe(viewLifecycleOwner) { total ->
            binding.textViewTotalScans.text = total.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}