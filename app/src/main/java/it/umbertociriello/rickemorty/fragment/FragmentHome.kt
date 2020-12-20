package it.umbertociriello.rickemorty.fragment

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.okhttp.Request
import it.umbertociriello.rickemorty.R
import it.umbertociriello.rickemorty.api.RickEMortyAPI
import it.umbertociriello.rickemorty.bindinglistener.HomeBindingListener
import it.umbertociriello.rickemorty.databinding.FragmentHomeBinding
import it.umbertociriello.rickemorty.models.CharacterResponse
import it.umbertociriello.rickemorty.viewmodels.HomeViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import org.jetbrains.anko.support.v4.runOnUiThread
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random


class FragmentHome: Fragment(), HomeBindingListener, RickEMortyAPI.CharacterInterface,
    CoroutineScope {

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob


    private val REQUEST_IMAGE_CAPTURE = 1
    private val homeViewModel: HomeViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        Dexter.withContext(context)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    homeViewModel.isCameraGranted.postValue(true)
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    homeViewModel.isCameraGranted.postValue(false)
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) { /* ... */
                }
            }).check()
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView_top.setImageBitmap(imageBitmap)
            launch {
                requestCharacter()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHomeBinding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_home, container, false
        )
        val view: View = binding.root
        binding.homeViewModel = homeViewModel
        binding.lifecycleOwner = this
        binding.listener = this
        return view

    }

    override fun onButtonClick() {
        if(homeViewModel.isCameraGranted.value == true) {
            dispatchTakePictureIntent()
        }else {
            checkCameraPermission()
        }
    }

    override fun onResponseSuccessful(character: CharacterResponse) {
        runOnUiThread {
            homeViewModel.characterName.postValue(character.name)
            homeViewModel.isResponseSuccessful.postValue(true)
            Glide.with(this).load(character.image).into(imageView_bottom)
        }
    }

    override fun onResponseError(request: Request?, exception: Exception?) {
        homeViewModel.isResponseSuccessful.postValue(false)
        exception?.let {
            Log.e("FragmentHome", exception.toString())
        } ?: run {
            Log.e("FragmentHome", "Something went wrong...")
        }
    }

    private suspend fun requestCharacter() {
        withContext(Dispatchers.IO) {
            RickEMortyAPI.getCharacter(Random.nextInt(1, 671).toString(), this@FragmentHome)
        }
    }
}