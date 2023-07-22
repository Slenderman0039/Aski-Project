package it.am.gpsmodule.map

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.navigation.NavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.ViewAnnotationAnchor
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.AnnotationPlugin
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMoveListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.state.FollowPuckViewportState
import com.mapbox.maps.plugin.viewport.viewport
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions
import com.squareup.moshi.Json
import it.am.gpsmodule.R
import it.am.gpsmodule.auth.Login
import it.am.gpsmodule.auth.Registration
import it.am.gpsmodule.auth.registration.sone_registration_fragment
import it.am.gpsmodule.databinding.ActivityMainBinding
import it.am.gpsmodule.databinding.MapDialogBinding
import it.am.gpsmodule.entity.MapCar
import it.am.gpsmodule.entity.Prenotazione
import it.am.gpsmodule.entity.Utente
import it.am.gpsmodule.map.booking.book.BookingActivity
import it.am.gpsmodule.map.booking.information_car
import it.am.gpsmodule.map.filtri.filter
import it.am.gpsmodule.map.navbar.guida.Guida
import it.am.gpsmodule.map.navbar.owncar.MapOwnCarActivityList
import it.am.gpsmodule.map.navbar.payment.PaymentActivity
import it.am.gpsmodule.map.navbar.profile.MapProfiloActivity
import it.am.gpsmodule.map.navbar.riepilogo.Riepilogo
import it.am.gpsmodule.utils.*
import it.am.gpsmodule.utils.fcm.FCMNotification
import it.am.gpsmodule.utils.fcm.FCMNotificationData
import it.am.gpsmodule.utils.fcm.FCMService
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AskiMap : AppCompatActivity() {
    var mapView: MapView? = null
    lateinit var binding: ActivityMainBinding
    private var cars: ArrayList<MapCar>? = ArrayList()
    private var flag = false
    private var flag2 = false
    var receiver: BroadcastReceiver? = null
    var pointManagers = mutableListOf<PointAnnotationManager>()


    companion object {
         var credito:TextView? = null
        var brandFilter: String = "SELEZIONA"
        var kmFilter: Int = 1
        var lgm = LocationGpsManager()
        var aski: AskiMap? = null
        var annotationApi: AnnotationPlugin? = null
        private var inst: AskiMap? = null
        const val SELECTED_ADD_COEF_PX = 25
        val POINT: Point = Point.fromLngLat(13.349641666666667, 38.104243333333336)
        fun instances(): AskiMap? {
            return inst
        }
        var pren_future = ArrayList<Prenotazione>()
    }

    lateinit var locationManager: LocationManager
    lateinit var viewAnnotationManager: ViewAnnotationManager

    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e("TAG", "onCreate()")
        setTitle("Noleggia la tua auto")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); //DISATTIVA LA MODALITA' DARK
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        aski = this
        viewAnnotationManager = binding.mapView.viewAnnotationManager
        inst = this
        LocationGpsManager.getLocation(this,this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lgm.requestPermissions(this)


        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("TAG", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            if(DBMSManager.utente!!.token.isNullOrBlank()){
                //Todo: FARE LA INSERT ed impostare il valore di DBMSManager.utente!!.token
                if(ClientNetwork.isOnline(this)) {
                    DBMSManager.queryInsert("INSERT INTO Notifiche VALUES (${DBMSManager.utente?.id},'$token')",
                        {
                            DBMSManager.utente!!.token = token
                        },
                        {
                            val dialog2 = onCreateDialogFailedConn()
                            dialog2.show()
                            dialog2.findViewById<TextView>(R.id.errore).text =
                                "Errore connessione al server!"
                        })
                }else{
                    val dialog2 = onCreateDialogFailed()
                    dialog2.show()
                    dialog2.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
                }
            }else{
                if(token != DBMSManager.utente!!.token){
                    if(ClientNetwork.isOnline(this)) {
                        DBMSManager.queryInsert("UPDATE Notifiche SET Token = '$token'  WHERE ID=${DBMSManager.utente?.id}",
                            {
                                DBMSManager.utente!!.token = token
                            },
                            {
                                val dialog2 = onCreateDialogFailedConn()
                                dialog2.show()
                                dialog2.findViewById<TextView>(R.id.errore).text =
                                    "Errore connessione al server!"
                            })
                    }else{
                        val dialog2 = onCreateDialogFailed()
                        dialog2.show()
                        dialog2.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
                    }
                }
            }
        })


        //Todo: SAMPLE NOTIFICHE

        /*
        NotificationReceiver.setNotification("AskiMap","Prenotazione",R.drawable.car_icon)
        startAlarm(calendar)*/


        mapView = binding.mapView
        mapView!!.scalebar.enabled = false
        mapView!!.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS,
            // After the style is loaded, initialize the Location component.
            object : Style.OnStyleLoaded {
                override fun onStyleLoaded(style: Style) {
                    addAnnotationToMap(mapView!!.getMapboxMap().cameraState.center)
                    mapView!!.location.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                    }
                }
            }
        )
         pren_future = ArrayList<Prenotazione>()
        GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(1000)
                //Todo: SAMPLE NOTIFICHE

                if (pren_future.size > 0) {
                    val date: Long = System.currentTimeMillis()
                    val sdf: SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                    val sdf2: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    var current_string = sdf.format(date)
                    var current = sdf.parse(current_string)
                    for (i in 0 until pren_future.size) {
                        //CURRENT = ORARIO DI ADESSO
                        //NEWDATE = ORARIO DI INIZIO PRENOTAZIONE
                        //NEWDATE2 = ORARIO DI FINE PRENOTAZIONE
                        var newdate = sdf2.parse(
                            pren_future.get(i).data_prenotazione_inizio.toString().replace("T", " ")
                        )
                        when (current.compareTo(newdate)) {
                            0 -> {
                                //E' INIZIATA ORA LA PRENOTAZIONE
                                binding.chiama.visibility = View.VISIBLE
                                binding.stop.visibility = View.VISIBLE

                                binding.chiama.setOnClickListener {

                                    if (ClientNetwork.isOnline(aski!!)) {
                                        DBMSManager.querySelect("SELECT u.Num_Telefono as telefono FROM Utenti u,Veicoli v WHERE u.ID_Utente = v.ID AND v.Targa = '${
                                            pren_future.get(
                                                i
                                            ).targa_veicolo
                                        }'",
                                            { result ->
                                                runOnUiThread {
                                                    if (result.size() > 0) {
                                                        val obj = (result.get(0) as JsonObject)

                                                        val dialIntent = Intent(Intent.ACTION_DIAL)
                                                        dialIntent.data =
                                                            Uri.parse("tel:" + obj.get("telefono").asString)
                                                        startActivity(dialIntent)
                                                    }
                                                }
                                            }, {
                                                val dialog = onCreateDialogFailedConn()
                                                dialog.show()
                                                dialog.findViewById<TextView>(R.id.errore).text =
                                                    "Errore connessione al server!"
                                            })
                                    } else {
                                        val dialog = onCreateDialogFailed()
                                        dialog.show()
                                        dialog.findViewById<TextView>(R.id.errore).text =
                                            "Non sei connesso ad Internet!"
                                    }
                                }
                                //AL CLICK DELLO STOP
                                binding.stop.setOnClickListener {
                                    if(LocationGpsManager.currentPosition != null) {
                                        if (ClientNetwork.isOnline(aski!!)) {
                                            var current_update =
                                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                            var finale = current_update.format(current)
                                            Log.e("PROVA:", "${finale}")
                                            DBMSManager.queryUpdate(
                                                "UPDATE Prenotazioni SET Data_Prenotazione_fine = '${finale}' WHERE ID_Utente = ${DBMSManager.utente?.id} AND Targa_Veicolo = '${
                                                    pren_future.get(
                                                        i
                                                    ).targa_veicolo
                                                }' AND Data_Prenotazione_Inizio = '${
                                                    pren_future.get(i).data_prenotazione_inizio!!.replace(
                                                        "T",
                                                        " "
                                                    )
                                                }' AND Data_Prenotazione_Fine ='${
                                                    pren_future.get(i).data_prenotazione_fine!!.replace(
                                                        "T",
                                                        " "
                                                    )
                                                }';", { result ->
                                                    runOnUiThread {
                                                        DBMSManager.queryUpdate("UPDATE Veicoli SET Latitudine = '${LocationGpsManager.currentPosition!!.latitude()}', Longitudine= '${LocationGpsManager.currentPosition!!.longitude()}' WHERE Targa = '${pren_future.get(i).targa_veicolo}';",{
                                                                result->
                                                            runOnUiThread{
                                                                cars!!.clear()
                                                                val annotationApi = mapView?.annotations
                                                                for (point in pointManagers) {
                                                                    annotationApi?.removeAnnotationManager(point)
                                                                    Log.e("TAG", pointManagers.size.toString())
                                                                }
                                                                pointManagers.clear()
                                                        DBMSManager.querySelect(
                                                            "SELECT * FROM Veicoli WHERE Targa = '${
                                                                pren_future.get(
                                                                    i
                                                                ).targa_veicolo
                                                            }'", { result2 ->
                                                                runOnUiThread {

                                                                    val obj =
                                                                        result2.get(0) as JsonObject
                                                                    var datafinale =
                                                                        pren_future.get(i).data_prenotazione_inizio.toString()
                                                                            .replace("T", " ")
                                                                            .split(" ")
                                                                    var datafinaledata =
                                                                        datafinale.get(0).split("-")
                                                                    var data_f = finale.split(" ")
                                                                    var data_finale_f =
                                                                        data_f.get(0).split("-")
                                                                    binding.chiama.visibility =
                                                                        View.INVISIBLE
                                                                    binding.stop.visibility =
                                                                        View.INVISIBLE
                                                                    //Todo: Mettere dialog recensione e resoconto costo corsa
                                                                    var dialog = onCreateDialog(
                                                                        pren_future.get(i).targa_veicolo!!,
                                                                        DBMSManager.utente?.id.toString(),
                                                                        BookingActivity.calcoloprezzo_tot(
                                                                            BookingActivity.calcoloPrezzo2(
                                                                                obj["Marca"].asString,
                                                                                obj["Modello"].asString,
                                                                                obj["Anno_Produzione"].asString
                                                                            ),
                                                                            datafinaledata.get(2) + "/" + datafinaledata.get(
                                                                                1
                                                                            ) + "/" + datafinaledata.get(
                                                                                0
                                                                            ) + " " + datafinale.get(
                                                                                1
                                                                            ),
                                                                            data_finale_f.get(2) + "/" + data_finale_f.get(
                                                                                1
                                                                            ) + "/" + data_finale_f.get(
                                                                                0
                                                                            ) + " " + data_f.get(1)
                                                                        ).toString()
                                                                    )
                                                                    dialog.show()
                                                                    dialog.findViewById<TextView>(R.id.inizio).text =
                                                                        datafinaledata.get(2) + "/" + datafinaledata.get(
                                                                            1
                                                                        ) + "/" + datafinaledata.get(
                                                                            0
                                                                        ) + " " + datafinale.get(
                                                                            1
                                                                        )
                                                                    dialog.findViewById<TextView>(R.id.fine).text =
                                                                        data_finale_f.get(2) + "/" + data_finale_f.get(
                                                                            1
                                                                        ) + "/" + data_finale_f.get(
                                                                            0
                                                                        ) + " " + data_f.get(
                                                                            1
                                                                        )
                                                                    dialog.findViewById<TextView>(R.id.totale).text =
                                                                        BookingActivity.calcoloprezzo_tot(
                                                                            BookingActivity.calcoloPrezzo2(
                                                                                obj["Marca"].asString,
                                                                                obj["Modello"].asString,
                                                                                obj["Anno_Produzione"].asString
                                                                            ),
                                                                            datafinaledata.get(2) + "/" + datafinaledata.get(
                                                                                1
                                                                            ) + "/" + datafinaledata.get(
                                                                                0
                                                                            ) + " " + datafinale.get(
                                                                                1
                                                                            ),
                                                                            data_finale_f.get(2) + "/" + data_finale_f.get(
                                                                                1
                                                                            ) + "/" + data_finale_f.get(
                                                                                0
                                                                            ) + " " + data_f.get(1)
                                                                        ).toString() + " €"

                                                                    Toast.makeText(
                                                                        aski,
                                                                        "HAI TERMINATO LA TUA CORSA",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                    pren_future.removeAt(i)
                                                                }
                                                            }, {
                                                                val dialog =
                                                                    onCreateDialogFailedConn()
                                                                dialog.show()
                                                                dialog.findViewById<TextView>(R.id.errore).text =
                                                                    "Errore connessione al server!"
                                                            })
                                                    }
                                                }, {
                                                    val dialog = onCreateDialogFailedConn()
                                                    dialog.show()
                                                    dialog.findViewById<TextView>(R.id.errore).text =
                                                        "Errore connessione al server!"
                                                })
                                                    }
                                                }, {
                                                    val dialog = onCreateDialogFailedConn()
                                                    dialog.show()
                                                    dialog.findViewById<TextView>(R.id.errore).text =
                                                        "Errore connessione al server!"
                                                })

                                        } else {
                                            var dialog = onCreateDialogFailed()
                                            dialog.show()
                                            dialog.findViewById<TextView>(R.id.errore).text =
                                                "Non sei Connesso ad Internet!"
                                        }
                                    }else{
                                        Toast.makeText(aski,"ABILITA IL GPS",Toast.LENGTH_SHORT).show()
                                    }
                                }

                            }
                            1 -> {
                                //PUO' ESSERE NEL MENTRE DELLA PRENOTAZIONE O GIA' TERMINATA
                                var newdate2 = sdf2.parse(
                                    pren_future.get(i).data_prenotazione_fine.toString()
                                        .replace("T", " ")
                                )
                                when (current.compareTo(newdate2)) {
                                    0 -> {
                                        binding.chiama.visibility = View.INVISIBLE
                                        binding.stop.visibility = View.INVISIBLE

                                        if(LocationGpsManager.currentPosition != null) {
                                            if (ClientNetwork.isOnline(aski!!)) {
                                                var current_update =
                                                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                                var finale = current_update.format(current)
                                                Log.e("PROVA:", "${finale}")
                                                DBMSManager.queryUpdate(
                                                    "UPDATE Prenotazioni SET Data_Prenotazione_fine = '${finale}' WHERE ID_Utente = ${DBMSManager.utente?.id} AND Targa_Veicolo = '${
                                                        pren_future.get(
                                                            i
                                                        ).targa_veicolo
                                                    }' AND Data_Prenotazione_Inizio = '${
                                                        pren_future.get(i).data_prenotazione_inizio!!.replace(
                                                            "T",
                                                            " "
                                                        )
                                                    }' AND Data_Prenotazione_Fine ='${
                                                        pren_future.get(i).data_prenotazione_fine!!.replace(
                                                            "T",
                                                            " "
                                                        )
                                                    }';", { result ->
                                                        runOnUiThread {
                                                            DBMSManager.queryUpdate("UPDATE Veicoli SET Latitudine = '${LocationGpsManager.currentPosition!!.latitude()}', Longitudine= '${LocationGpsManager.currentPosition!!.longitude()}' WHERE Targa = '${pren_future.get(i).targa_veicolo}';",{
                                                                    result->
                                                                runOnUiThread{
                                                                    cars!!.clear()
                                                                    val annotationApi = mapView?.annotations
                                                                    for (point in pointManagers) {
                                                                        annotationApi?.removeAnnotationManager(point)
                                                                        Log.e("TAG", pointManagers.size.toString())
                                                                    }
                                                                    pointManagers.clear()
                                                                    DBMSManager.querySelect(
                                                                        "SELECT * FROM Veicoli WHERE Targa = '${
                                                                            pren_future.get(
                                                                                i
                                                                            ).targa_veicolo
                                                                        }'", { result2 ->
                                                                            runOnUiThread {

                                                                                val obj =
                                                                                    result2.get(0) as JsonObject
                                                                                var datafinale =
                                                                                    pren_future.get(i).data_prenotazione_inizio.toString()
                                                                                        .replace("T", " ")
                                                                                        .split(" ")
                                                                                var datafinaledata =
                                                                                    datafinale.get(0).split("-")
                                                                                var data_f = finale.split(" ")
                                                                                var data_finale_f =
                                                                                    data_f.get(0).split("-")
                                                                                binding.chiama.visibility =
                                                                                    View.INVISIBLE
                                                                                binding.stop.visibility =
                                                                                    View.INVISIBLE
                                                                                //Todo: Mettere dialog recensione e resoconto costo corsa
                                                                                var dialog = onCreateDialog(
                                                                                    pren_future.get(i).targa_veicolo!!,
                                                                                    DBMSManager.utente?.id.toString(),
                                                                                    BookingActivity.calcoloprezzo_tot(
                                                                                        BookingActivity.calcoloPrezzo2(
                                                                                            obj["Marca"].asString,
                                                                                            obj["Modello"].asString,
                                                                                            obj["Anno_Produzione"].asString
                                                                                        ),
                                                                                        datafinaledata.get(2) + "/" + datafinaledata.get(
                                                                                            1
                                                                                        ) + "/" + datafinaledata.get(
                                                                                            0
                                                                                        ) + " " + datafinale.get(
                                                                                            1
                                                                                        ),
                                                                                        data_finale_f.get(2) + "/" + data_finale_f.get(
                                                                                            1
                                                                                        ) + "/" + data_finale_f.get(
                                                                                            0
                                                                                        ) + " " + data_f.get(1)
                                                                                    ).toString()
                                                                                )
                                                                                dialog.show()
                                                                                dialog.findViewById<TextView>(R.id.inizio).text =
                                                                                    datafinaledata.get(2) + "/" + datafinaledata.get(
                                                                                        1
                                                                                    ) + "/" + datafinaledata.get(
                                                                                        0
                                                                                    ) + " " + datafinale.get(
                                                                                        1
                                                                                    )
                                                                                dialog.findViewById<TextView>(R.id.fine).text =
                                                                                    data_finale_f.get(2) + "/" + data_finale_f.get(
                                                                                        1
                                                                                    ) + "/" + data_finale_f.get(
                                                                                        0
                                                                                    ) + " " + data_f.get(
                                                                                        1
                                                                                    )
                                                                                dialog.findViewById<TextView>(R.id.totale).text =
                                                                                    BookingActivity.calcoloprezzo_tot(
                                                                                        BookingActivity.calcoloPrezzo2(
                                                                                            obj["Marca"].asString,
                                                                                            obj["Modello"].asString,
                                                                                            obj["Anno_Produzione"].asString
                                                                                        ),
                                                                                        datafinaledata.get(2) + "/" + datafinaledata.get(
                                                                                            1
                                                                                        ) + "/" + datafinaledata.get(
                                                                                            0
                                                                                        ) + " " + datafinale.get(
                                                                                            1
                                                                                        ),
                                                                                        data_finale_f.get(2) + "/" + data_finale_f.get(
                                                                                            1
                                                                                        ) + "/" + data_finale_f.get(
                                                                                            0
                                                                                        ) + " " + data_f.get(1)
                                                                                    ).toString() + " €"


                                                                                Toast.makeText(
                                                                                    aski,
                                                                                    "HAI TERMINATO LA TUA CORSA",
                                                                                    Toast.LENGTH_SHORT
                                                                                ).show()

                                                                                pren_future.removeAt(i)
                                                                            }
                                                                        }, {
                                                                            val dialog =
                                                                                onCreateDialogFailedConn()
                                                                            dialog.show()
                                                                            dialog.findViewById<TextView>(R.id.errore).text =
                                                                                "Errore connessione al server!"
                                                                        })
                                                                }
                                                            }, {
                                                                val dialog = onCreateDialogFailedConn()
                                                                dialog.show()
                                                                dialog.findViewById<TextView>(R.id.errore).text =
                                                                    "Errore connessione al server!"
                                                            })
                                                        }
                                                    }, {
                                                        val dialog = onCreateDialogFailedConn()
                                                        dialog.show()
                                                        dialog.findViewById<TextView>(R.id.errore).text =
                                                            "Errore connessione al server!"
                                                    })

                                            } else {
                                                var dialog = onCreateDialogFailed()
                                                dialog.show()
                                                dialog.findViewById<TextView>(R.id.errore).text =
                                                    "Non sei Connesso ad Internet!"
                                            }
                                        }else{
                                            Toast.makeText(aski,"ABILITA IL GPS",Toast.LENGTH_SHORT).show()
                                        }

                                        //E' TERMINATA LA PRENOTAZIONE DA ADESSO
                                    }
                                    1 -> {

                                        //E' TERMINATA LA PRENOTAZIONE GIA' DA UN PO'
                                        binding.chiama.visibility = View.INVISIBLE
                                        binding.stop.visibility = View.INVISIBLE
                                    }
                                    -1 -> {
                                        //E' DURANTE LA PRENOTAZIONE
                                        binding.chiama.visibility = View.VISIBLE
                                        binding.stop.visibility = View.VISIBLE
                                        binding.chiama.setOnClickListener {
                                            if (ClientNetwork.isOnline(aski!!)) {
                                                DBMSManager.querySelect("SELECT u.Num_Telefono as telefono FROM Utenti u,Veicoli v WHERE u.ID_Utente = v.ID AND v.Targa = '${
                                                    pren_future.get(
                                                        i
                                                    ).targa_veicolo
                                                }'",
                                                    { result ->
                                                        runOnUiThread {
                                                            if (result.size() > 0) {
                                                                val obj =
                                                                    (result.get(0) as JsonObject)

                                                                val dialIntent =
                                                                    Intent(Intent.ACTION_DIAL)
                                                                dialIntent.data =
                                                                    Uri.parse("tel:" + obj.get("telefono").asString)
                                                                startActivity(dialIntent)
                                                            }
                                                        }
                                                    }, {
                                                        val dialog = onCreateDialogFailedConn()
                                                        dialog.show()
                                                        dialog.findViewById<TextView>(R.id.errore).text =
                                                            "Errore connessione al server!"
                                                    })
                                            } else {
                                                val dialog = onCreateDialogFailed()
                                                dialog.show()
                                                dialog.findViewById<TextView>(R.id.errore).text =
                                                    "Non sei connesso ad Internet!"
                                            }
                                        }
                                        //AL CLICK DELLO STOP
                                        binding.stop.setOnClickListener {
                                            if(LocationGpsManager.currentPosition != null) {
                                                if (ClientNetwork.isOnline(aski!!)) {
                                                    var current_update =
                                                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                                    var finale = current_update.format(current)
                                                    Log.e("PROVA:", "${finale}")
                                                    DBMSManager.queryUpdate(
                                                        "UPDATE Prenotazioni SET Data_Prenotazione_fine = '${finale}' WHERE ID_Utente = ${DBMSManager.utente?.id} AND Targa_Veicolo = '${
                                                            pren_future.get(
                                                                i
                                                            ).targa_veicolo
                                                        }' AND Data_Prenotazione_Inizio = '${
                                                            pren_future.get(
                                                                i
                                                            ).data_prenotazione_inizio!!.replace(
                                                                "T",
                                                                " "
                                                            )
                                                        }' AND Data_Prenotazione_Fine ='${
                                                            pren_future.get(
                                                                i
                                                            ).data_prenotazione_fine!!.replace(
                                                                "T",
                                                                " "
                                                            )
                                                        }';", { result ->
                                                            runOnUiThread {
                                                                DBMSManager.queryUpdate("UPDATE Veicoli SET Latitudine = '${LocationGpsManager.currentPosition!!.latitude()}', Longitudine= '${LocationGpsManager.currentPosition!!.longitude()}' WHERE Targa = '${pren_future.get(i).targa_veicolo}';",{
                                                                        result->
                                                                    runOnUiThread{
                                                                        cars!!.clear()
                                                                        val annotationApi = mapView?.annotations
                                                                        for (point in pointManagers) {
                                                                            annotationApi?.removeAnnotationManager(point)
                                                                            Log.e("TAG", pointManagers.size.toString())
                                                                        }
                                                                        pointManagers.clear()
                                                                DBMSManager.querySelect(
                                                                    "SELECT * FROM Veicoli WHERE Targa = '${
                                                                        pren_future.get(
                                                                            i
                                                                        ).targa_veicolo
                                                                    }'", { result2 ->
                                                                        runOnUiThread {
                                                                            var datafinale =
                                                                                pren_future.get(i).data_prenotazione_inizio.toString()
                                                                                    .replace(
                                                                                        "T",
                                                                                        " "
                                                                                    )
                                                                                    .split(" ")
                                                                            var datafinaledata =
                                                                                datafinale.get(0)
                                                                                    .split("-")
                                                                            var data_f =
                                                                                finale.split(" ")
                                                                            var data_finale_f =
                                                                                data_f.get(0)
                                                                                    .split("-")
                                                                            val obj =
                                                                                result2.get(0) as JsonObject
                                                                            binding.chiama.visibility =
                                                                                View.INVISIBLE
                                                                            binding.stop.visibility =
                                                                                View.INVISIBLE
                                                                            //Todo: Mettere dialog recensione e resoconto costo corsa
                                                                            var dialog =
                                                                                onCreateDialog(
                                                                                    pren_future.get(
                                                                                        i
                                                                                    ).targa_veicolo!!,
                                                                                    DBMSManager.utente?.id.toString(),
                                                                                    BookingActivity.calcoloprezzo_tot(
                                                                                        BookingActivity.calcoloPrezzo2(
                                                                                            obj["Marca"].asString,
                                                                                            obj["Modello"].asString,
                                                                                            obj["Anno_Produzione"].asString
                                                                                        ),
                                                                                        datafinaledata.get(
                                                                                            2
                                                                                        ) + "/" + datafinaledata.get(
                                                                                            1
                                                                                        ) + "/" + datafinaledata.get(
                                                                                            0
                                                                                        ) + " " + datafinale.get(
                                                                                            1
                                                                                        ),
                                                                                        data_finale_f.get(
                                                                                            2
                                                                                        ) + "/" + data_finale_f.get(
                                                                                            1
                                                                                        ) + "/" + data_finale_f.get(
                                                                                            0
                                                                                        ) + " " + data_f.get(
                                                                                            1
                                                                                        )
                                                                                    ).toString()
                                                                                )
                                                                            dialog!!.show()
                                                                            dialog.setCanceledOnTouchOutside(
                                                                                false
                                                                            )
                                                                            dialog.setCancelable(
                                                                                false
                                                                            );
                                                                            dialog.findViewById<TextView>(
                                                                                R.id.inizio
                                                                            ).text =
                                                                                datafinaledata.get(2) + "/" + datafinaledata.get(
                                                                                    1
                                                                                ) + "/" + datafinaledata.get(
                                                                                    0
                                                                                ) + " " + datafinale.get(
                                                                                    1
                                                                                )
                                                                            dialog.findViewById<TextView>(
                                                                                R.id.fine
                                                                            ).text =
                                                                                data_finale_f.get(2) + "/" + data_finale_f.get(
                                                                                    1
                                                                                ) + "/" + data_finale_f.get(
                                                                                    0
                                                                                ) + " " + data_f.get(
                                                                                    1
                                                                                )
                                                                            dialog.findViewById<TextView>(
                                                                                R.id.totale
                                                                            ).text =
                                                                                BookingActivity.calcoloprezzo_tot(
                                                                                    BookingActivity.calcoloPrezzo2(
                                                                                        obj["Marca"].asString,
                                                                                        obj["Modello"].asString,
                                                                                        obj["Anno_Produzione"].asString
                                                                                    ),
                                                                                    datafinaledata.get(
                                                                                        2
                                                                                    ) + "/" + datafinaledata.get(
                                                                                        1
                                                                                    ) + "/" + datafinaledata.get(
                                                                                        0
                                                                                    ) + " " + datafinale.get(
                                                                                        1
                                                                                    ),
                                                                                    data_finale_f.get(
                                                                                        2
                                                                                    ) + "/" + data_finale_f.get(
                                                                                        1
                                                                                    ) + "/" + data_finale_f.get(
                                                                                        0
                                                                                    ) + " " + data_f.get(
                                                                                        1
                                                                                    )
                                                                                ).toString() + " €"
                                                                            Toast.makeText(
                                                                                aski,
                                                                                "HAI TERMINATO LA TUA CORSA",
                                                                                Toast.LENGTH_SHORT
                                                                            ).show()
                                                                            pren_future.removeAt(i)
                                                                        }
                                                                    }, {
                                                                        val dialog =
                                                                            onCreateDialogFailedConn()
                                                                        dialog.show()
                                                                        dialog.findViewById<TextView>(
                                                                            R.id.errore
                                                                        ).text =
                                                                            "Errore connessione al server!"
                                                                    })
                                                                    }
                                                                }, {
                                                                    val dialog = onCreateDialogFailedConn()
                                                                    dialog.show()
                                                                    dialog.findViewById<TextView>(R.id.errore).text =
                                                                        "Errore connessione al server!"
                                                                })
                                                            }
                                                        }, {
                                                            val dialog = onCreateDialogFailedConn()
                                                            dialog.show()
                                                            dialog.findViewById<TextView>(R.id.errore).text =
                                                                "Errore connessione al server!"
                                                        })
                                                } else {
                                                    var dialog = onCreateDialogFailed()
                                                    dialog.show()
                                                    dialog.findViewById<TextView>(R.id.errore).text =
                                                        "Non sei Connesso ad Internet!"
                                                }
                                            }else{
                                                Toast.makeText(aski,"ABILITA IL GPS",Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                }
                            }
                            -1 -> {
                                //E' PRENOTAZIONE E' DISTANTE
                                binding.chiama.visibility = View.INVISIBLE
                                binding.stop.visibility = View.INVISIBLE
                            }
                        }
                    }
                }

            }
        }

        if (ClientNetwork.isOnline(this)) {
            DBMSManager.querySelect("SELECT * FROM Prenotazioni WHERE ID_Utente=${DBMSManager.utente!!.id} AND (NOW() < Data_Prenotazione_Fine);",
                { result ->
                    runOnUiThread {
                        if (result.size() > 0) {
                            for (i in 0 until result.size()) {
                                val obj = (result.get(i) as JsonObject)
                                pren_future.add(
                                    Prenotazione(
                                        obj.get("ID_Utente").asInt,
                                        obj.get("Targa_Veicolo").asString,
                                        obj.get("Data_Prenotazione_Inizio").asString,
                                        obj.get("Data_Prenotazione_Fine").asString
                                    )
                                )
                                Log.e("Oggetto:", pren_future.toString())
                            }
                        } else {
                            binding.chiama.visibility = View.INVISIBLE
                            binding.stop.visibility = View.INVISIBLE
                        }
                    }
                }, {
                    val dialog = onCreateDialogFailedConn()
                    dialog.show()
                    dialog.findViewById<TextView>(R.id.errore).text =
                        "Errore connessione al server!"
                })
        } else {
            val dialog = onCreateDialogFailedConn()
            dialog.show()
            dialog.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
        }


        //EVENTO MAPPA CHE AGGIUNGE/REFRESHA PUNTI
        mapView!!.getMapboxMap().addOnMoveListener(object : OnMoveListener {
            override fun onMove(detector: MoveGestureDetector): Boolean {
                return false
            }

            override fun onMoveBegin(detector: MoveGestureDetector) {
                binding.loading.visibility = View.VISIBLE
            }

            override fun onMoveEnd(detector: MoveGestureDetector) {
                Log.e("TAG", mapView!!.getMapboxMap().cameraState.center.toString())
                addAnnotationToMap(mapView!!.getMapboxMap().cameraState.center)
                binding.loading.visibility = View.INVISIBLE
            }
        })

        binding.layoutFilter.setOnClickListener {
            if (binding.layoutBottom.visibility == INVISIBLE) {
                binding.layoutBottom.visibility = VISIBLE
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.fragmentContainerView, filter(), "filter")
                transaction.commitNow()
            } else {
                binding.layoutBottom.visibility = INVISIBLE
            }
        }

        //BOTTONE DI CHIUSURA DEL FRAGMENT POSTO IN BASSO
        binding.layoutCloseBottom.setOnClickListener {
            if (binding.layoutBottom.visibility == VISIBLE) {
                binding.layoutBottom.visibility = INVISIBLE
            }
        }

        binding.layoutSlidingmenu.setOnClickListener {
            if (!binding.drawerLayout.isOpen) {
                binding.drawerLayout.open()
            }
        }

        //LOGICA CLICK ITEM NELLA NAVBAR
        binding.navbar.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.account -> {
                    startActivity(
                        Intent(
                            this,
                            MapProfiloActivity::class.java
                        )
                    );overridePendingTransition(R.anim.bottom_to_top, R.anim.nothing_anim)
                }
                R.id.own_car -> {
                    startActivity(
                        Intent(
                            this,
                            MapOwnCarActivityList::class.java
                        )
                    );overridePendingTransition(R.anim.bottom_to_top, R.anim.nothing_anim)
                }
                R.id.payment_method -> {
                    startActivity(
                        Intent(
                            this,
                            PaymentActivity::class.java
                        )
                    );overridePendingTransition(R.anim.bottom_to_top, R.anim.nothing_anim)
                }
                R.id.reservations -> {
                    startActivity(
                        Intent(
                            this,
                            Riepilogo::class.java
                        )
                    );overridePendingTransition(R.anim.bottom_to_top, R.anim.nothing_anim)
                }
                R.id.guide -> {
                    startActivity(
                        Intent(
                            this,
                            Guida::class.java
                        )
                    );overridePendingTransition(R.anim.bottom_to_top, R.anim.nothing_anim)
                }
                R.id.logout -> {
                    val sharedPreferences =
                        getSharedPreferences("AskiSharedPreferences", Context.MODE_PRIVATE)
                    if (sharedPreferences.contains("id_utente") && sharedPreferences.contains("password_utente")) {
                        var editor = sharedPreferences.edit()
                        editor.remove("id_utente")
                        editor.remove("password_utente")
                        editor.apply()
                        startActivity(Intent(this, Login::class.java))
                        finish()
                    }
                }
            }
            true
        }

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //BOTTONE ZOOM
        binding.layoutZoom.setOnClickListener {

            Log.e("POSIZIONE:", LocationGpsManager.currentPosition.toString())
            //CONTROLLO SE HA IL PERMESSO, SE HA UNA POSIZIONE CONOSCIUTA E SE HA IL GPS ATTIVO
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && LocationGpsManager.currentPosition != null && locationManager.isProviderEnabled(
                    LocationManager.GPS_PROVIDER
                )
            ) {
                //SE TUTTO E' OKAY ALLORA IMPOSTO LO ZOOM SULL'UTENTE
                mapView!!.getMapboxMap().loadStyleUri(
                    Style.MAPBOX_STREETS,
                    // After the style is loaded, initialize the Location component.
                    object : Style.OnStyleLoaded {
                        override fun onStyleLoaded(style: Style) {
                            addAnnotationToMap(mapView!!.getMapboxMap().cameraState.center)
                            mapView!!.location.updateSettings {
                                enabled = true
                                pulsingEnabled = true
                            }
                            Log.e(
                                "PROVA-POS:",
                                mapView!!.getMapboxMap().cameraState.center.toString()
                            )
                        }
                    }
                )
                val viewportPlugin = this.mapView!!.viewport
                Log.e("QUI2", "QUI2")
                val followPuckViewportState: FollowPuckViewportState =
                    viewportPlugin.makeFollowPuckViewportState(
                        FollowPuckViewportStateOptions.Builder()
                            .bearing(FollowPuckViewportStateBearing.Constant(0.0)).build()
                    )
                viewportPlugin.transitionTo(followPuckViewportState) { success ->
                }
            } else {
                Log.e("PROVA", "CI HAI CLICCATO")
                val initialCameraOptions1 = CameraOptions.Builder()
                    .center(Point.fromLngLat(13.352443, 38.111227))
                    .zoom(12.0)
                    .build()
                val animationOptions = MapAnimationOptions.Builder()
                    .duration(2000)
                    .build()
                mapView!!.camera.easeTo(initialCameraOptions1, animationOptions)
            }
        }
        val navigationView = findViewById<NavigationView>(R.id.navbar)
        val headerView = navigationView.getHeaderView(0)
        val nomeUtenteTextView = headerView.findViewById<TextView>(R.id.nome_utente)
        val RentedTextView = headerView.findViewById<TextView>(R.id.rented)
         credito = headerView.findViewById<TextView>(R.id.aski_credito)
        credito!!.text= "${DBMSManager.utente?.credito}€"
        RentedTextView.text = "${DBMSManager.utente?.num_pren} auto"
        nomeUtenteTextView.text = "Bentornato\n${DBMSManager.utente?.nome}"
        val giorniTextView = headerView.findViewById<TextView>(R.id.giorni)
        giorniTextView.text = "${DBMSManager.utente?.giorni} G ${DBMSManager.utente?.ore!!.toFloat().toInt()} H"
    }

    private fun addAnnotationToMap(center: Point) {
// Create an instance of the Annotation API and get the PointAnnotationManager.
        var str: String = ""
        if (cars!!.size == 0) {

            str = "''"
        } else {
            for (i in 0 until cars!!.size) {
                Log.e("TAG", cars?.get(0)!!.targa.toString())
                str += "'${cars?.get(i)?.targa}'"
                if (i < cars?.size!! - 1)
                    str += ","
            }
        }
        Log.e("TAG", center.toString())
        if (ClientNetwork.isOnline(this)) {
            DBMSManager.querySelect("SELECT *, \n" +
                    "    6371 * 2 * ASIN(\n" +
                    "        SQRT(\n" +
                    "            POW(SIN((RADIANS(${
                        center.latitude().toString()
                    }) - RADIANS(v.Latitudine)) / 2), 2) +\n" +
                    "            COS(RADIANS(${
                        center.latitude().toString()
                    })) * COS(RADIANS(v.Latitudine)) *\n" +
                    "            POW(SIN((RADIANS(${
                        center.longitude().toString()
                    }) - RADIANS(v.Longitudine)) / 2), 2)\n" +
                    "        )\n" +
                    "    ) AS distanza FROM Veicoli v WHERE ${if (brandFilter.equals("SELEZIONA")) "" else "Marca = '$brandFilter' AND "} Disponibile = 1 AND Targa NOT IN (${str}) HAVING distanza <= ${kmFilter}",
                { result ->
                    runOnUiThread {
                        if (result.size() > 0) {
                            for (i in 0 until result.size()) {
                                val obj: JsonObject = result.get(i) as JsonObject
                                cars?.add(
                                    MapCar(
                                        obj.get("Marca").asString,
                                        obj.get("Modello").asString,
                                        obj.get("Anno_Produzione").asString,
                                        obj.get("Carburante").asString,
                                        obj.get("Targa").asString,
                                        obj.get("ID").asInt,
                                        obj.get("Latitudine").asFloat,
                                        obj.get("Longitudine").asFloat
                                    )
                                )
                                Log.e("TAG", " " + cars?.get(0)!!.targa.toString())
                                val resources: Resources = this.applicationContext.resources
                                Log.e(
                                    "TAG",
                                    "brand_" + obj.get("Marca").asString.toLowerCase()
                                        .replace("-", "_")
                                )
                                val resourceId: Int = resources.getIdentifier(
                                    "brand_" + obj.get("Marca").asString.toLowerCase()
                                        .replace("-", "_"), "drawable",
                                    this.applicationContext.packageName
                                )

                                if (obj.get("ID").asInt == DBMSManager.utente!!.id) {
                                    binding.template.background =
                                        resources.getDrawable(R.drawable.wrap_unavaible)
                                } else {
                                    binding.template.background =
                                        resources.getDrawable(R.drawable.wrap_avaible)
                                }
                                Log.e("TAG", obj.toString() + " " + resourceId)

                                binding.templateImg.setImageResource(resourceId)
                                var bitmapfinale: Bitmap = binding.template.drawToBitmap()


                                bitmapFromDrawableRes(
                                    this@AskiMap,
                                    resourceId
                                )?.let {

                                    val annotationApi = mapView?.annotations
                                    val pointAnnotationManager =
                                        annotationApi?.createPointAnnotationManager()
                                    pointManagers.add(pointAnnotationManager!!)
                                    pointAnnotationManager?.apply {
                                        addClickListener(OnPointAnnotationClickListener {
                                            val initialCameraOptions1 = CameraOptions.Builder()
                                                .center(
                                                    Point.fromLngLat(
                                                        it.point.longitude(),
                                                        it.point.latitude()
                                                    )
                                                )
                                                .zoom(14.0)
                                                .build()
                                            val animationOptions = MapAnimationOptions.Builder()
                                                .duration(2000)
                                                .build()
                                            mapView!!.camera.easeTo(
                                                initialCameraOptions1,
                                                animationOptions
                                            )
                                            val mapcar: MapCar = MapCar(
                                                obj.get("Marca").asString,
                                                obj.get("Modello").asString,
                                                obj.get("Anno_Produzione").asString,
                                                obj.get("Carburante").asString,
                                                obj.get("Targa").asString,
                                                obj.get("ID").asInt,
                                                obj.get("Latitudine").asFloat,
                                                obj.get("Longitudine").asFloat
                                            )
                                            prepareViewAnnotation(it, mapcar, resourceId)
                                            Log.e("TAG", "Hai cliccato ${obj.toString()}")
                                            false
                                        })
                                    }
// Set options for the resulting symbol layer.
                                    val pointAnnotationOptions: PointAnnotationOptions =
                                        PointAnnotationOptions()
// Define a geographic coordinate.
                                            .withPoint(
                                                Point.fromLngLat(
                                                    obj.get("Longitudine").asDouble,
                                                    obj.get("Latitudine").asDouble
                                                )
                                            )
                                            // Specify the bitmap you assigned to the point annotation
// The bitmap will be added to map style automatically.

                                            .withIconImage(bitmapfinale)
// Add the resulting pointAnnotation to the map.
                                    pointAnnotationManager?.create(pointAnnotationOptions)
                                }
                            }

                        }
                    }
                },
                {
                    if (!flag) {
                        val dialog = onCreateDialogFailedConn()
                        dialog.show()
                        dialog.findViewById<TextView>(R.id.errore).text =
                            "Errore connessione al server!"
                        flag = true
                    }
                })
        } else {
            if (!flag2) {
                val dialog = onCreateDialogFailed()
                dialog.show()
                dialog.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
                flag2 = true
            }
        }
    }


    private lateinit var viewAnnotation: View

    @SuppressLint("SetTextI18n")
    private fun prepareViewAnnotation(
        point: PointAnnotation,
        car: MapCar,
        resourceId: Int,
    ) {
        viewAnnotationManager.removeAllViewAnnotations()
        viewAnnotation = viewAnnotationManager.addViewAnnotation(
            resId = R.layout.map_dialog,
            options = viewAnnotationOptions {
                geometry(point.point)
                associatedFeatureId(point.featureIdentifier)
                anchor(ViewAnnotationAnchor.BOTTOM)
                offsetY((point.iconImageBitmap?.height!! * 0.7).toInt())
            }
        )
        MapDialogBinding.bind(viewAnnotation).apply {
            if (LocationGpsManager.currentPosition != null) {
                Log.e(
                    "POSIZIONE",
                    point.point.latitude()
                        .toString() + "|" + point.point.longitude() + "|" + LocationGpsManager.getLastKnownLocation(
                        inst!!,
                        inst!!.applicationContext
                    )!!
                        .latitude() + "|" + LocationGpsManager.getLastKnownLocation(
                        inst!!,
                        inst!!.applicationContext
                    )!!
                        .longitude()
                )
                km.text = DistanceCalculator().getDistanceBetweenPointsNew(
                    point.point.latitude(),
                    point.point.longitude(),
                    LocationGpsManager.currentPosition!!
                        .latitude(),
                    LocationGpsManager.currentPosition!!
                        .longitude(),
                    "kilometers"
                ).toString() + " km"
            } else {
                km.text = DistanceCalculator().getDistanceBetweenPointsNew(
                    point.point.latitude(),
                    point.point.longitude(),
                    38.115683,
                    13.361457,
                    "kilometers"
                ).toString() + " km"


            }
            modelloText.text = car.modello
            //prenota
            prenota.setOnClickListener {
                if (car.id == DBMSManager.utente!!.id) {
                    var dialog = onCreateDialogFailed()
                    dialog.show()
                    dialog.findViewById<TextView>(R.id.errore).text =
                        "Non puoi prenotare la tua auto!"
                } else {
                    // Gets linearlayout
                    val layout: LinearLayout = binding.layoutBottom
                    val params: ViewGroup.LayoutParams = layout.layoutParams
                    layout.layoutParams = params

                    if (binding.layoutBottom.visibility == View.INVISIBLE) {
                        val bundle = Bundle()
                        bundle.putParcelable("car", car)
                        bundle.putInt("img", resourceId)
                        val informationcar = information_car()
                        informationcar.setArguments(bundle)
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.replace(R.id.fragmentContainerView, informationcar, "icar")
                        transaction.commitNow()
                        binding.layoutBottom.visibility = View.VISIBLE
                    }
                }
            }
            //PERCORSO
            percorso.setOnClickListener {
                if (LocationGpsManager.getLastKnownLocation(
                        inst!!,
                        inst!!.applicationContext
                    ) != null
                ) {
                    val uri = "http://maps.google.com/maps?f=d&hl=it&saddr=${
                        LocationGpsManager.getLastKnownLocation(
                            inst!!,
                            inst!!.applicationContext
                        )!!.latitude().toString()
                    },${
                        LocationGpsManager.getLastKnownLocation(inst!!, inst!!.applicationContext)!!
                            .longitude().toString()
                    }&daddr=${point.point.latitude().toString()},${
                        point.point.longitude().toString()
                    }"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    startActivity(Intent.createChooser(intent, "Seleziona app"))
                } else {
                    val uri = "http://maps.google.com/maps?f=d&hl=it&saddr=Palermo&daddr=${
                        point.point.latitude().toString()
                    },${point.point.longitude().toString()}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    startActivity(Intent.createChooser(intent, "Seleziona app"))
                }
            }

        }
    }


    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    override fun onResume() {
        super.onResume()
        cars!!.clear()
        val annotationApi = mapView?.annotations
        for (point in pointManagers) {
            annotationApi?.removeAnnotationManager(point)
            Log.e("TAG", pointManagers.size.toString())
        }
        pointManagers.clear()
        Log.e("TAG", "RESUME")
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    fun onCreateDialog(targa:String,id:String,saldo:String): Dialog {
        return aski?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = aski!!.layoutInflater;
            lateinit var ratingbar: RatingBar
            var view = inflater.inflate(R.layout.dialog_fine_corsa, null)
            builder.setView(view)
                // Add action buttons
                .setPositiveButton("Conferma",
                    DialogInterface.OnClickListener { dialog, id ->
                        //SE NON ESISTE FA QUESTO:
                        if(ClientNetwork.isOnline(this)){
                            ratingbar = view.findViewById<RatingBar>(R.id.ratingBar)
                            val valutazione = ratingbar.rating
                            DBMSManager.queryInsert("INSERT INTO Valutazioni VALUES('$targa',${DBMSManager.utente!!.id},NOW(),$valutazione)",{
                            runOnUiThread {
                                val sharedpreferences:SharedPreferences = getSharedPreferences("AskiSharedPreferences", Context.MODE_PRIVATE)
                                if(sharedpreferences.getString("pref_card","default").equals("default")){
                                            DBMSManager.queryUpdate("UPDATE Utenti SET Credito=Credito-$saldo WHERE ID_Utente = ${DBMSManager.utente!!.id}",{
                                                runOnUiThread{
                                                    DBMSManager.queryInsert("INSERT INTO Pagamenti VALUES (${DBMSManager.utente!!.id},'Conto Aski',$saldo,NOW())",{
                                                    runOnUiThread {
                                                        //TODO: TRONCAMENTO SOLDI

                                                        credito!!.text = (DBMSManager.utente!!.credito!!.toFloat() - saldo.toFloat()).toString()+" €"
                                                    }
                                                    },{
                                                        val dialog = onCreateDialogFailedConn()
                                                        dialog.show()
                                                        dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                                    })
                                                }
                                            },{
                                                val dialog = onCreateDialogFailedConn()
                                                dialog.show()
                                                dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                            })
                                }else{
                                    DBMSManager.queryInsert("INSERT INTO Pagamenti VALUES (${DBMSManager.utente!!.id},'${sharedpreferences.getString("pref_card","default")}',$saldo,NOW())",{
                                        runOnUiThread {

                                        }
                                    },{
                                        val dialog = onCreateDialogFailedConn()
                                        dialog.show()
                                        dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                    })
                                }
                                DBMSManager.queryUpdate("UPDATE Utenti ut SET Credito = Credito + ($saldo/100)*40 WHERE ut.ID_Utente IN (SELECT u.ID_Utente FROM Utenti u, Veicoli v WHERE u.ID_Utente = v.ID AND v.Targa = '$targa')",{
                                    runOnUiThread {

                                        DBMSManager.querySelect("SELECT n.Token FROM Utenti u, Notifiche n, Veicoli v WHERE u.ID_Utente = n.ID AND u.ID_Utente = v.ID AND v.Targa = '$targa';",{
                                            runOnUiThread {
                                                            val obj = (it[0] as JsonObject)["Token"].asString //token
                                                            var accredito = (saldo.toFloat()/100)*40
                                                            Log.e("ACCREDITO",accredito.toString())
                                                            val fcmService = FCMService.create()
                                                            val token = obj
                                                            val title = "Aski"
                                                            val message =
                                                                "Ti sono stati accreditati ${accredito} € a seguito della prenotazione del tuo veicolo targato ${targa}."
                                                            val notification =
                                                                FCMNotification(
                                                                    to = token,
                                                                    notification = FCMNotificationData(
                                                                        title = title,
                                                                        body = message
                                                                    ),
                                                                    data = emptyMap() // Dati aggiuntivi opzionali
                                                                )
                                                            NotificationReceiver.setNotification(
                                                                "AskiMap",
                                                                "Prenotazione",
                                                                R.drawable.car_icon
                                                            )

                                                            val call = fcmService.sendNotification(
                                                                notification
                                                            )
                                                            call.enqueue(object :
                                                                retrofit2.Callback<ResponseBody> {
                                                                override fun onResponse(
                                                                    call: Call<ResponseBody>,
                                                                    response: retrofit2.Response<ResponseBody>
                                                                ) {
                                                                    if (response.isSuccessful) {

                                                                    } else {
                                                                        // Errore nell'invio della notifica
                                                                        Log.e(
                                                                            "CALL",
                                                                            call.toString() + " " + response.message()
                                                                        )
                                                                    }
                                                                }

                                                                override fun onFailure(
                                                                    call: Call<ResponseBody>,
                                                                    t: Throwable
                                                                ) {
                                                                    Log.e(
                                                                        "ERRORE",
                                                                        call.toString() + " " + t.toString()
                                                                    )
                                                                    // Gestisci l'errore di invio della notifica
                                                                }
                                                            })

                                            }},{

                                            val dialog = onCreateDialogFailedConn()
                                            dialog.show()
                                            dialog.findViewById<TextView>(R.id.errore).text =
                                                "Errore connessione al server!"
                                        })
                                        //INIZIO


                                        //FINE
                                    }
                                },{
                                    val dialog2 = onCreateDialogFailedConn()
                                    dialog2.show()
                                    dialog2.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                })
                            }
                        },{
                            val dialog = onCreateDialogFailedConn()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                        })}else{
                            val dialog = onCreateDialogFailed()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text = "Non sei connesso ad Internet!"
                        }
                        dialog.cancel()
                    })

            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla")
    }

    fun onCreateDialogFailedConn(): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                    this.finishAffinity()
                    System.exit(0)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }

    fun onCreateDialogFailed(): Dialog {
        return (this?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }

    fun deleteAnnotations() {
        cars!!.clear()
        val annotationApi = mapView?.annotations
        for (point in pointManagers) {
            annotationApi?.removeAnnotationManager(point)
            Log.e("TAG", pointManagers.size.toString())
        }
        pointManagers.clear()
    }

     fun startAlarm(calendar: Calendar,context:Context ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}