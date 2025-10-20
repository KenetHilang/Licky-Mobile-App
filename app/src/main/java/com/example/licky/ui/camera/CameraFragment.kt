package com.example.licky.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.licky.R
import com.example.licky.databinding.FragmentCameraBinding
import com.example.licky.utils.ImageUtils
import com.example.licky.utils.PermissionUtils
import com.example.licky.utils.Resource
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Camera Fragment - Captures tongue images
 */
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CameraViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private var currentPhotoFile: File? = null

    private val IMG_SIZE = 224

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            startCamera()
        } else {
            Toast.makeText(
                requireContext(),
                "Camera permission is required",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigateUp()
        }
    }

    // Gallery picker
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            handlePickedImage(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (PermissionUtils.hasCameraPermission(requireContext())) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        }

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        binding.buttonCapture.setOnClickListener {
            takePhoto()
        }

        binding.buttonClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonUpload.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Top bar upload icon
        binding.buttonUploadTop.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun setupObservers() {
        viewModel.analysisResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.buttonCapture.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonCapture.isEnabled = true

                    // Navigate to result screen using Bundle instead of Safe Args
                    resource.data?.let { scanResult ->
                        // Build top-5 string from latest class probabilities
                        val pairs = viewModel.classProbabilities.value ?: emptyList()
                        val top5Text = pairs.take(5).mapIndexed { idx, (label, p) ->
                            val pct = (p * 100f)
                            "${idx + 1}. ${label} ${"%.1f".format(pct)}%"
                        }.joinToString("\n")
                        val bundle = bundleOf(
                            "scanResultId" to scanResult.id,
                            "top5Text" to top5Text
                        )
                        findNavController().navigate(
                            R.id.action_navigation_camera_to_resultDetailFragment,
                            bundle
                        )
                        viewModel.resetAnalysisResult()
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonCapture.isEnabled = true
                    Toast.makeText(
                        requireContext(),
                        resource.message ?: "Analysis failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                null -> {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonCapture.isEnabled = true
                }
            }
        }

        // Show all class probabilities after inference
        viewModel.classProbabilities.observe(viewLifecycleOwner) { pairs ->
            if (pairs.isNullOrEmpty()) return@observe
            val msg = pairs.joinToString(separator = "\n") { (label, p) ->
                val pct = (p * 100f)
                "${label}: ${"%.1f".format(pct)}%"
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to start camera: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        currentPhotoFile = ImageUtils.createImageFile(requireContext())

        val outputOptions = ImageCapture.OutputFileOptions.Builder(currentPhotoFile!!).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    currentPhotoFile?.let { file ->
                        val bitmap = ImageUtils.loadBitmap(file.absolutePath)
                        if (bitmap != null) {
                            val rotated = ImageUtils.rotateBitmapIfNeeded(file.absolutePath, bitmap)
                            // Delegate analysis to ViewModel (saves result and navigates via observer)
                            viewModel.analyzeTongueImage(file.absolutePath, rotated)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed to load image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        requireContext(),
                        "Photo capture failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun handlePickedImage(uri: Uri) {
        try {
            // Load bitmap from URI
            val stream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
            stream?.close()

            if (bitmap == null) {
                Toast.makeText(requireContext(), "Unable to load image", Toast.LENGTH_SHORT).show()
                return
            }

            // Save a copy to app storage for consistency with camera flow
            val file = ImageUtils.createImageFile(requireContext())
            val rotated = bitmap // Gallery images usually oriented; optional: detect EXIF via Uri
            if (!ImageUtils.saveBitmap(rotated, file)) {
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
                return
            }

            // Analyze via ViewModel
            viewModel.analyzeTongueImage(file.absolutePath, rotated)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Failed to process image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Inference is handled in ViewModel


    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
}
