package it.umbertociriello.rickemorty.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    val isCameraGranted = MutableLiveData<Boolean>()
    val characterName = MutableLiveData<String>()
    val isResponseSuccessful = MutableLiveData(false)
}