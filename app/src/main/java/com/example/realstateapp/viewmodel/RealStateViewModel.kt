package com.example.realstateapp.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.realstateapp.data.remote.ApiInterface
import com.example.realstateapp.data.remote.Home
import com.example.realstateapp.data.remote.OccupiedHome
import com.example.realstateapp.data.remote.User
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.Resources
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RealStateViewModel(
    application: Application,
    val apiInterface: ApiInterface
) :
    AndroidViewModel(application) {
    val TAG = "RealStateViewModel"
    val isValid = MutableLiveData<Resources<Boolean>>()
    val isUserAdded = MutableLiveData<Resources<Boolean>>()
    val isUserAvailable = MutableLiveData<Resources<Boolean>>()
    val searchHouseData = MutableLiveData<Resources<List<Home?>>>()
    private val likeHomeList: ArrayList<OccupiedHome> = ArrayList()
    private var _propertyType = ""
    private var _residentalType = ""
    private var _bhk = ""
    private var _State = ""
    private var _City = ""
    private var _MinPrice = 0
    private var _MaxPrice = 0
    val isImageUploaded = apiInterface.isImageUploaded
    val isImageProgress = apiInterface.isImageProgress
    val liveUser: MutableLiveData<User> = apiInterface.mUser
    private var mUser: User = User()
    val latestList: MutableLiveData<Resources<ArrayList<Home>>> = MutableLiveData()
    val topList: MutableLiveData<Resources<ArrayList<Home>>> = MutableLiveData()
    val stateList: MutableLiveData<Resources<ArrayList<Home>>> = MutableLiveData()
    val cityList: MutableLiveData<Resources<ArrayList<Home>>> = MutableLiveData()


    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        GlobalScope.launch {
            isValid.postValue(Resources.Loading())
            val value = apiInterface.signInWithPhoneAuthCredential(credential)
            if (value) {
                isValid.postValue(Resources.Success(true))
            } else {
                isValid.postValue(Resources.Error("In valid Otp"))
            }
        }
    }

    fun getTopHouse(){
        GlobalScope.launch {
            try {
                topList.postValue(Resources.Loading())
                val list = apiInterface.getTopHouse()
                topList.postValue(Resources.Success(list))
            }catch (e: Exception){
                topList.postValue(Resources.Error("{${e.message}}"))
                Log.d(TAG,"top houses $e")
            }
        }
    }


    fun getCitiesHouse(city : String){
        GlobalScope.launch {
            try {
                cityList.postValue(Resources.Loading())
                val list = apiInterface.getCityHouse(city)
                cityList.postValue(Resources.Success(list))
            }catch (e: Exception){
                cityList.postValue(Resources.Error("{${e.message}}"))
                Log.d(TAG,"top houses $e")
            }
        }
    }

    fun getStateHouse(state : String){
        GlobalScope.launch {
            try {
                stateList.postValue(Resources.Loading())
                val list = apiInterface.getStatesHouse(state)
                stateList.postValue(Resources.Success(list))
            }catch (e: Exception){
                stateList.postValue(Resources.Error("{${e.message}}"))
                Log.d(TAG,"top houses $e")
            }
        }
    }

    fun getLatestHouse(){
        GlobalScope.launch {
            try {
                latestList.postValue(Resources.Loading())
                val list = apiInterface.getLatestHouse()
                latestList.postValue(Resources.Success(list))
            }catch (e: Exception){
                latestList.postValue(Resources.Error("{${e.message}}"))
                Log.d(TAG,"top houses $e")
            }
        }
    }

    fun getUserAvailable(){
        GlobalScope.launch {
            try {
                apiInterface.getUserAvailable()
            }catch (e: Exception){
                Log.d(TAG,"error on getting user $e")
            }
        }
    }

    fun getUser() {
        GlobalScope.launch {
            try {
                Log.d(TAG, "User are set")
                val user = apiInterface.getUserData()
                Log.d(TAG, "Setting user $user")
                mUser = user
                Log.d(TAG, "Setting user $mUser")
            } catch (e: Exception) {
                Log.d(TAG, "Error of setting home $e")
            }
        }
    }

    fun setUser() {
        GlobalScope.launch {
            suspend {
                try {
                    Log.d(TAG, "User are set")
                    val user = apiInterface.getUserData()
                    Log.d(TAG, "Setting user $user")
                    mUser = user
                    Log.d(TAG, "Setting user $mUser")
                } catch (e: Exception) {
                    Log.d(TAG, "Error of setting home $e")
                }
            }.invoke()
        }
    }

    fun getUserData(): User{
        return apiInterface.getUser()
    }

    fun uploadHomeImage(activity: RealStateActivity, uri: Uri) {
        GlobalScope.launch {
            try {
                apiInterface.addHouseImage(activity, uri)
            } catch (e: Exception) {
                Log.d(TAG, "error on image $e")
            }
        }
    }

    fun addUser(user: User) {
        viewModelScope.launch {
            isUserAdded.postValue(Resources.Loading())
            val value = apiInterface.addUser(user)
            if (value) {
                isUserAdded.postValue(Resources.Success(true))
            } else {
                isUserAdded.postValue(Resources.Error("SomeThing Wrong"))
            }
        }
    }

    fun addFavorite(home: Home) {
        GlobalScope.launch {
            apiInterface.addFavorite(home)
        }
    }

    fun removeFavorite(home: Home) {
        GlobalScope.launch {
            apiInterface.removeFavorite(home)
        }
    }

    fun setAllLikeHome() {
        GlobalScope.launch {
            suspend {
                Log.d(TAG, "Set Liked Function are called")
                val alreadyLiked = apiInterface.getAllFavoriteFromUid()
                likeHomeList.addAll(alreadyLiked)
            }.invoke()
        }
    }

    fun getAllLikedHome(): ArrayList<OccupiedHome> {
        return likeHomeList
    }

    fun addLikedHome(home: Home) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val favoriteUid =
            ""
        if (uid != null) {
            val occupiedHome = OccupiedHome(
                favoriteUid,
                uid,
                home.ownerUid,
                home.uid,
                home.title,
                home.state,
                home.city,
                home.address,
                home.zipCode,
                home.propertyType,
                home.residentialType,
                home.price,
                home.priceType,
                home.bhk,
                home.bedroom,
                home.bathroom,
                home.phoneNumber,
                home.description,
                home.timeStamp,
                home.frontHouseImage,
                home.houseImages,
            )
            likeHomeList.add(occupiedHome)
        }
    }

    fun removeLikedHome(home: Home) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val favoriteUid =
            ""
        if (uid != null) {
            val occupiedHome = OccupiedHome(
                favoriteUid,
                uid,
                home.ownerUid,
                home.uid,
                home.title,
                home.state,
                home.city,
                home.address,
                home.zipCode,
                home.propertyType,
                home.residentialType,
                home.price,
                home.priceType,
                home.bhk,
                home.bedroom,
                home.bathroom,
                home.phoneNumber,
                home.description,
                home.timeStamp,
                home.frontHouseImage,
                home.houseImages,
            )
            likeHomeList.remove(occupiedHome)
        }
    }

    fun checkHomeLikedOrNot(home: Home): Boolean {
        likeHomeList.forEach { likedHomes ->
            if (home.uid == likedHomes.homeUid) {
                return true
            }
        }
        return false
    }

    fun addHistory(home: Home) {
        GlobalScope.launch {
            apiInterface.addHistory(home)
        }
    }

    fun removeHistory(home: Home) {
        GlobalScope.launch {
            apiInterface.removeHistory(home)
        }
    }

    fun addHome(home: Home) {
        GlobalScope.launch {
            apiInterface.addHome(home = home)
        }
    }

    fun deleteHome(home: Home) {
        GlobalScope.launch {
            apiInterface.deleteHome(home = home)
        }
    }

    fun isUserAvailable(uid: String) {
        viewModelScope.launch {
            isUserAvailable.postValue(Resources.Loading())
            val value = apiInterface.isUserExist(uid)
            if (value) {
                isUserAvailable.postValue(Resources.Success(true))
            } else {
                isUserAvailable.postValue(Resources.Success(false))
            }
        }
    }

    fun addHouseImages(uid: String, url: String) {
        GlobalScope.launch {
            apiInterface.addHouseImages(uid, url)
        }
    }

    fun deleteHouseImages(uid: String, url: String) {
        GlobalScope.launch {
            apiInterface.deleteHouseImages(uid, url)
        }
    }

    fun getSearchData(query: String) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchHouse(query)
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    private fun convertDocumentSnapshotToList(searchHouse: List<DocumentSnapshot>): ArrayList<Home?> {
        val list: ArrayList<Home?> = ArrayList()
        searchHouse.forEach { data ->
            val home = data.toObject(Home::class.java)
            list.add(home)
        }
        return list
    }


    fun searchPropertyType(query: String, propertyName: String) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyType(query, propertyName)
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchBhk(query: String, bhk: String) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchBhk(query, bhk)
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchState(query: String, state: String) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchState(query, state)
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsResidentialType(
        query: String,
        propertyName: String,
        residentialName: String
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsResidentialType(
                    query,
                    propertyName,
                    residentialName
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsResidentialTypeAsBhk(
        query: String,
        propertyName: String,
        residentialName: String,
        bhkName: String
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsResidentialTypeAsBhk(
                    query,
                    propertyName,
                    residentialName,
                    bhkName
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsResidentialTypeAsBhkAsState(
        query: String,
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsResidentialTypeAsBhkAsState(
                    query,
                    propertyName,
                    residentialName,
                    bhkName,
                    state
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCity(
        query: String,
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String,
        city: String
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCity(
                    query,
                    propertyName,
                    residentialName,
                    bhkName,
                    state,
                    city
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCityAsMinMax(
        query: String,
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data =
                    apiInterface.searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCityAsMinMax(
                        query,
                        propertyName,
                        residentialName,
                        bhkName,
                        state,
                        city,
                        min,
                        max
                    )
                if (data.isNotEmpty()) {
                    searchHouseData.postValue(Resources.Success(data))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsBhk(
        query: String,
        propertyName: String,
        bhkName: String
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsBhk(
                    query,
                    propertyName,
                    bhkName,
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsBhkAsState(
        query: String,
        propertyName: String,
        bhkName: String,
        state: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsBhkAsState(
                    query,
                    propertyName,
                    bhkName,
                    state
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsBhkAsStateAsCity(
        query: String,
        propertyName: String,
        bhkName: String,
        state: String,
        city: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsBhkAsStateAsCity(
                    query,
                    propertyName,
                    bhkName,
                    state,
                    city
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsBhkAsStateAsCityAsMinMax(
        query: String,
        propertyName: String,
        bhkName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsBhkAsStateAsCityAsMinMax(
                    query,
                    propertyName,
                    bhkName,
                    state,
                    city,
                    min,
                    max
                )
                if (data.isNotEmpty()) {
                    searchHouseData.postValue(Resources.Success(data))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsState(
        query: String,
        propertyName: String,
        state: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsState(
                    query,
                    propertyName,
                    state,
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsStateAsCity(
        query: String,
        propertyName: String,
        state: String,
        city: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsStateAsCity(
                    query,
                    propertyName,
                    state,
                    city
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsStateAsCityAsMinMax(
        query: String,
        propertyName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsStateAsCityAsMinMax(
                    query,
                    propertyName,
                    state,
                    city,
                    min,
                    max
                )
                if (data.isNotEmpty()) {
                    searchHouseData.postValue(Resources.Success(data))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsMinMax(
        query: String,
        propertyName: String,
        min: Int,
        max: Int
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsMinMax(
                    query,
                    propertyName,
                    min,
                    max
                )
                if (data.isNotEmpty()) {
                    searchHouseData.postValue(Resources.Success(data))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchBhkAsState(
        query: String,
        bhkName: String,
        state: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchBhkAsState(
                    query,
                    bhkName,
                    state
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchBhkAsStateAsCity(
        query: String,
        bhkName: String,
        state: String,
        city: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchBhkAsStateAsCity(
                    query,
                    bhkName,
                    state,
                    city
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchStateAsCity(
        query: String,
        state: String,
        city: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchStateAsCity(
                    query,
                    state,
                    city
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyType(propertyName: String) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyType(
                    propertyName
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchBhk(bhk: String) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchBhk(
                    bhk
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchState(state: String) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchState(
                    state
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsResidentialType(
        propertyName: String,
        residentialName: String
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsResidentialType(
                    propertyName,
                    residentialName
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsResidentialTypeAsBhk(
        propertyName: String,
        residentialName: String,
        bhkName: String
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsResidentialTypeAsBhk(
                    propertyName,
                    residentialName,
                    bhkName
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsResidentialTypeAsBhkAsState(
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsResidentialTypeAsBhkAsState(
                    propertyName,
                    residentialName,
                    bhkName,
                    state
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCity(
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String,
        city: String
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCity(
                    propertyName,
                    residentialName,
                    bhkName,
                    state,
                    city
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCityAsMinMax(
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data =
                    apiInterface.searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCityAsMinMax(
                        propertyName,
                        residentialName,
                        bhkName,
                        state,
                        city,
                        min,
                        max
                    )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsBhk(
        propertyName: String,
        bhkName: String
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsBhk(
                    propertyName,
                    bhkName,
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsBhkAsState(
        propertyName: String,
        bhkName: String,
        state: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsBhkAsState(
                    propertyName,
                    bhkName,
                    state
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsBhkAsStateAsCity(
        propertyName: String,
        bhkName: String,
        state: String,
        city: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsBhkAsStateAsCity(
                    propertyName,
                    bhkName,
                    state,
                    city
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsBhkAsStateAsCityAsMinMax(
        propertyName: String,
        bhkName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsBhkAsStateAsCityAsMinMax(
                    propertyName,
                    bhkName,
                    state,
                    city,
                    min,
                    max
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsState(
        propertyName: String,
        state: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsState(
                    propertyName,
                    state,
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsStateAsCity(
        propertyName: String,
        state: String,
        city: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsStateAsCity(
                    propertyName,
                    state,
                    city
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsStateAsCityAsMinMax(
        propertyName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsStateAsCityAsMinMax(
                    propertyName,
                    state,
                    city,
                    min,
                    max
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchPropertyTypeAsMinMax(
        propertyName: String,
        min: Int,
        max: Int
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchPropertyTypeAsMinMax(
                    propertyName,
                    min,
                    max
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchBhkAsState(
        bhkName: String,
        state: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchBhkAsState(
                    bhkName,
                    state
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchBhkAsStateAsCity(
        bhkName: String,
        state: String,
        city: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchBhkAsStateAsCity(
                    bhkName,
                    state,
                    city
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun searchStateAsCity(
        state: String,
        city: String,
    ) {
        GlobalScope.launch {
            try {
                searchHouseData.postValue(Resources.Loading())
                val data = apiInterface.searchStateAsCity(
                    state,
                    city
                )
                if (data.isNotEmpty()) {
                    val list: List<Home?> =
                        convertDocumentSnapshotToList(data)
                    searchHouseData.postValue(Resources.Success(list))
                } else {
                    searchHouseData.postValue(Resources.Success(emptyList()))
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error occurs : $e")
                searchHouseData.postValue(Resources.Error("${e.stackTrace}", emptyList()))
            }
        }
    }

    fun setPropertyType(value: String) {
        _propertyType = value
    }


    fun getPropertyType(): String {
        return _propertyType
    }

    fun setResidentalType(value: String) {
        _residentalType = value
    }

    fun getResidentalType(): String {
        return _residentalType
    }

    fun setBhk(value: String) {
        _bhk = value
    }

    fun getBhk(): String {
        return _bhk
    }

    fun setState(value: String) {
        _State = value
    }

    fun getState(): String {
        return _State
    }

    fun setCity(value: String) {
        _City = value
    }

    fun getCity(): String {
        return _City
    }

    fun setMin(value: Int) {
        _MinPrice = value
    }

    fun getMin(): Int {
        return _MinPrice
    }

    fun setMax(value: Int) {
        _MaxPrice = value
    }

    fun getMax(): Int {
        return _MaxPrice
    }

}
