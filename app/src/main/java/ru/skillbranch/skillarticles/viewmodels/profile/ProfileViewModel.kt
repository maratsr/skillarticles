package ru.skillbranch.skillarticles.viewmodels.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.provider.Settings
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import kotlinx.android.parcel.Parcelize
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.skillbranch.skillarticles.data.repositories.ProfileRepository
import ru.skillbranch.skillarticles.viewmodels.base.*
import java.io.InputStream

class ProfileViewModel(handle: SavedStateHandle) :
    BaseViewModel<ProfileState>(handle, ProfileState()) {

    val repository = ProfileRepository
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

    private fun startForResult(action: PendingAction) {
        activityResults.value = Event(action)
    }

    fun handleTestAction(uri: Uri) {
        //val pendingAction = PendingAction.GalleryAction("image/jpeg")
        val pendingAction = PendingAction.CameraAction(uri)
        updateState { it.copy(pendingAction = pendingAction) }
        requestPermissions(storagePermissions)
    }

    fun handlePermission(permissionsResult: Map<String, Pair<Boolean, Boolean>>) { // Обработка результата запроса разрешений
        val arePermissionsGranted = !permissionsResult.values.map { it.first }.contains(false)
        val isPermissionRequestBlocked = permissionsResult.values.map { it.second }.contains(false)

        when {
            arePermissionsGranted -> executePendingAction() // Все разрешения выданы - запустить action
            isPermissionRequestBlocked -> executeOpenSettings() // Если нажата don't ask again - то остается только вызвать экран настроек
            else -> { // Перезапросим разрешения (покажем snackBar
                val msg = Notify.ErrorMessage(
                    "Need permissions for storage",
                    "Retry"
                ) { requestPermissions(storagePermissions) }
                notify(msg)
            }
        }
    }

    private fun executeOpenSettings() {
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

    private fun executePendingAction() {
        val pendingAction = currentState.pendingAction ?: return
        startForResult(pendingAction)
    }

    fun handleUploadedPhoto(inputStream: InputStream?) { // Обработка загрузки фото
        inputStream ?: return

        val byteArray = inputStream.use {//use закрывает inputstream по окончании обработки
            it.readBytes()
        }

        val requestFile = byteArray.toRequestBody("image/jpeg".toMediaType())
        // любое имя файла, так как backend перезапишет
        val body = MultipartBody.Part.createFormData("avatar", "name.jpg", requestFile)

        launchSafety {
            repository.uploadAvatar(body)
        }
    }

    fun observeActivityResults(owner: LifecycleOwner, handle: (action: PendingAction) -> Unit) {
        activityResults.observe(owner, EventObserver { handle(it) })
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
}