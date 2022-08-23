package com.saboon.defter.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.saboon.defter.R
import com.saboon.defter.activities.PasswordActivity
import com.saboon.defter.databinding.FragmentHomeBinding
import com.saboon.defter.models.ModelHomeSection
import com.saboon.defter.models.ModelMoments
import com.saboon.defter.utils.DateTimeConverter
import com.saboon.defter.viewmodels.HomeFragmentViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding?=null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeFragmentViewModel
    private lateinit var momentList: List<ModelMoments>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeFragmentViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_home, container, false)
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel.getUserProfilePhotoURL {
//            Glide
//                .with(this)
//                .load(it)
//                .into(binding.appBarAccountImage)
//        }


        binding.homeRecyclerView.layoutManager = LinearLayoutManager(context)

        observer()
    }

    private fun observer(){
        viewModel.moments.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                binding.homeRecyclerView.visibility = View.VISIBLE
                binding.progressBarLoading.visibility = View.GONE
                binding.linearLayoutError.visibility = View.GONE

                momentList = it
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer {
            if(it){
                binding.homeRecyclerView.visibility = View.GONE
                binding.progressBarLoading.visibility = View.VISIBLE
                binding.linearLayoutError.visibility = View.GONE
            }
        })

        viewModel.errorLayout.observe(viewLifecycleOwner, Observer {
            if(it){
                binding.homeRecyclerView.visibility = View.GONE
                binding.progressBarLoading.visibility = View.GONE
                binding.linearLayoutError.visibility = View.VISIBLE
            }
        })
        viewModel.error.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                Toast.makeText(requireContext(),it,Toast.LENGTH_LONG).show()
            }
        })
    }

//    private fun orderDataToRecyclerView(momentList: List<ModelMoments>){
//        val sectionList: List<ModelHomeSection> = arrayListOf()
//        var date = momentList[0].date
//
//        for (moment in momentList){
//            if (moment.date == date){
//                val section = ModelHomeSection(DateTimeConverter().getTime(date,"dd.MMMM.yyyy"),)
//            }
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}