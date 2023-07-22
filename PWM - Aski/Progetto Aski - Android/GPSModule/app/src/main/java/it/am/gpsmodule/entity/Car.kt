package it.am.gpsmodule.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class Car(open var brand:String?, open var modello:String?, open var anno:String?, open var carburante:String?,open var targa:String?,open var id:Int):Parcelable
{

    constructor(brand:String?,modello:String?,anno:String?,carburante:String?,targa:String?) : this(brand,modello,anno,carburante,targa,0)
    fun completo(): Boolean{
        if(brand != null && modello != null && anno != null && carburante != null && targa != null)
            return true
        return false
    }
    fun reset(){
        brand = null
        modello = null
        anno = null
        carburante = null
        targa = null
    }
}