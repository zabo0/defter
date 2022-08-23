package com.saboon.defter.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.saboon.defter.R
import com.saboon.defter.adapters.AddMomentFragmentRecyclerAdapter
import com.saboon.defter.databinding.FragmentAddNewMomentBinding
import com.saboon.defter.models.ModelMoments
import com.saboon.defter.utils.*
import com.saboon.defter.viewmodels.AddNewMomentFragmentViewModel


class AddNewMomentFragment : Fragment() {
    private var _binding: FragmentAddNewMomentBinding? =null
    private val binding get() = _binding!!

    private lateinit var moment: ModelMoments

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private lateinit var viewModel: AddNewMomentFragmentViewModel

    private var dayMoment: Long = 0
    private var selectedPicture: Uri? = null
    private var dailyRemainingMoments = "0"

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


        viewModel.getDailyRemainingMoments()

        viewModel.getDailyMoments()

        dayMoment = DateTimeConverter().getCurrentTime()
        binding.date.setText(DateTimeConverter().getTime(dayMoment, "dd MMMM yyyy"))


        binding.recyclerViewDailyMoments.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)


        binding.date.setOnClickListener {
            getDay {
                dayMoment = it!!
                val date = DateTimeConverter().getTime(dayMoment,"dd MMMM yyyy")
                binding.date.setText(date)
            }
        }


        binding.imageViewAddPhoto.setOnClickListener {
            binding.buttonAdd.text = resources.getString(R.string.add)
            binding.updateSuccessText.visibility = View.GONE
            if (dailyRemainingMoments == "0"){
                Toast.makeText(requireContext(),"can not add more then 3 moment",Toast.LENGTH_LONG).show()
            }else{
                goToGallery(it)
            }
        }

        binding.buttonAdd.setOnClickListener {view->

            val action = AddNewMomentFragmentDirections.actionAddNewMomentFragmentToImageViewerFragment()
            view.findNavController().navigate(action)

//            if (dailyRemainingMoments == "0"){
//                Toast.makeText(requireContext(),resources.getString(R.string.cantAddMoment),Toast.LENGTH_LONG).show()
//            }else{
//                when(binding.buttonAdd.text){
//                    resources.getString(R.string.add) -> {
//                        if(selectedPicture != null){
//
//                            viewModel.addPhotoToStorage(selectedPicture!!,dayMoment){downloadURL, resizedDownloadURL ->
//                                moment = createMoment(downloadURL,resizedDownloadURL)
//
//                                viewModel.addMoment(moment){
//                                    if(it){
//                                        binding.updateSuccessText.visibility = View.VISIBLE
//                                        binding.buttonAdd.isEnabled = true
//                                        binding.buttonAdd.text = resources.getString(R.string.addNew)
//                                        binding.imageViewAddPhoto.setImageResource(R.drawable.avatars)
//                                        binding.comment.setText("")
//                                        binding.comment.clearFocus()
//                                        Toast.makeText(requireContext(),resources.getString(R.string.momentAdded),Toast.LENGTH_LONG).show()
//
//                                        viewModel.updateUserRemaining(resizedDownloadURL,dailyRemainingMoments)
//                                        viewModel.updateUserMomentsNumber()
//
//                                    }
//                                }
//                            }
//
//                        }else{
//                            Toast.makeText(requireContext(),resources.getString(R.string.pleaseSelectPhoto),Toast.LENGTH_LONG).show()
//                        }
//                    }
//
//                    resources.getString(R.string.addNew) -> {
//                        goToGallery(view)
//                        binding.buttonAdd.text = resources.getString(R.string.add)
//                        binding.updateSuccessText.visibility = View.GONE
//                    }
//                }
//            }
        }
        observer()
    }



    private fun observer(){
        viewModel.dailyMoments.observe(viewLifecycleOwner, Observer {
            if(it != null){
                binding.recyclerViewDailyMoments.visibility = View.VISIBLE
                binding.recyclerViewDailyMoments.adapter = AddMomentFragmentRecyclerAdapter(it)
            }else{
                binding.recyclerViewDailyMoments.visibility = View.GONE
            }
        })
        viewModel.dailyRemainingMoment.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(it!= null){
                dailyRemainingMoments = it
//                binding.editTextRemaining.text = it
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

    private fun createMoment(downloadURL: String, resizedDownloadURL: String):ModelMoments{
        val sender = viewModel.getUser().email
        val senderUserName = viewModel.getUserName()
        val date = dayMoment
        val dateAdded = DateTimeConverter().getCurrentTime()
        val text = binding.comment.text.toString()
        val id = IDGenerator().generateMomentID(dayMoment,sender!!)

        return ModelMoments(id,sender,senderUserName,date,dateAdded,downloadURL,resizedDownloadURL,text)
    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult!=null){
                    selectedPicture = intentFromResult.data
                    selectedPicture.let {
                        binding.imageViewAddPhoto.setImageURI(it)
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