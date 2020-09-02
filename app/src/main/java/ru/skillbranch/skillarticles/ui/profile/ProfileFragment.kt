package ru.skillbranch.skillarticles.ui.profile


import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions.circleCropTransform
import kotlinx.android.synthetic.main.fragment_profile.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.ui.base.Binding
import ru.skillbranch.skillarticles.ui.delegates.RenderProp
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.profile.PendingAction
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileState
import ru.skillbranch.skillarticles.viewmodels.profile.ProfileViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : BaseFragment<ProfileViewModel>() {

    override val viewModel: ProfileViewModel by viewModels()
    override val layout: Int = R.layout.fragment_profile
    override val binding: ProfileBinding by lazy { ProfileBinding()}

    // Callback на запрос нескольких permissions
    private val permissionResultCallback =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            Log.e("ProfileFragment","request runtime permissions result: $result")
            val permissionsResult = result.mapValues { (permission, isGranted) ->
                if (isGranted) {
                    true to true
                } else {
                    false to ActivityCompat.shouldShowRequestPermissionRationale( // можно ли повторно запросить permission у пользователя (Запрет повторного запроса есть или нет)
                        requireActivity(),
                        permission
                    )
                }
            }
            Log.e("ProfileFragment","request runtime permissions result: $permissionsResult")

            viewModel.handlePermission(permissionsResult)
        }

    private val galleryResultCallback = // результат выбора фото из галлереи
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            if (result != null) {
                val inputStream = requireContext().contentResolver.openInputStream(result)
                viewModel.handleUploadedPhoto(inputStream)
            }
        }


    private val settingsResultCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            //DO something if needed
        }

    private val cameraResultCallback = // Получение изображения с камеры
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            val (payload) = binding.pendingAction as PendingAction.CameraAction

            if (result) { // Фотография была сделана и сохранена
                requireContext().contentResolver.openInputStream(payload)
                    ?.let { viewModel.handleUploadedPhoto(it) }
            } else { //удалить временный файл по Uri
                removeTempUri(payload)
            }
        }

    override fun setupViews() {
        iv_avatar.setOnClickListener {
            val uri = prepareTempUri()
            viewModel.handleTestAction(uri)
        }

        viewModel.observePermissions(viewLifecycleOwner) {
            // launch callback for request permissions
            permissionResultCallback.launch(it.toTypedArray())
        }

        viewModel.observeActivityResults(viewLifecycleOwner) {
            when (it) {
                is PendingAction.GalleryAction -> galleryResultCallback.launch(it.payload)
                is PendingAction.SettingsAction -> settingsResultCallback.launch(it.payload)
                is PendingAction.CameraAction -> cameraResultCallback.launch(it.payload)
            }
        }
    }

    private fun updateAvatar(avatarUrl: String) {
        if (avatarUrl.isBlank()) {
            Glide.with(root)
                .load(R.drawable.ic_avatar)
                .into(iv_avatar)
        } else {
            Glide.with(root)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_avatar)
                .apply(circleCropTransform())
                .into(iv_avatar)
        }
    }

    private fun removeTempUri(uri: Uri) {
        requireContext().contentResolver.delete(uri, null, null)
    }

    private fun prepareTempUri(): Uri { // Подготовим ContentUri для сохранения файла с камеры
        val timestamp = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val tempFile = File.createTempFile( // Пустой временный файл с уникальным именем
            "JPEG_${timestamp}",
            ".jpg",
            storageDir
        )

        val contentUri = FileProvider.getUriForFile( // Составляем URI к файлу
            requireContext(),
            "${requireContext().packageName}.provider",
            tempFile
        )

        Log.d("ProfileFragment", "file uri(toUri): ${tempFile.toUri()}")
        Log.d("ProfileFragment", "content uri: $contentUri")

        return contentUri // данный URI можно передать в другие приложения
    }

    inner class ProfileBinding() : Binding() {
        var pendingAction: PendingAction? = null
        var avatar by RenderProp("") {updateAvatar(it) }
        var name by RenderProp("") { tv_name.text = it }
        var about by RenderProp("") { tv_about.text = it }
        var rating by RenderProp(0) { tv_rating.text = "Rating: $it" }
        var respect by RenderProp(0) { tv_respect.text = "Respect: $it" }

        override fun bind(data: IViewModelState) {
            data as ProfileState
            if (data.avatar != null){ avatar = data.avatar }
            if (data.name != null){ name = data.name }
            if (data.about != null){ about = data.about }
            rating = data.rating
            respect = data.respect
            if (data.pendingAction != null) {pendingAction = data.pendingAction}
        }
    }
}
