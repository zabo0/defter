package com.saboon.defter.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.saboon.defter.R
import com.saboon.defter.activities.MainActivity
import com.saboon.defter.databinding.FragmentLogInBinding

class LogInFragment : Fragment() {


    private var _binding: FragmentLogInBinding?=null
    private val binding get() = _binding!!

    private lateinit var userEmail: String
    private lateinit var userPassword: String

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        var currentUser = auth.currentUser

        // Check if user is signed in (non-null) and update UI accordingly.

        if(currentUser != null){
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_log_in, container, false)
        _binding = FragmentLogInBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogin.setOnClickListener {

            userEmail = binding.editTextUserEmail.text.toString()
            userPassword = binding.editTextUserPassword.text.toString()

            auth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        val intent = Intent(requireActivity(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(requireContext(), resources.getString(R.string.authFailed), Toast.LENGTH_SHORT).show()
                    }
                }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}