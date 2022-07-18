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
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.saboon.defter.R
import com.saboon.defter.databinding.FragmentAddNewMomentBinding
import com.saboon.defter.models.ModelMoments
import com.saboon.defter.utils.*


class AddNewMomentFragment : Fragment() {
    private var _binding: FragmentAddNewMomentBinding? =null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore

    private lateinit var moment: ModelMoments

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private var dayMoment: Long = 0
    private var selectedPicture: Uri? = null
    private var dailyRemainingMoments: Int = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        storage = Firebase.storage
        firestore = Firebase.firestore

        registerLauncher()
        setImagesToDaily()

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

        dayMoment = DateTimeConverter().getCurrentTime()
        binding.date.setText(DateTimeConverter().getTime(dayMoment, "dd MMMM yyyy"))


        binding.date.setOnClickListener {
            getDay {
                dayMoment = it!!
                val date = DateTimeConverter().getTime(dayMoment,"dd MMMM yyyy")
                binding.date.setText(date)
            }
        }


        binding.imageViewAddPhoto.setOnClickListener {
           goToGallery(it)
        }

        binding.buttonAdd.setOnClickListener {view->

            when(binding.buttonAdd.text){
                resources.getString(R.string.add) -> {
                    val photoRef = storage.reference.child(PATH_TO_MOMENTS_PHOTOS).child(IDGenerator().generateMomentPhotoID(dayMoment)+".jpg")
                    selectedPicture.let {imgUri->
                        photoRef.putFile(imgUri!!).addOnProgressListener {taskSnapshot->

                            binding.loadingLayout.visibility = View.VISIBLE
                            binding.progressBarFirst.visibility = View.VISIBLE
                            binding.buttonAdd.isEnabled = false

                            val progress: Double = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                            binding.progressBar.progress = progress.toInt()
                        }.addOnSuccessListener {

                            photoRef.downloadUrl.addOnSuccessListener {
                                val downloadURL = it.toString()
                                val moment = createMoment(downloadURL)
                                firestore.collection(COLLECTION_MOMENTS).document(IDGenerator().generateMomentID(moment.date,moment.sender))
                                    .set(moment)
                                    .addOnSuccessListener {

                                        when(dailyRemainingMoments){
                                            3 -> {
                                                firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).update("dailyMomentIDFirst", downloadURL)
                                            }
                                            2 -> {
                                                firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).update("dailyMomentIDSecond", downloadURL)
                                            }
                                            1 -> {
                                                firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).update("dailyMomentIDThird", downloadURL)
                                            }
                                        }
                                        // TODO: gunluk en fazla 3 moment ekleme olayini yap

                                        binding.loadingLayout.visibility = View.GONE
                                        binding.progressBarFirst.visibility = View.GONE
                                        binding.updateSuccessText.visibility = View.VISIBLE
                                        binding.buttonAdd.isEnabled = true
                                        binding.buttonAdd.text = resources.getString(R.string.addNew)
                                        binding.imageViewAddPhoto.setImageResource(R.drawable.avatars)
                                        binding.comment.setText("")
                                        binding.comment.clearFocus()

                                        Glide.with(this)
                                            .load(moment.photoURL)
                                            .into(binding.imageViewPhotoFirst)

                                        Toast.makeText(requireContext(),resources.getString(R.string.momentAdded),Toast.LENGTH_LONG).show()
                                    }.addOnFailureListener {e->
                                        Toast.makeText(requireContext(),e.localizedMessage,Toast.LENGTH_LONG).show()
                                    }
                            }
                        }.addOnFailureListener {e->
                            Toast.makeText(requireContext(),e.localizedMessage,Toast.LENGTH_LONG).show()
                        }
                    }
                }

                resources.getString(R.string.addNew) -> {
                    goToGallery(view)
                    binding.buttonAdd.text = resources.getString(R.string.add)
                    binding.updateSuccessText.visibility = View.GONE
                }
            }



        }


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

    private fun createMoment(downloadURL: String):ModelMoments{
        val sender = auth.currentUser!!.email!!
        val date = dayMoment
        val text = binding.comment.text.toString()
        val id = IDGenerator().generateMomentID(dayMoment,sender)

        return ModelMoments(id,sender,date,downloadURL,text)
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
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
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
                // You can use the API that requires the permission.
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
                Snackbar.make(view,resources.getString(R.string.permission),
                    Snackbar.LENGTH_INDEFINITE).setAction(resources.getString(R.string.givePermission)){
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }.show()
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun setImagesToDaily(){
        firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if(it != null){
                    if(it.data?.get("dailyMomentIDFirst") != "null"){
                        Glide.with(this).load(it.data?.get("dailyMomentIDFirst")).into(binding.imageViewPhotoFirst)
                        dailyRemainingMoments--
                    }
                    if(it.data?.get("dailyMomentIDSecond") != "null"){
                        Glide.with(this).load(it.data?.get("dailyMomentIDSecond")).into(binding.imageViewPhotoSecond)
                        dailyRemainingMoments--
                    }
                    if(it.data?.get("dailyMomentIDThird") != "null"){
                        Glide.with(this).load(it.data?.get("dailyMomentIDThird")).into(binding.imageViewPhotoThird)
                        dailyRemainingMoments--
                    }
                }
            }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}