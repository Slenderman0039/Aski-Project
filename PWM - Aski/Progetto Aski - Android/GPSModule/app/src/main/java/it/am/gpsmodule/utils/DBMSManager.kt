package it.am.gpsmodule.utils

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import it.am.gpsmodule.R
import it.am.gpsmodule.entity.Utente
import it.am.gpsmodule.entity.UtenteRegistrazione
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DBMSManager {
    companion object{
        var utente:Utente? = null
         fun querySelect(query:String,callback:  (result: JsonArray) -> Unit,errorcallback:()->Unit){
            Log.e("TAG","sono qui: " + query)
            ClientNetwork.retrofit.querySelect(query).enqueue(
                object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.isSuccessful) {
                                Log.e("TAG SELECT", (response.body()?.get("queryset").toString()))
                                callback(response.body()?.get("queryset") as JsonArray)
                        } else{
                                Log.e("TAG SELECT",response.message())
                                Log.e("TAG SELECT","Risposta SELECT non andata a buon fine")
                            }
                    }
                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.e("TAG SELECT", "Errore SELECT!")
                        Log.e("TAG SELECT",call.toString())
                        Log.e("TAG SELECT", t?.message as String)
                        errorcallback()
                    }
                }
            )
        }
        //Funzione INSERT
        fun queryInsert(query: String,callback: (result: JsonElement) -> Unit,errorcallback:()->Unit){
            Log.e("TAG","sono qui INSERT: " + query)
            ClientNetwork.retrofit.queryInsert(query).enqueue(
                object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.isSuccessful) {
                                Log.e("TAG INSERT",call.toString())
                                Log.e("TAG INSERT", (response.body()?.get("queryset").toString()))
                                callback(response.body()?.get("queryset") as JsonElement)
                        } else{
                                Log.e("TAG INSERT",call.toString())
                                Log.e("TAG INSERT","Risposta INSERT non andata a buon fine")
                            }
                        }
                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.e("TAG INSERT", "Errore INSERT!")
                        Log.e("TAG INSERT",call.toString())
                        Log.e("TAG INSERT", t?.message as String)
                        errorcallback()
                    }
                }
            )
        }
        //funzione DELETE
        fun queryDelete(query:String,callback: (result: JsonElement) -> Unit,errorcallback:()->Unit){
            Log.e("TAG","sono qui DELETE: " + query)
            ClientNetwork.retrofit.queryDelete(query).enqueue(
                object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.isSuccessful) {
                                Log.e("TAG DELETE",call.toString())
                                Log.e("TAG DELETE", (response.body()?.get("queryset").toString()))
                                callback(response.body()?.get("queryset") as JsonElement)

                        }else{
                            Log.e("TAG DELETE",call.toString())
                            Log.e("TAG DELETE","Risposta DELETE non andata a buon fine")
                        }
                    }
                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.e("TAG DELETE", "Errore DELETE!")
                        Log.e("TAG DELETE",call.toString())
                        Log.e("TAG DELETE", t?.message as String)
                        errorcallback()
                    }
                }
            )
        }
        //funzione UPDATE
        fun queryUpdate(query: String,callback: (result: JsonElement) -> Unit,errorcallback:()->Unit){
            Log.e("TAG","sono qui UPDATE: " + query)
            ClientNetwork.retrofit.queryUpdate(query).enqueue(
                object : Callback<JsonObject> {
                    override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                        if (response.isSuccessful) {
                                Log.e("TAG UPDATE",call.toString())
                                Log.e("TAG UPDATE", (response.body()?.get("queryset").toString()))
                                callback(response.body()?.get("queryset") as JsonElement)
                        } else{

                                Log.e("TAG UPDATE",call.toString())
                                Log.e("TAG UPDATE","Risposta UPDATE non andata a buon fine")
                            }
                    }
                    override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                        Log.e("TAG UPDATE", "Errore UPDATE!")
                        Log.e("TAG UPDATE",call.toString())
                        Log.e("TAG UPDATE", t?.message as String)
                        errorcallback()
                    }
                }
            )
        }
    }

}