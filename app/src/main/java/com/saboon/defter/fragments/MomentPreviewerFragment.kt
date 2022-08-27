package com.saboon.defter.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.saboon.defter.R
import com.saboon.defter.databinding.FragmentMomentPreviewerBinding
import com.saboon.defter.models.ModelMoments
import com.saboon.defter.utils.DateTimeConverter
import com.saboon.defter.viewmodels.MomentPreviewerFragmentViewModel


class MomentPreviewerFragment : Fragment() {

    private var _binding: FragmentMomentPreviewerBinding?=null
    private val binding get() = _binding!!

    private lateinit var viewModel: MomentPreviewerFragmentViewModel

    private lateinit var moment: ModelMoments

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MomentPreviewerFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_moment_previewer, container, false)
        _binding = FragmentMomentPreviewerBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        arguments.let {
            val momentID = MomentPreviewerFragmentArgs.fromBundle(it!!).momentID

            viewModel.getMoment(momentID)
        }

        binding.topAppBar.setNavigationOnClickListener {
            val action = MomentPreviewerFragmentDirections.actionMomentPreviewerFragmentToAddNewMomentFragment()
            it.findNavController().navigate(action)
        }

        observer()
    }

    private fun observer(){
        viewModel.moment.observe(viewLifecycleOwner, Observer {
            if(it!= null){
                moment = it
                setUI(moment)
            }
        })
    }

    private fun setUI(moment: ModelMoments){

        binding.topAppBar.title = DateTimeConverter().getTime(moment.date,"dd MMMM yyyy")

        Glide.with(requireContext())
            .load(moment.resizedPhotoURL)
            .into(binding.imageView)

        viewModel.getUserName(moment.senderUID){
            binding.textViewUserName.text = it
        }

        binding.textViewComment.text = moment.text

        if (moment.senderUID == viewModel.getUser().uid){
            binding.buttonDelete.visibility = View.VISIBLE
        }else{
            binding.buttonDelete.visibility = View.GONE
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}