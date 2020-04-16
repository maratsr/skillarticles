package ru.skillbranch.skillarticles.viewmodels

import androidx.lifecycle.SavedStateHandle
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.data.repositories.RootRepository
import ru.skillbranch.skillarticles.viewmodels.base.BaseViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

class RootViewModel(handle: SavedStateHandle): BaseViewModel<RootState>(handle, RootState()) {
    private val repository = RootRepository
    val privateRouters = listOf(R.id.nav_profile)

    init{
        subscribeOnDataSource(repository.isAuth()) {isAuth, state ->
            state.copy(isAuth=isAuth)

        }
    }

    override fun navigate(command: NavigationCommand) {
        when(command) {
            is NavigationCommand.To -> {
                // Если не авториазован - перекидываем на логин c передачей точки назначения
                if(privateRouters.contains(command.destination) && !currentState.isAuth){
                    super.navigate(NavigationCommand.StartLogin(command.destination))
                }else{
                    super.navigate(command)
                }
            }
            else -> super.navigate(command)
        }
    }
}

data class RootState(val isAuth: Boolean=false): IViewModelState {

}
