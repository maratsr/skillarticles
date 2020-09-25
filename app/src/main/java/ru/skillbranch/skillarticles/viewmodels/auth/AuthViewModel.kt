package ru.skillbranch.skillarticles.viewmodels.auth

import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.data.repositories.RootRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand
import ru.skillbranch.skillarticles.viewmodels.base.Notify


class AuthViewModel(handle: SavedStateHandle) : BaseViewModel<AuthState>(handle, AuthState()), IAuthViewModel{

    companion object { //static class consts
        val REGEX_NAME_CHECK="^[а-яА-Яa-zA-Z0-9_.-]{3,}\$".toRegex()
        val REGEX_PASSWORD_CHECK="^[а-яА-Яa-zA-Z0-9_.-]{8,}\$".toRegex()
        val REGEX_EMAIL_CHECK = android.util.Patterns.EMAIL_ADDRESS.toRegex()

        val INVALID_PASSWORD_MSG = "Password must be at least 8 characters long and contain only letters and numbers"
        val INVALID_EMAIL_MSG = "Incorrect email entered"
        val INVALID_NAME_MSG = "The name must be at least 3 characters long and contain only letters and numbers and can also contain the characters \"-\" and \"_\""

        val INVALID_CONFIRM_MSG = "Password and confirm isn't equal"
        val INVALID_BLANK_FIELD = "Name, login, password it is required fields and not must be empty"

    }

    private val repository = RootRepository

    init {
        subscribeOnDataSource(repository.isAuth()){isAuth, state ->
            state.copy(isAuth = isAuth)
        }
    }

    override fun handleLogin(login:String, pass:String, dest:Int?){
        launchSafety(null, { navigate(NavigationCommand.FinishLogin(dest)) }) {
            repository.login(login, pass)
        }
    }

    override fun handleRegister(name: String, login: String, password: String, dest: Int?) {
        if (name.isBlank() || login.isBlank() || password.isBlank())
            notify( Notify.ErrorMessage(INVALID_BLANK_FIELD)).also {return}
        if (!name.matches(REGEX_NAME_CHECK))
            notify( Notify.ErrorMessage(INVALID_NAME_MSG)).also {return}
        if (!password.matches(REGEX_PASSWORD_CHECK))
            notify( Notify.ErrorMessage(INVALID_PASSWORD_MSG)).also {return}
        if (!login.matches(REGEX_EMAIL_CHECK))
            notify( Notify.ErrorMessage(INVALID_EMAIL_MSG)).also {return}

        launchSafety(null, { navigate(NavigationCommand
            .FinishLogin(if (dest != null && dest < 0)  null else dest)) })
        {
            repository.signUp(name, login, password)
        }
    }
}

data class AuthState(val isAuth: Boolean = false): IViewModelState