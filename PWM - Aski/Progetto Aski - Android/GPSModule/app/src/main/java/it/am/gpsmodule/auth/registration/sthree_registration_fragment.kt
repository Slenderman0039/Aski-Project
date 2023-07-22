package it.am.gpsmodule.auth.registration

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentResultListener
import com.google.gson.JsonObject
import it.am.gpsmodule.map.AskiMap
import it.am.gpsmodule.R
import it.am.gpsmodule.auth.Registration
import it.am.gpsmodule.databinding.FragmentSthreeRegistrationBinding
import it.am.gpsmodule.entity.Utente
import it.am.gpsmodule.utils.ClientNetwork
import it.am.gpsmodule.utils.DBMSManager
import it.am.gpsmodule.utils.FormValidator
import it.am.gpsmodule.utils.OnSwipeTouchListener


class sthree_registration_fragment : Fragment(){
    private var _binding: FragmentSthreeRegistrationBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSthreeRegistrationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(Registration.utente?.telefono != null){
            binding.phoneRegistration.setText(Registration.utente!!.telefono)
            binding.emailRegistration.setText(Registration.utente!!.email)
            binding.passwordRegistration.setText(Registration.utente!!.password)
        }




        Registration.root!!.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeRight() {
                super.onSwipeRight()
                _back()
            }
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                if(FormValidator.validateFormPhone(binding.phoneRegistration) && FormValidator.validateFormEmail(binding.emailRegistration) && FormValidator.validateFormPassword(binding.passwordRegistration)){
                    _complete()
                }


            }
        })

    }
    private fun _back() {
        val supportFragmentManager: FragmentManager = getParentFragmentManager()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.right_to_left,R.anim.right_to_left,R.anim.right_to_left,R.anim.right_to_left)

        transaction.replace(R.id.fragmentContainerView, stwo_registration_fragment(),"STWO")
        transaction.commitNow()

        Registration.step2.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps) }
        Registration.step3.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps) }

    }
    private fun _complete() {

        lateinit var name:String
        lateinit var surname:String
        lateinit var d_b:String
        lateinit var address:String
        lateinit var cap:String
        lateinit var city:String
        var phone:String = binding.phoneRegistration.text.toString()
        var email:String = binding.emailRegistration.text.toString()
        var password:String = binding.passwordRegistration.text.toString()


        parentFragmentManager.setFragmentResultListener("step1",this, FragmentResultListener {
                requestKey, result ->
           name = result.getString("name")!!
            surname = result.getString("surname")!!
            d_b = result.getString("d_b")!!

        })
        parentFragmentManager.setFragmentResultListener("step2",this, FragmentResultListener {
                requestKey, result ->
            address = result.getString("address")!!
            cap = result.getString("cap")!!
            city = result.getString("city")!!

        })

        Registration.utente?.telefono = binding.phoneRegistration.text.toString()
        Registration.utente?.email = binding.emailRegistration.text.toString()
        Registration.utente?.password = binding.passwordRegistration.text.toString()

            Registration.step3.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps_confirmed) }
       var dialog =  onCreateDialog()

        dialog.show()
        dialog.findViewById<TextView>(R.id.name).text = name
        dialog.findViewById<TextView>(R.id.surname).text = surname
        dialog.findViewById<TextView>(R.id.d_b).text = d_b
        dialog.findViewById<TextView>(R.id.address).text = address
        dialog.findViewById<TextView>(R.id.cap).text = cap
        dialog.findViewById<TextView>(R.id.city).text = city
        dialog.findViewById<TextView>(R.id.phone).text = phone
        dialog.findViewById<TextView>(R.id.mail).text = email
    }
    fun onCreateDialogFailed(): Dialog {
        return (activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Torna Indietro") { dialog, id ->
                    dialog.cancel()
                    val supportFragmentManager: FragmentManager = getParentFragmentManager()
                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.setCustomAnimations(
                        R.anim.right_to_left,
                        R.anim.right_to_left,
                        R.anim.right_to_left,
                        R.anim.right_to_left
                    )

                    transaction.replace(
                        R.id.fragmentContainerView,
                        sone_registration_fragment(),
                        "SONE"
                    )
                    transaction.commitNow()

                    Registration.step1.background =
                        context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_steps) }
                    Registration.step2.background =
                        context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_steps) }
                    Registration.step3.background =
                        context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_steps) }
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
     fun onCreateDialog(): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            builder.setView(inflater.inflate(R.layout.dialog_confirm_registration, null))

                // Add action buttons
                .setPositiveButton("Conferma",
                    DialogInterface.OnClickListener { dialog, id ->
                        //SE NON ESISTE FA QUESTO:
                        if(ClientNetwork.isOnline(Registration.inst!!.applicationContext)){
                        DBMSManager.querySelect("SELECT ID_Utente FROM Utenti WHERE Email='${Registration.utente?.email}'",{
                                result ->
                            requireActivity().runOnUiThread{
                                if(result.size() > 0){
                                    var dialog = onCreateDialogFailed()
                                    dialog?.show()
                                    Log.e("TAG","l'utente è già registrato")
                                }else{
                                    val dati:ArrayList<String> =    Registration.utente?.data_nascita?.split("/") as ArrayList<String>
                                    val data = dati.get(2) + "-" + dati.get(1) + "-" + dati.get(0)
                                    val str:String = "INSERT INTO Utenti VALUES (NULL,'${Registration.utente?.nome}','${Registration.utente?.cognome}','${Registration.utente?.indirizzo}',${Registration.utente?.cap},'${Registration.utente?.citta}','${Registration.utente?.email}','${Registration.utente?.telefono}','${Registration.utente?.password}','${data}',DEFAULT)"

                                    DBMSManager.queryInsert(str,{
                                        result ->
                                        Log.e("TAG",result.toString())
                                    },{
                                        val dialog = onCreateDialogFailedConn()
                                        dialog.show()
                                        dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                    })
                                    DBMSManager.querySelect("SELECT * FROM Utenti WHERE Email='${Registration.utente?.email}'",{
                                        result ->
                                        val obj = (result.get(0) as JsonObject)


                                        DBMSManager.utente = Utente(obj.get("ID_Utente").asInt,obj.get("Nome").asString,obj.get("Cognome").asString,obj.get("Data_Nascita").asString,obj.get("Indirizzo").asString,obj.get("CAP").asInt,obj.get("Città").asString,obj.get("Num_Telefono").asString,obj.get("Email").asString,obj.get("Password").asString,obj.get("Credito").asString,"0","0","0","")
                                        val sharedPreferences =  Registration.inst!!.getSharedPreferences("AskiSharedPreferences", Context.MODE_PRIVATE)
                                        if(!sharedPreferences.contains("id_utente") && !sharedPreferences.contains("password_utente")){
                                            var editor = sharedPreferences.edit()
                                            editor.putInt("id_utente",obj.get("ID_Utente").asInt) //DATO CHE E' ENTRATO NELL'APP IMPOSTIAMO IL PARAMETRO A FALSE
                                            editor.putString("password_utente",obj.get("Password").asString)
                                            editor.commit()
                                        }
                                    },{
                                        val dialog = onCreateDialogFailedConn()
                                        dialog.show()
                                        dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                                    })
                                    var dialog = onCreateDialogSuccess()
                                    dialog.show()
                                    Handler().postDelayed({ //faccio aspettare 3secondi e poi avvio l'intent

                                        dialog.dismiss()
                                        startActivity(Intent(Registration.inst, AskiMap::class.java))
                                        Registration.inst?.finish()
                                    }, 3000)
                                    Log.e("TAG","l'utente non è ancora registrato già registrato")
                                }
                            }
                        },{
                            val dialog = onCreateDialogFailedConn()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text = "Errore connessione al server!"
                        })} else{
                            var dialog = onCreateDialogFailed()
                            dialog.show()
                            dialog.findViewById<TextView>(R.id.errore).text = "Non sei Connesso ad Internet!"
                        }
                    })
                .setNegativeButton("Annulla",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        val supportFragmentManager: FragmentManager = getParentFragmentManager()
                        val transaction = supportFragmentManager.beginTransaction()
                        transaction.setCustomAnimations(R.anim.right_to_left,R.anim.right_to_left,R.anim.right_to_left,R.anim.right_to_left)

                        transaction.replace(R.id.fragmentContainerView, sone_registration_fragment(),"SONE")
                        transaction.commitNow()

                        Registration.step1.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps)}
                            Registration.step2.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps)}
                                Registration.step3.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps)}
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla")
    }
    fun onCreateDialogSuccess(): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater;

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_success_registration, null))


            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
    fun onCreateDialogFailedConn(): Dialog {
        return (activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                    this.requireActivity().finishAffinity()
                    System.exit(0)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }
}