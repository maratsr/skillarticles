package ru.skillbranch.skillarticles.data.delegates

import android.util.Log
import ru.skillbranch.skillarticles.data.local.PrefManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//Реализуй делегат PrefDelegate<T>(private val defaultValue: T) : ReadWriteProperty<PrefManager, T?> (ru.skillbranch.skillarticles.data.delegates.PrefDelegate)
// возвращающий значений примитивов (Boolean, String, Float, Int, Long)
//
//Пример: var storedBoolean by PrefDelegate(false)
//var storedString by PrefDelegate("")
//var storedFloat by PrefDelegate(0f)
//var storedInt by PrefDelegate(0)
//var storedLong by PrefDelegate(0)
//
//Реализуй в классе PrefManager(context:Context) (ru.skillbranch.skillarticles.data.local.PrefManager)
//свойство val preferences : SharedPreferences проинициализированое экземпляром SharedPreferences приложения.
//И метод fun clearAll() - очищающий все сохраненные значения SharedPreferences приложения.
//Использовать PrefManager из androidx (import androidx.preference.PreferenceManager)
class PrefDelegate<T>(private val defaultValue: T) : ReadWriteProperty<PrefManager, T?> {
    //private var storedValue: T? = null
    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        if  (thisRef.preferences.all[property.name] == null)
            setValue(thisRef, property, defaultValue)
        return thisRef.preferences.all[property.name] as T?
//        return if (storedValue == null) {
//            storedValue = defaultValue
//            storedValue
//        } else storedValue
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        val valueR:T = value?: defaultValue
        with(thisRef.preferences.edit()) {
            when (valueR) {
                is Boolean -> putBoolean(property.name, valueR).apply()
                is Float -> putFloat(property.name, valueR).apply()
                is Long -> putLong(property.name, valueR).apply()
                is Int -> putInt(property.name, valueR).apply()
                is String -> putString(property.name, valueR).apply()
                else -> throw NotImplementedError("Wrong type of value. Only Boolean, Float, Long, Int, String types are possible!")
            }
        }
    }
}