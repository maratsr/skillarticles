package ru.skillbranch.skillarticles.viewmodels.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.provider.Settings
import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.skillbranch.skillarticles.data.repositories.ProfileRepository
import ru.skillbranch.skillarticles.viewmodels.base.*
import java.io.InputStream

class ProfileViewModel @ViewModelInject constructor(
    @Assisted handle: SavedStateHandle,
    private val repository: ProfileRepository,
) : BaseViewModel<ProfileState>(handle, ProfileState()) {

    private val activityResults = MutableLiveData<Event<PendingAction>>()


    private val storagePermissions = listOf<String>( // Необходимые разрешения для работы Fragment-а
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    init {
        subscribeOnDataSource(repository.getProfile()) { profile, state ->
            profile ?: return@subscribeOnDataSource null

                state.copy(
                    avatar = profile.avatar,
                    name = profile.name,
                    about = profile.about,
                    rating = profile.rating,
                    respect = profile.respect
                )
        }
    }

    fun startForResult(action: PendingAction) {
        activityResults.value = Event(action)
    }

    fun handlePermission(permissionsResult: Map<String, Pair<Boolean, Boolean>>) { // Обработка результата запроса разрешений
        val isAllGranted = !permissionsResult.values.map { it.first }.contains(false)
        val isAllMayBeShown = permissionsResult.values.map { it.second }.contains(false)

        when {
            isAllGranted -> executePendingAction() // Все разрешения выданы - запустить action
            isAllMayBeShown -> executeOpenSettings() // Если нажата don't ask again - то остается только вызвать экран настроек
            else -> { // Перезапросим разрешения (покажем snackBar
                val msg = Notify.ErrorMessage(
                    "Need permissions for storage",
                    "Retry"
                ) { requestPermissions(storagePermissions) }
                notify(msg)
            }
        }
    }

    fun executeOpenSettings() {
        val errHandler = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:ru.skillbranch.skillarticles")
            }
            startForResult(PendingAction.SettingsAction(intent))
        }
        notify(
            Notify.ErrorMessage("Need permissions for storage","Open settings") { errHandler() }
        )
    }

    fun executePendingAction() {
        val pendingAction = currentState.pendingAction ?: return
        startForResult(pendingAction)
    }

    fun handleUploadPhoto(inputStream: InputStream?) { // Обработка загрузки фото
        inputStream ?: return

        launchSafety(null, {updateState { it.copy(pendingAction = null) }} ) {
            // Чтение файла в background-е
            val byteArray = withContext(Dispatchers.IO) {
                inputStream.use {//use закрывает inputstream по окончании обработки
                    it.readBytes()
                }
            }

            val requestFile = byteArray.toRequestBody("image/jpeg".toMediaType())
            // любое имя файла, так как backend перезапишет
            val body = MultipartBody.Part.createFormData("avatar", "name.jpg", requestFile)
            repository.uploadAvatar(body)
        }
    }

    fun observeActivityResults(owner: LifecycleOwner, handle: (action: PendingAction) -> Unit) {
        activityResults.observe(owner, EventObserver { handle(it) })
    }

    fun handleEditAction(source: Uri, destination: Uri) {
        updateState { it.copy(pendingAction = PendingAction.EditAction(source to destination)) }
        requestPermissions(storagePermissions)
    }


    fun handleCameraAction(destination: Uri) {
        updateState { it.copy(pendingAction = PendingAction.CameraAction(destination)) }
        requestPermissions(storagePermissions)
    }

    fun handleGalleryAction() {
        updateState { it.copy(pendingAction = PendingAction.GalleryAction("image/jpeg")) }
        requestPermissions(storagePermissions)
    }

    fun handleDeleteAction() {
        TODO("Not yet implemented")
        // use repository method
    }

}

data class ProfileState(
    val avatar: String? = null,
    val name: String? = null,
    val about: String? = null,
    val rating: Int = 0,
    val respect: Int = 0,
    val pendingAction: PendingAction? = null
) : IViewModelState {
    override fun save(outState: SavedStateHandle) {
        outState.set("pendingAction", pendingAction)
    }

    override fun restore(savedState: SavedStateHandle): IViewModelState {
        return copy(pendingAction = savedState["pendingAction"])
    }

}

sealed class PendingAction() : Parcelable { // Обертка, вызываемые на ViewModel-е action-ы
    abstract val payload: Any?

    @Parcelize // Автоматическая реализация Parcelable
    data class GalleryAction(override val payload: String) : PendingAction()

    @Parcelize
    data class SettingsAction(override val payload: Intent) : PendingAction()

    @Parcelize
    data class CameraAction(override val payload: Uri) : PendingAction() // payload - uri по которому будет сохранено изображение с камеры

    data class EditAction(override val payload: Pair<Uri, Uri>) : PendingAction(), Parcelable {
        constructor(parcel: Parcel) : this(Uri.parse(parcel.readString()) to Uri.parse(parcel.readString()))

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(payload.first.toString())
            parcel.writeString(payload.second.toString())
        }

        companion object CREATOR : Parcelable.Creator<EditAction> {
            override fun createFromParcel(parcel: Parcel): EditAction {
                return EditAction(parcel)
            }

            override fun newArray(size: Int): Array<EditAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
