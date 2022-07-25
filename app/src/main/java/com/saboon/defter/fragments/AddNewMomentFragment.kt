package com.saboon.defter.fragments

import android.Manifest
import android.R.attr.bitmap
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.UriUtils
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
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule


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
    private var selectedPictureBitmap: Bitmap? = null
    private var dailyRemainingMoments = "0"

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


        firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                if (it != null){
                   dailyRemainingMoments = it.data?.get("dailyRemaining").toString()
                    binding.editTextRemaining.text = dailyRemainingMoments
                    if (dailyRemainingMoments == "0"){
                        binding.buttonAdd.isEnabled = false
                    }
                }
            }


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

            if (dailyRemainingMoments == "0"){
                Toast.makeText(requireContext(),resources.getString(R.string.cantAddMoment),Toast.LENGTH_LONG).show()
            }else{
                when(binding.buttonAdd.text){
                    resources.getString(R.string.add) -> {
                        if(selectedPicture != null){
                            val photoID = IDGenerator().generateMomentPhotoID(dayMoment)
                            val photoRef = storage.reference.child(PATH_TO_MOMENTS_PHOTOS).child(photoID + ".jpg")
                            val resizedPhotoRef = storage.reference.child(
                                PATH_TO_MOMENTS__RESIZED_PHOTOS).child(photoID+"_200x200.jpg")
                            photoRef.putFile(selectedPicture!!).addOnProgressListener {taskSnapshot->
                                binding.loadingLayout.visibility = View.VISIBLE
                                binding.buttonAdd.isEnabled = false
                                val progress: Double = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                                binding.progressBar.progress = progress.toInt()
                            }.addOnSuccessListener {

                                photoRef.downloadUrl.addOnSuccessListener {downloadURL->
                                    Timer().schedule(5000) {
                                        resizedPhotoRef.downloadUrl.addOnSuccessListener {resizedDownloadURL->
                                            moment = createMoment(downloadURL.toString(),resizedDownloadURL.toString())
                                            firestore.collection(COLLECTION_MOMENTS).document(IDGenerator().generateMomentID(moment.date,moment.sender))
                                                .set(moment)
                                                .addOnSuccessListener {

                                                    binding.updateSuccessText.visibility = View.VISIBLE
                                                    binding.buttonAdd.isEnabled = true
                                                    binding.buttonAdd.text = resources.getString(R.string.addNew)
                                                    binding.imageViewAddPhoto.setImageResource(R.drawable.avatars)
                                                    binding.comment.setText("")
                                                    binding.comment.clearFocus()
                                                    Toast.makeText(requireContext(),resources.getString(R.string.momentAdded),Toast.LENGTH_LONG).show()

                                                    updateUserRemaining(resizedDownloadURL.toString())

                                                    binding.loadingLayout.visibility = View.GONE
                                                }.addOnFailureListener {e->
                                                    binding.loadingLayout.visibility = View.GONE
                                                    Toast.makeText(requireContext(),e.localizedMessage,Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    }
                                }
                            }.addOnFailureListener {e->
                                Toast.makeText(requireContext(),e.localizedMessage,Toast.LENGTH_LONG).show()
                            }
                        }else{
                            Toast.makeText(requireContext(),resources.getString(R.string.pleaseSelectPhoto),Toast.LENGTH_LONG).show()
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
    }

    private fun updateUserRemaining(resizedDownloadURL: String) {
        when(dailyRemainingMoments){
            "3" -> {
                firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).update(
                    mapOf(
                        "dailyMomentIDFirst" to resizedDownloadURL,
                        "dailyRemaining" to "2"
                    ))
                Glide.with(this)
                    .load(moment.resizedPhotoURL)
                    .into(binding.imageViewPhotoFirst)
                dailyRemainingMoments = "2"
                binding.editTextRemaining.text = dailyRemainingMoments

            }
            "2" -> {
                firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).update(
                    mapOf(
                        "dailyMomentIDSecond" to resizedDownloadURL,
                        "dailyRemaining" to "1"
                    ))
                Glide.with(this)
                    .load(moment.resizedPhotoURL)
                    .into(binding.imageViewPhotoSecond)

                dailyRemainingMoments = "1"
                binding.editTextRemaining.text = dailyRemainingMoments
            }
            "1" -> {
                firestore.collection(COLLECTION_USERS).document(auth.currentUser!!.uid).update(
                    mapOf(
                        "dailyMomentIDThird" to resizedDownloadURL,
                        "dailyRemaining" to "0"
                    ))
                Glide.with(this)
                    .load(moment.resizedPhotoURL)
                    .into(binding.imageViewPhotoThird)

                dailyRemainingMoments = "0"
                binding.editTextRemaining.text = dailyRemainingMoments
                binding.buttonAdd.isEnabled = false
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

    private fun createMoment(downloadURL: String, resizedDownloadURL: String):ModelMoments{
        val sender = auth.currentUser!!.email!!
        val date = dayMoment
        val dateAdded = DateTimeConverter().getCurrentTime()
        val text = binding.comment.text.toString()
        val id = IDGenerator().generateMomentID(dayMoment,sender)

        return ModelMoments(id,sender,date,dateAdded,downloadURL,resizedDownloadURL,text)
    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult!=null){
                    selectedPicture = intentFromResult.data
                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(requireActivity().contentResolver, selectedPicture!!)
                            selectedPictureBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageViewAddPhoto.setImageBitmap(selectedPictureBitmap)
                        } else {
                            selectedPictureBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedPicture)
                            binding.imageViewAddPhoto.setImageBitmap(selectedPictureBitmap)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
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
                        Glide.with(this)
                            .load(it.data?.get("dailyMomentIDFirst"))
                            .into(binding.imageViewPhotoFirst)
                    }
                    if(it.data?.get("dailyMomentIDSecond") != "null"){
                        Glide.with(this).load(it.data?.get("dailyMomentIDSecond")).into(binding.imageViewPhotoSecond)
                    }
                    if(it.data?.get("dailyMomentIDThird") != "null"){
                        Glide.with(this).load(it.data?.get("dailyMomentIDThird")).into(binding.imageViewPhotoThird)
                    }
                }
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
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}