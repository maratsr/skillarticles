package ru.skillbranch.skillarticles.di.scopes

import javax.inject.Scope

//Пример кастомной области видимости для инъекций
@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class ActivityScope {
}