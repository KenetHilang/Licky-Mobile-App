package com.example.licky.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.licky.R
import com.example.licky.databinding.FragmentProfileBinding

/**
 * Profile Fragment - User profile and settings
 */
class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }
    
    private fun setupUI() {
        binding.apply {
            // About section
            cardAbout.setOnClickListener {
                // Navigate to about screen
            }
            
            // Help section
            cardHelp.setOnClickListener {
                // Navigate to help screen
            }
            
            // Privacy section
            cardPrivacy.setOnClickListener {
                // Navigate to privacy screen
            }
            
            // Terms section
            cardTerms.setOnClickListener {
                // Navigate to terms screen
            }
        }
    }
    
    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            binding.apply {
                if (user != null) {
                    textViewUserName.text = user.name
                    textViewUserEmail.text = user.email
                    
                    user.profileImagePath?.let { imagePath ->
                        Glide.with(this@ProfileFragment)
                            .load(imagePath)
                            .placeholder(R.drawable.ic_profile)
                            .error(R.drawable.ic_profile)
                            .circleCrop()
                            .into(imageViewProfile)
                    }
                } else {
                    textViewUserName.text = "Guest User"
                    textViewUserEmail.text = "Sign in to save your data"
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
