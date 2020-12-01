package ru.skillbranch.skillarticles.di.scopes

import javax.inject.Scope

//Пример кастомной области видимости для инъекций (не путать с ActivityScoped Hilt-а)
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope {
}