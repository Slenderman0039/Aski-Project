package it.am.gpsmodule.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize



@Parcelize
class MapCar(override var brand:String?, override var modello:String?, override var anno:String?, override var carburante:String?, override var targa:String?, override var  id:Int, var Latitudine:Float, var Longitudine:Float): Car(brand,modello,anno,carburante,targa,id),
    Parcelable
{

}