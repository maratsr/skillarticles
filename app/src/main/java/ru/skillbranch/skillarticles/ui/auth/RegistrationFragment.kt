package ru.skillbranch.skillarticles.ui.auth


import androidx.annotation.VisibleForTesting
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.savedstate.SavedStateRegistryOwner
import kotlinx.android.synthetic.main.fragment_registration.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.RootActivity
import ru.skillbranch.skillarticles.ui.base.BaseFragment
import ru.skillbranch.skillarticles.viewmodels.auth.AuthViewModel


/*
Реализуй фрагмент RegistrationFragment и соответствующие методы в AuthViewModel и RootRepository
метод регистрации пользователя должен соответствовать следующей сигнатуре
fun handleRegister(name:String, login:String, password:String, dest:Int?) где
name - Имя пользователя
где login - email пользователя
где password - пароль пользователя
где dest - идентификатор точки назначения
после регистрации метод должен проводить валидацию корректности email, имени и
пароля (имя не короче 3 символов, пароль не короче 8 символов без спецзнаков - только буквы и цифры),
после регистрации пользователя необходимо вернуть на предыдущую страницу или в профиль пользователя в зависимости от dest.
Переход на страницу авторизации должен быть доступен с AuthFragment при клике по TextView c
идентификатором tv_register (оформить аналогично с tv_access_code, tv_privacy)
 */


class RegistrationFragment() : BaseFragment<AuthViewModel>()  {

    var _mockFactory:((SavedStateRegistryOwner)-> ViewModelProvider.Factory)? = null

    override val viewModel: AuthViewModel by viewModels(){
        _mockFactory?.invoke(this)?: defaultViewModelProviderFactory
    }
    override val layout: Int = R.layout.fragment_registration
    private val args: AuthFragmentArgs by navArgs()


    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    constructor( mockRoot: RootActivity, mockFactory: ((SavedStateRegistryOwner)-> ViewModelProvider.Factory)? = null) : this() {
        _mockRoot = mockRoot
        _mockFactory = mockFactory
    }

    override fun setupViews() {
        btn_sign_up.setOnClickListener{
            viewModel.handleRegister(
                et_name.text.toString(), et_login.text.toString(), et_password.text.toString(),
                args.privateDestination
            )
        }

        et_password.doAfterTextChanged {
            wrap_password.error =
                if (!it.toString().matches(AuthViewModel.REGEX_PASSWORD_CHECK)) AuthViewModel.INVALID_PASSWORD_MSG
                else null
        }

        et_name.doAfterTextChanged {
            wrap_name.error =
                if (!it.toString().matches(AuthViewModel.REGEX_NAME_CHECK)) AuthViewModel.INVALID_NAME_MSG
                else null
        }

        et_login.doAfterTextChanged {
            wrap_login.error =
                if (!it.toString().matches(AuthViewModel.REGEX_EMAIL_CHECK)) AuthViewModel.INVALID_EMAIL_MSG
                else null
        }
    }
}