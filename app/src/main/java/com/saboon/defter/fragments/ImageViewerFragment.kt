package com.saboon.defter.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.davemorrissey.labs.subscaleview.ImageSource
import com.google.android.material.snackbar.Snackbar
import com.saboon.defter.R
import com.saboon.defter.databinding.FragmentImageViewerBinding
import com.saboon.defter.utils.SelectedImageSingleton


class ImageViewerFragment : Fragment() {

    private var _binding : FragmentImageViewerBinding?=null
    private val binding get() = _binding!!

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerLauncher()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_image_viewer, container, false)

        _binding = FragmentImageViewerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (SelectedImageSingleton.selectedImageBitmap == null){
            goToGallery(view)
        }else{
            binding.imageView.setImage(ImageSource.bitmap(SelectedImageSingleton.selectedImageBitmap!!))
        }

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.cancel -> {
                    SelectedImageSingleton.selectedImageBitmap = null
                    val action = ImageViewerFragmentDirections.actionImageViewerFragmentToAddNewMomentFragment()
                    view.findNavController().navigate(action)
                    true
                }
                R.id.turnLeft -> {
                    turnLeft()
                    binding.imageView.setImage(ImageSource.bitmap(SelectedImageSingleton.selectedImageBitmap!!))
                    true
                }
                R.id.turnRight -> {
                    turnRight()
                    binding.imageView.setImage(ImageSource.bitmap(SelectedImageSingleton.selectedImageBitmap!!))
                    true
                }
                else -> false
            }
        }

        binding.topAppBar.setNavigationOnClickListener {
            goToGallery(it)
        }

        binding.imageView.setOnClickListener {

            if (binding.appBarLayout.visibility == View.VISIBLE){
                binding.appBarLayout.visibility = View.GONE
                binding.UIComponent.visibility = View.GONE
                binding.imageView.setBackgroundColor(Color.Black.hashCode())
            }else{
                binding.appBarLayout.visibility = View.VISIBLE
                binding.UIComponent.visibility = View.VISIBLE
                binding.imageView.setBackgroundColor(Color.White.hashCode())
            }
        }

        binding.fab.setOnClickListener {
            val action = ImageViewerFragmentDirections.actionImageViewerFragmentToAddNewMomentFragment()
            it.findNavController().navigate(action)
        }

    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == Activity.RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult!=null){

                    intentFromResult.data.let {
                        SelectedImageSingleton.selectedImageBitmap = uriToBitmap(intentFromResult.data!!)
                        binding.imageView.setImage(ImageSource.bitmap(SelectedImageSingleton.selectedImageBitmap!!))
                    }
                }
            }
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                Toast.makeText(requireContext(),resources.getString(R.string.permission), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun goToGallery(view: View){
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                Snackbar.make(view,resources.getString(R.string.permission),
                    Snackbar.LENGTH_INDEFINITE).setAction(resources.getString(R.string.givePermission)){
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }.show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        val result : Bitmap
        try {
            val source = ImageDecoder.createSource(requireActivity().contentResolver, uri)
            result = ImageDecoder.decodeBitmap(source)
            return result
        }catch (e: Exception){
            Toast.makeText(requireContext(),e.localizedMessage,Toast.LENGTH_LONG).show()
        }

        return null
    }

    private fun turnLeft(){
        val matrix = Matrix()
        matrix.postRotate(-90F)
        SelectedImageSingleton.selectedImageBitmap = Bitmap.createBitmap(SelectedImageSingleton.selectedImageBitmap!!, 0, 0,
            SelectedImageSingleton.selectedImageBitmap!!.width,
            SelectedImageSingleton.selectedImageBitmap!!.height,
            matrix, true)
    }

    private fun turnRight(){
        val matrix = Matrix()
        matrix.postRotate(90F)
        SelectedImageSingleton.selectedImageBitmap = Bitmap.createBitmap(SelectedImageSingleton.selectedImageBitmap!!, 0, 0,
            SelectedImageSingleton.selectedImageBitmap!!.width,
            SelectedImageSingleton.selectedImageBitmap!!.height,
            matrix, true)
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}