package com.saboon.defter.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.saboon.defter.R
import com.saboon.defter.adapters.AddMomentFragmentRecyclerAdapter
import com.saboon.defter.databinding.FragmentAddNewMomentBinding
import com.saboon.defter.models.ModelMoments
import com.saboon.defter.utils.*
import com.saboon.defter.viewmodels.AddNewMomentFragmentViewModel
import java.io.ByteArrayOutputStream
import java.util.*


class AddNewMomentFragment : Fragment() {
    private var _binding: FragmentAddNewMomentBinding? =null
    private val binding get() = _binding!!

    private lateinit var moment: ModelMoments

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var viewModel: AddNewMomentFragmentViewModel

    private var dateOfMoment: Long = 0
    //private var selectedPictureURI: Uri? = null
    private var selectedPhotoBitmap : Bitmap? = null
    private var dailyMomentPhotoURLsList: ArrayList<String> = arrayListOf()

    private val recyclerAdapter = AddMomentFragmentRecyclerAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(AddNewMomentFragmentViewModel::class.java)

        registerLauncher()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_add_new_moment, container, false)
        _binding = FragmentAddNewMomentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getDailyMoments()

        dateOfMoment = DateTimeConverter().getCurrentTime()
        binding.date.setText(DateTimeConverter().getTime(dateOfMoment, "dd MMMM yyyy"))


        binding.recyclerViewDailyMoments.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        binding.recyclerViewDailyMoments.adapter = recyclerAdapter

        binding.date.setOnClickListener {
            getDay {
                dateOfMoment = it!!
                val date = DateTimeConverter().getTime(dateOfMoment,"dd MMMM yyyy")
                binding.date.setText(date)
            }
        }


        binding.imageViewSelectPhoto.setOnClickListener { view ->
            viewModel.getDailySentMomentsNumber {
                val ten: Long = 10
                if (it == ten){
                    Toast.makeText(requireContext(),resources.getString(R.string.cantAddMoment),Toast.LENGTH_LONG).show()
                }else{
                    goToGallery(view)
                }
            }

        }

        binding.buttonAdd.setOnClickListener {view->
            viewModel.getDailySentMomentsNumber { dailySentMomentsNumber ->
                val ten: Long = 10
                if (dailySentMomentsNumber == ten){
                    Toast.makeText(requireContext(),resources.getString(R.string.cantAddMoment),Toast.LENGTH_LONG).show()
                }else{
                    if(selectedPhotoBitmap != null){
                        val resizedPhotoBitmap = makeSmallerBitmap(selectedPhotoBitmap!!,500)

                        val outputStream = ByteArrayOutputStream()
                        val resizedOutputStream = ByteArrayOutputStream()

                        selectedPhotoBitmap!!.compress(Bitmap.CompressFormat.JPEG,100,outputStream)
                        val selectedPhotoByteArray = outputStream.toByteArray()

                        resizedPhotoBitmap.compress(Bitmap.CompressFormat.JPEG,100,resizedOutputStream)
                        val resizedPhotoByteArray = resizedOutputStream.toByteArray()


                        viewModel.addPhotosToStorage(selectedPhotoByteArray,resizedPhotoByteArray, dateOfMoment){ photoURL, resizedPhotoURL ->
                            moment = createMoment(photoURL,resizedPhotoURL)

                            viewModel.addMoment(moment){
                                if(it){
                                    binding.updateSuccessText.visibility = View.VISIBLE
                                    binding.loadingLayout.visibility = View.GONE
                                    binding.buttonAdd.isEnabled = false
                                    binding.imageViewSelectPhoto.setImageResource(R.drawable.avatars)
                                    binding.comment.setText("")
                                    binding.comment.clearFocus()
                                    Toast.makeText(requireContext(),resources.getString(R.string.momentAdded),Toast.LENGTH_LONG).show()

                                    viewModel.getTotalMomentsNumber {totalMomentsNumber->
                                        viewModel.updateDailySentMoments(moment.id,resizedPhotoURL,dailySentMomentsNumber,totalMomentsNumber)
                                    }
                                    recyclerAdapter.insertNewItem(resizedPhotoURL)
                                    binding.recyclerViewDailyMoments.smoothScrollToPosition(0)
                                }
                            }
                        }
                    }
                }
            }
        }
        observer()
    }



    private fun observer(){
        viewModel.dailyMoments.observe(viewLifecycleOwner, Observer {
            if(it != null){
                dailyMomentPhotoURLsList = it
                binding.recyclerViewDailyMoments.visibility = View.VISIBLE
                recyclerAdapter.updateAllList(dailyMomentPhotoURLsList)
            }else{
                binding.recyclerViewDailyMoments.visibility = View.GONE
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if (it){
                binding.loadingLayout.visibility = View.VISIBLE
                binding.buttonAdd.isEnabled = false
            }else{
                binding.loadingLayout.visibility = View.GONE
                binding.buttonAdd.isEnabled = true
            }
        })
        viewModel.progress.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it!=null){
                binding.progressBar.progress = it
            }
        })
    }


    private fun getDay(callback: (Long?) -> Unit){
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setTitleText("Select Date")
            .build()

        datePicker.show(childFragmentManager, "tag")

        datePicker.addOnPositiveButtonClickListener {
            val date = datePicker.selection
            callback(date)
        }
    }

    private fun createMoment(photoURL: String, resizedPhotoURL: String):ModelMoments{
        val sender = viewModel.getUser().email
        val senderUserName = viewModel.getUserName()
        val date = dateOfMoment
        val dateAdded = DateTimeConverter().getCurrentTime()
        val text = binding.comment.text.toString()
        val id = IDGenerator().generateMomentID(dateAdded,sender!!)

        return ModelMoments(id,sender,senderUserName,date,dateAdded,photoURL,resizedPhotoURL,text)
    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult!=null){
                    val selectedPhotoURI = intentFromResult.data
                    selectedPhotoURI.let {
                        binding.updateSuccessText.visibility = View.GONE
                        uriToBitmap(selectedPhotoURI!!)
                        binding.imageViewSelectPhoto.setImageBitmap(selectedPhotoBitmap)
                        binding.buttonAdd.isEnabled = true
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
                    Toast.makeText(requireContext(),resources.getString(R.string.permission),Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun uriToBitmap(uri: Uri){
        try {
            val source = ImageDecoder.createSource(requireActivity().contentResolver, uri)
            selectedPhotoBitmap = ImageDecoder.decodeBitmap(source)
        }catch (e: Exception){
            Toast.makeText(requireContext(),e.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }

    private fun makeSmallerBitmap(image: Bitmap, maximumSize : Int) : Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}