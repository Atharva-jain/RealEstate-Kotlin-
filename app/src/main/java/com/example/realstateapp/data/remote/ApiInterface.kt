package com.example.realstateapp.data.remote

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.realstateapp.ui.activities.RealStateActivity
import com.example.realstateapp.utils.Constant.BHK
import com.example.realstateapp.utils.Constant.CITY
import com.example.realstateapp.utils.Constant.FAVORITE
import com.example.realstateapp.utils.Constant.HISTORY
import com.example.realstateapp.utils.Constant.HOME
import com.example.realstateapp.utils.Constant.HOME_UID
import com.example.realstateapp.utils.Constant.HOUSE_ARRAY_NAME
import com.example.realstateapp.utils.Constant.OWN_UID
import com.example.realstateapp.utils.Constant.PRICE
import com.example.realstateapp.utils.Constant.PROPERTY_TYPE
import com.example.realstateapp.utils.Constant.RESIDENTAIL_TYPE
import com.example.realstateapp.utils.Constant.STATE
import com.example.realstateapp.utils.Constant.TIMESNAP
import com.example.realstateapp.utils.Constant.TITLE
import com.example.realstateapp.utils.Constant.USER
import com.example.realstateapp.utils.GetImageExtension
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class ApiInterface {

    private val mAuth = FirebaseAuth.getInstance()
    private val mInstance = FirebaseFirestore.getInstance()
    private val mUserCollection = mInstance.collection(USER)
    private val mHomeCollection = mInstance.collection(HOME)
    private val mFavoriteCollection = mInstance.collection(FAVORITE)
    private val mHistoryCollection = mInstance.collection(HISTORY)
    private val fireStorageReference = FirebaseStorage.getInstance().reference
    val isImageUploaded: MutableLiveData<String> = MutableLiveData<String>()
    val isImageProgress: MutableLiveData<Int> = MutableLiveData()
    val mUser: MutableLiveData<User> = MutableLiveData()
    private val TAG = "PhoneAuthentication"

    suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential): Boolean {
        return try {
            mAuth.signInWithCredential(credential).await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Error on Phone Authentication $e")
            false
        }
    }


    suspend fun getLatestHouse(): ArrayList<Home> {
        return try {
            val originalHomes = ArrayList<Home>()
            val snapshot = mHomeCollection
                .orderBy(TIMESNAP, Query.Direction.DESCENDING)
                .limit(20)
                .get().await().documents.toList()
            val homes = convertDocumentSnapshotToList(snapshot)
            homes.forEach { home ->
                if (home != null) {
                    originalHomes.add(home)
                }
            }
            Log.d(TAG, "latest house $originalHomes")
            originalHomes
        } catch (e: Exception) {
            Log.d(TAG, "error on latest house $e")
            ArrayList()
        }
    }

    suspend fun getTopHouse(): ArrayList<Home> {
        return try {
            val originalHomes = ArrayList<Home>()
            val snapshot = mHomeCollection
                .orderBy(PRICE)
                .limit(20)
                .get().await().documents.toList()

            val homes = convertDocumentSnapshotToList(snapshot)
            homes.forEach { home ->
                if (home != null) {
                    originalHomes.add(home)
                }
            }
            originalHomes
        } catch (e: Exception) {
            Log.d(TAG, "error on top house $e")
            ArrayList()
        }
    }

    suspend fun getStatesHouse(state: String): ArrayList<Home> {
        return try {
            val originalHomes = ArrayList<Home>()
            val snapshot = mHomeCollection
                .orderBy(PRICE)
                .whereEqualTo(STATE, state)
                .limit(20)
                .get().await().documents.toList()
            val homes = convertDocumentSnapshotToList(snapshot)
            homes.forEach { home ->
                if (home != null) {
                    originalHomes.add(home)
                }
            }
            originalHomes
        } catch (e: Exception) {
            Log.d(TAG, "error on state house $e")
            ArrayList()
        }
    }

    suspend fun getCityHouse(city: String): ArrayList<Home> {
        return try {
            val originalHomes = ArrayList<Home>()
            val snapshot = mHomeCollection
                .orderBy(PRICE)
                .whereEqualTo(CITY, city)
                .limit(20)
                .get().await().documents.toList()
            val homes = convertDocumentSnapshotToList(snapshot)
            homes.forEach { home ->
                if (home != null) {
                    originalHomes.add(home)
                }
            }
            Log.d(TAG, "city house $originalHomes")
            originalHomes
        } catch (e: Exception) {
            Log.d(TAG, "error on city house $e")
            ArrayList()
        }
    }

    suspend fun getUserData(): User {
        return try {
            suspend {
                var originalUser = User()
                val uid = getUid()
                val documentSnapshot =
                    mUserCollection.whereEqualTo("uid", uid).get().await().documents.toList()
                val list = convertDocumentSnapshotToUser(documentSnapshot)
                list.forEach { user ->
                    if (user != null) {
                        Log.d(TAG, "Getting user $user")
                        originalUser = user
                        mUser.postValue(originalUser)
                        Log.d(TAG, "Setting user $originalUser")
                    }
                }
                originalUser
            }.invoke()
        } catch (e: Exception) {
            Log.d(TAG, "error on getting data of user $e")
            User()
        }
    }

    fun getUser(): User {
        return try {
            var originalUser = User()
            val uid = getUid()
            mUserCollection.whereEqualTo("uid", uid).get().addOnSuccessListener {
                val list = convertDocumentSnapshotToUser(it.documents.toList())
                list.forEach { user ->
                    if (user != null) {
                        Log.d(TAG, "Getting user $user")
                        originalUser = user
                        Log.d(TAG, "Setting user $originalUser")
                    }
                }
                originalUser
            }.addOnFailureListener {
                originalUser
            }
            originalUser
        } catch (e: Exception) {
            Log.d(TAG, "error on getting data of user $e")
            User()
        }
    }

    suspend fun getUserAvailable() {
        try {
            val uid = getUid()
            val snapshot = mHomeCollection.whereEqualTo("uid", uid).get().await().documents.toList()
            val users = convertDocumentSnapshotToUser(snapshot)
            users.forEach { user ->
                if (user != null) {
                    mUser.postValue(user)
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "error on getting user $e")
        }

    }

    suspend fun addHouseImage(activity: RealStateActivity, uri: Uri) {
        try {
            val imageExtension =
                GetImageExtension.getFileExtension(activity, uri)
            if (imageExtension != null) {
                val fileRef: StorageReference =
                    fireStorageReference.child("${System.currentTimeMillis()}.$imageExtension")
                fileRef.putFile(uri)
                    .addOnSuccessListener { task ->
                        fileRef.downloadUrl.addOnSuccessListener { uri ->
                            val url = uri.toString()
                            isImageUploaded.postValue(url)
                        }.addOnFailureListener {
                            Log.d(TAG, "error on image  $it")
                        }
                    }.addOnProgressListener { task ->
                        val progress =
                            (100 * task.bytesTransferred) / task.totalByteCount
                        isImageProgress.postValue(progress.toInt())
                    }.addOnFailureListener {
                        Log.d(TAG, "error on image  $it")
                    }.await()
            }
        } catch (e: Exception) {
            Log.d(TAG, "error on image  $e")
        }
    }

    private fun getUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    suspend fun addFavorite(home: Home) {
        try {
            suspend {
                val uid = getUid()
                val favoriteUid = FirebaseFirestore.getInstance().collection(FAVORITE).document().id
                val date = Calendar.getInstance().time
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
                        date,
                        home.frontHouseImage,
                        home.houseImages,
                    )
                    mFavoriteCollection.document(favoriteUid).set(occupiedHome).await()
                }
            }.invoke()
        } catch (e: Exception) {
            Log.d(TAG, "error on adding favorite $e")
        }
    }

    suspend fun removeFavorite(home: Home) {
        try {
            suspend {
                val uid = getUid()
                Log.d(TAG, "remove fav $uid")
                if (uid != null) {
                    val documentSnapshots = mFavoriteCollection
                        .whereEqualTo(OWN_UID, uid)
                        .whereEqualTo(HOME_UID, home.uid)
                        .get().await().toList()
                    Log.d(TAG, "remove fav doc $documentSnapshots")
                    documentSnapshots.forEach { documentSnapshot ->
                        val occupiedHome = documentSnapshot.toObject(OccupiedHome::class.java)
                        Log.d(TAG, "remove fav ocHom $occupiedHome")
                        mFavoriteCollection.document(occupiedHome.occupiedUid).delete().await()
                    }
                }
            }.invoke()
        } catch (e: Exception) {
            Log.d(TAG, "error on adding favorite $e")
        }
    }

    suspend fun getAllFavoriteFromUid(): ArrayList<OccupiedHome> {
        return try {
            val list = ArrayList<OccupiedHome>()
            val uid = getUid()
            if (uid != null) {
                val listHome: List<DocumentSnapshot> =
                    mFavoriteCollection.whereEqualTo(OWN_UID, uid).get()
                        .await().documents.toList()
                val asList = convertDocumentSnapshotToOccupiedList(listHome)
                asList.forEach { home ->
                    if (home != null) {
                        Log.d(TAG, "Liked homes $home")
                        list.add(home)
                    }
                }
            }
            list
        } catch (e: Exception) {
            Log.d(TAG, "$e")
            ArrayList()
        }
    }

    suspend fun addHistory(home: Home) {
        try {
            suspend {
                val uid = getUid()
                val historyUid = FirebaseFirestore.getInstance().collection(HISTORY).document().id
                val date = Calendar.getInstance().time
                if (uid != null) {
                    val occupiedHome = OccupiedHome(
                        historyUid,
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
                        date,
                        home.frontHouseImage,
                        home.houseImages,
                    )
                    mHistoryCollection.document(historyUid).set(occupiedHome).await()
                }
            }.invoke()
        } catch (e: Exception) {
            Log.d(TAG, "error on adding favorite $e")
        }
    }

    suspend fun removeHistory(home: Home) {
        try {
            suspend {
                val uid = getUid()
                if (uid != null) {
                    val documentSnapshots = mHistoryCollection
                        .whereEqualTo(HOME_UID, home.uid)
                        .whereEqualTo(OWN_UID, uid)
                        .get().await().toList()
                    documentSnapshots.forEach { documentSnapshot ->
                        val occupiedHome = documentSnapshot.toObject(OccupiedHome::class.java)
                        mHistoryCollection.document(occupiedHome.occupiedUid).delete().await()
                    }
                }
            }.invoke()
        } catch (e: Exception) {
            Log.d(TAG, "error on adding favorite $e")
        }
    }

    suspend fun addUser(user: User): Boolean {
        return try {
            mUserCollection.document(user.uid).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addHome(home: Home) {
        try {
            mHomeCollection.document(home.uid).set(home).await()
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "Home data are not added to firebase $e")
        }
    }

    suspend fun deleteHome(home: Home) {
        try {
            mHomeCollection.document(home.uid).delete().await()
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "Home data are not deleted to firebase $e")
        }
    }

    suspend fun isUserExist(uid: String): Boolean {
        return try {
            val data = mUserCollection.document(uid).get().await()
            data.exists()
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "data are not getting $e")
            false
        }
    }

    suspend fun addHouseImages(uid: String, url: String) {
        try {
            mHomeCollection.document(uid).update(HOUSE_ARRAY_NAME, FieldValue.arrayUnion(url))
                .await()
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "Image are not uploaded $e")
        }
    }

    suspend fun deleteHouseImages(uid: String, url: String) {
        try {
            mHomeCollection.document(uid).update(HOUSE_ARRAY_NAME, FieldValue.arrayRemove(url))
                .await()
        } catch (e: java.lang.Exception) {
            Log.d(TAG, "Image are not delete $e")
        }
    }

    suspend fun searchHouse(query: String): List<DocumentSnapshot> {
        return try {
            mHomeCollection.orderBy(TITLE).startAt(query).endAt(query + "\uf8ff").get()
                .await().documents.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchPropertyType(query: String, propertyName: String): List<DocumentSnapshot> {
        return try {
            mHomeCollection.orderBy(TITLE)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(PROPERTY_TYPE, propertyName).get()
                .await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchBhk(query: String, bhk: String): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $bhk")
            mHomeCollection.orderBy(TITLE)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(BHK, bhk).get()
                .await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $bhk")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchState(query: String, state: String): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $state")
            mHomeCollection.orderBy(TITLE)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(STATE, state).get()
                .await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $state")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsResidentialType(
        query: String,
        propertyName: String,
        residentialName: String
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $propertyName $residentialName")
            mHomeCollection.orderBy(TITLE)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(RESIDENTAIL_TYPE, residentialName)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $residentialName")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsResidentialTypeAsBhk(
        query: String,
        propertyName: String,
        residentialName: String,
        bhkName: String
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $propertyName $residentialName $bhkName")
            mHomeCollection.orderBy(TITLE)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(RESIDENTAIL_TYPE, residentialName)
                .whereEqualTo(BHK, bhkName)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $residentialName $bhkName")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsResidentialTypeAsBhkAsState(
        query: String,
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $propertyName $residentialName $bhkName $state")
            mHomeCollection.orderBy(TITLE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(RESIDENTAIL_TYPE, residentialName)
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $residentialName $bhkName $state")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCity(
        query: String,
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String,
        city: String
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $propertyName $residentialName $bhkName $state $city")
            mHomeCollection.orderBy(TITLE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(RESIDENTAIL_TYPE, residentialName)
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $residentialName $bhkName $state $city")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCityAsMinMax(
        query: String,
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ): List<Home> {
        return try {
            Log.d(TAG, "$query $propertyName $residentialName $bhkName $state $city $min $max")
            val firstQuery =
                mHomeCollection.orderBy(TITLE).startAt(query).endAt(query + "\uf8ff").get()
                    .await().documents.toList()
            val firstList = convertDocumentSnapshotToList(firstQuery)
            val secondQuery =
                mHomeCollection.orderBy(PRICE)
                    .whereEqualTo(PROPERTY_TYPE, propertyName)
                    .whereEqualTo(RESIDENTAIL_TYPE, residentialName)
                    .whereEqualTo(BHK, bhkName)
                    .whereEqualTo(STATE, state)
                    .whereEqualTo(CITY, city)
                    .whereGreaterThanOrEqualTo(PRICE, min)
                    .whereLessThanOrEqualTo(PRICE, max)
                    .get().await().documents.toList()
            val secondList = convertDocumentSnapshotToList(secondQuery)
            Log.d(TAG, "First List $firstList")
            Log.d(TAG, "Second List $secondList")
            return getResultList(firstList, secondList)
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $residentialName $bhkName $state $city $min $max")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    private fun getResultList(
        firstList: ArrayList<Home?>,
        secondList: ArrayList<Home?>
    ): ArrayList<Home> {
        val originalList = ArrayList<Home>()
        try {
            firstList.forEach { firstHome ->
                secondList.forEach { secondHome ->
                    if (firstHome?.uid == secondHome?.uid) {
                        if (secondHome != null) {
                            Log.d(TAG, "original data are added $secondHome")
                            originalList.add(secondHome)
                        }
                    }
                }
            }
            return originalList
        } catch (
            e: Exception
        ) {
            Log.d(TAG, "$e")
        }
        return originalList
    }

    suspend fun searchPropertyTypeAsBhk(
        query: String,
        propertyName: String,
        bhkName: String
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $propertyName $bhkName ")
            mHomeCollection.orderBy(TITLE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(BHK, bhkName)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $bhkName ")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsBhkAsState(
        query: String,
        propertyName: String,
        bhkName: String,
        state: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $propertyName $bhkName $state ")
            mHomeCollection.orderBy(TITLE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $bhkName $state ")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsBhkAsStateAsCity(
        query: String,
        propertyName: String,
        bhkName: String,
        state: String,
        city: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $propertyName $bhkName $state $city")
            mHomeCollection.orderBy(TITLE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $bhkName $state $city")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsBhkAsStateAsCityAsMinMax(
        query: String,
        propertyName: String,
        bhkName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ): List<Home> {
        return try {
            Log.d(TAG, "$query $propertyName $bhkName $state $city $min $max")
            val firstQuery =
                mHomeCollection.orderBy(TITLE).startAt(query).endAt(query + "\uf8ff").get()
                    .await().documents.toList()
            val firstList = convertDocumentSnapshotToList(firstQuery)
            val secondQuery =
                mHomeCollection.orderBy(PRICE)
                    .whereEqualTo(PROPERTY_TYPE, propertyName)
                    .whereEqualTo(BHK, bhkName)
                    .whereEqualTo(STATE, state)
                    .whereEqualTo(CITY, city)
                    .whereGreaterThanOrEqualTo(PRICE, min)
                    .whereLessThanOrEqualTo(PRICE, max)
                    .get().await().documents.toList()
            val secondList = convertDocumentSnapshotToList(secondQuery)
            Log.d(TAG, "First List $firstList")
            Log.d(TAG, "Second List $secondList")
            return getResultList(firstList, secondList)
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $bhkName $state $city $min $max")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsState(
        query: String,
        propertyName: String,
        state: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $propertyName $state ")
            mHomeCollection.orderBy(TITLE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(STATE, state)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $state ")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsStateAsCity(
        query: String,
        propertyName: String,
        state: String,
        city: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $propertyName $state $city")
            mHomeCollection.orderBy(TITLE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $state $city")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsStateAsCityAsMinMax(
        query: String,
        propertyName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ): List<Home> {
        return try {
            Log.d(TAG, "$query $propertyName $state $city $min $max")
//            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
//                .startAt(query).endAt(query + "\uf8ff")
//                .whereEqualTo(STATE, state)
//                .whereEqualTo(CITY, city)
//                .whereGreaterThanOrEqualTo(PRICE, min)
//                .whereLessThanOrEqualTo(PRICE, max)
//                .get().await().documents.toList()
            val firstQuery =
                mHomeCollection.orderBy(TITLE).startAt(query).endAt(query + "\uf8ff").get()
                    .await().documents.toList()
            val firstList = convertDocumentSnapshotToList(firstQuery)
            val secondQuery =
                mHomeCollection.orderBy(PRICE)
                    .whereEqualTo(PROPERTY_TYPE, propertyName)
                    .whereEqualTo(STATE, state)
                    .whereEqualTo(CITY, city)
                    .whereGreaterThanOrEqualTo(PRICE, min)
                    .whereLessThanOrEqualTo(PRICE, max)
                    .get().await().documents.toList()
            val secondList = convertDocumentSnapshotToList(secondQuery)
            Log.d(TAG, "First List $firstList")
            Log.d(TAG, "Second List $secondList")
            return getResultList(firstList, secondList)
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $state $city $min $max")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsMinMax(
        query: String,
        propertyName: String,
        min: Int,
        max: Int
    ): List<Home> {
        return try {
            Log.d(TAG, "$query $propertyName $min $max")
//            mHomeCollection.orderBy(PRICE)
//                .startAt(query).endAt(query + "\uf8ff")
//                .whereEqualTo(PROPERTY_TYPE, propertyName)
//                .whereGreaterThanOrEqualTo(PRICE, min)
//                .whereLessThanOrEqualTo(PRICE, max)
//                .get().await().documents.toList()
            val firstQuery =
                mHomeCollection.orderBy(TITLE).startAt(query).endAt(query + "\uf8ff").get()
                    .await().documents.toList()
            val firstList = convertDocumentSnapshotToList(firstQuery)
            val secondQuery =
                mHomeCollection.orderBy(PRICE)
                    .whereEqualTo(PROPERTY_TYPE, propertyName)
                    .whereGreaterThanOrEqualTo(PRICE, min)
                    .whereLessThanOrEqualTo(PRICE, max)
                    .get().await().documents.toList()
            val secondList = convertDocumentSnapshotToList(secondQuery)
            Log.d(TAG, "First List $firstList")
            Log.d(TAG, "Second List $secondList")
            return getResultList(firstList, secondList)
        } catch (e: Exception) {
            Log.d(TAG, "$query $propertyName $min $max")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchBhkAsState(
        query: String,
        bhkName: String,
        state: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $bhkName $state")
            mHomeCollection.orderBy(TITLE)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $bhkName $state")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchBhkAsStateAsCity(
        query: String,
        bhkName: String,
        state: String,
        city: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $bhkName $state $city")
            mHomeCollection.orderBy(TITLE)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $bhkName $state $city")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchStateAsCity(
        query: String,
        state: String,
        city: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$query $state $city")
            mHomeCollection.orderBy(TITLE)
                .startAt(query).endAt(query + "\uf8ff")
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$query $state $city")
            Log.d(TAG, "$e")
            emptyList()
        }
    }


    suspend fun searchPropertyType(propertyName: String): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName).get()
                .await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchBhk(bhk: String): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$bhk")
            mHomeCollection.orderBy(PRICE).whereEqualTo(BHK, bhk).get()
                .await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$bhk")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchState(state: String): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$state")
            mHomeCollection.orderBy(PRICE).whereEqualTo(STATE, state).get()
                .await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$state")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsResidentialType(
        propertyName: String,
        residentialName: String
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $residentialName")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(RESIDENTAIL_TYPE, residentialName)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $residentialName")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsResidentialTypeAsBhk(
        propertyName: String,
        residentialName: String,
        bhkName: String
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $residentialName $bhkName")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(RESIDENTAIL_TYPE, residentialName)
                .whereEqualTo(BHK, bhkName)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $residentialName $bhkName")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsResidentialTypeAsBhkAsState(
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $residentialName $bhkName $state")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(RESIDENTAIL_TYPE, residentialName)
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $residentialName $bhkName $state")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCity(
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String,
        city: String
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $residentialName $bhkName $state $city")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(RESIDENTAIL_TYPE, residentialName)
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $residentialName $bhkName $state $city")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsResidentialTypeAsBhkAsStateAsCityAsMinMax(
        propertyName: String,
        residentialName: String,
        bhkName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $residentialName $bhkName $state $city $min $max")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(RESIDENTAIL_TYPE, residentialName)
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .whereGreaterThanOrEqualTo(PRICE, min)
                .whereLessThanOrEqualTo(PRICE, max)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $residentialName $bhkName $state $city $min $max")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsBhk(
        propertyName: String,
        bhkName: String
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $bhkName")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(BHK, bhkName)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $bhkName")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsBhkAsState(
        propertyName: String,
        bhkName: String,
        state: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $bhkName $state")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $bhkName $state")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsBhkAsStateAsCity(
        propertyName: String,
        bhkName: String,
        state: String,
        city: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $bhkName $state $city")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $bhkName $state $city")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsBhkAsStateAsCityAsMinMax(
        propertyName: String,
        bhkName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $bhkName $state $city $min $max")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .whereGreaterThanOrEqualTo(PRICE, min)
                .whereLessThanOrEqualTo(PRICE, max)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $bhkName $state $city $min $max")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsState(
        propertyName: String,
        state: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $state")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(STATE, state)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $state")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsStateAsCity(
        propertyName: String,
        state: String,
        city: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $state $city")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $state $city")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsStateAsCityAsMinMax(
        propertyName: String,
        state: String,
        city: String,
        min: Int,
        max: Int
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $state $city $min $max")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .whereGreaterThanOrEqualTo(PRICE, min)
                .whereLessThanOrEqualTo(PRICE, max)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $state $city $min $max")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchPropertyTypeAsMinMax(
        propertyName: String,
        min: Int,
        max: Int
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$propertyName $min $max")
            mHomeCollection.orderBy(PRICE).whereEqualTo(PROPERTY_TYPE, propertyName)
                .whereGreaterThanOrEqualTo(PRICE, min)
                .whereLessThanOrEqualTo(PRICE, max)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$propertyName $min $max")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchBhkAsState(
        bhkName: String,
        state: String,
    ): List<DocumentSnapshot> {
        return try {
            mHomeCollection.orderBy(PRICE)
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$bhkName $state")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchBhkAsStateAsCity(
        bhkName: String,
        state: String,
        city: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, "$bhkName $state $city")
            mHomeCollection.orderBy(PRICE)
                .whereEqualTo(BHK, bhkName)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, "$bhkName $state $city")
            Log.d(TAG, "$e")
            emptyList()
        }
    }

    suspend fun searchStateAsCity(
        state: String,
        city: String,
    ): List<DocumentSnapshot> {
        return try {
            Log.d(TAG, " $state $city")
            mHomeCollection.orderBy(PRICE)
                .whereEqualTo(STATE, state)
                .whereEqualTo(CITY, city)
                .get().await().documents.toList()
        } catch (e: Exception) {
            Log.d(TAG, " $state $city")
            Log.d(TAG, "$e")
            emptyList()
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

    private fun convertDocumentSnapshotToOccupiedList(occupiedHome: List<DocumentSnapshot>): ArrayList<OccupiedHome?> {
        val list: ArrayList<OccupiedHome?> = ArrayList()
        occupiedHome.forEach { data ->
            val occupiedHome = data.toObject(OccupiedHome::class.java)
            list.add(occupiedHome)
        }
        return list
    }

    private fun convertDocumentSnapshotToUser(users: List<DocumentSnapshot>): ArrayList<User?> {
        val list: ArrayList<User?> = ArrayList()
        users.forEach { data ->
            val user = data.toObject(User::class.java)
            list.add(user)
        }
        return list
    }

}