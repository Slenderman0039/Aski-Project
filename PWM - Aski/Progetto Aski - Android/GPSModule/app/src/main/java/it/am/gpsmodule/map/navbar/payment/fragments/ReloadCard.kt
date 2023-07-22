package it.am.gpsmodule.map.navbar.payment.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import it.am.gpsmodule.R
import it.am.gpsmodule.databinding.FragmentReloadCardBinding
import it.am.gpsmodule.map.navbar.payment.PaymentActivity
import it.am.gpsmodule.utils.FormValidator


class ReloadCard : Fragment(),View.OnClickListener {

    private var _binding: FragmentReloadCardBinding? = null
    private val binding get() = _binding!!
    private var selected_taglio:Int = -1
    private var selected_card:Int = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReloadCardBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.carta.setOnClickListener {
        var cards = ArrayList<String>()

            if(PaymentActivity.complete_card1 != null){
                cards.add(PaymentActivity.complete_card1!!)
            }
            if(PaymentActivity.complete_card2 != null){
                cards.add(PaymentActivity.complete_card2!!)
            }
            var popup = showMenu(it,R.menu.cards_menu,cards)
            popup.setOnMenuItemClickListener {
                selected_card = it.itemId
                binding.carta.setText("**** "+it.toString().takeLast(4))
                true
            }
            popup.show()
        }

        binding.layout5.setOnClickListener(this)
        binding.layout10.setOnClickListener(this)
        binding.layout20.setOnClickListener(this)
        binding.layout50.setOnClickListener(this)
        binding.layout100.setOnClickListener(this)


        binding.paga.setOnClickListener {
            if(selected_taglio != -1 && selected_card != -1 && !binding.carta.text!!.equals("CARTA") && !binding.cvv.text!!.equals("CVV")){

                val imm: InputMethodManager = PaymentActivity.inst!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm.isActive){imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)}
                if(FormValidator.validateFormCVV(binding.cvv)){
                Log.e("CARTA",PaymentActivity.cards.get(selected_card).cvc+" | "+binding.cvv.text.toString())
                    if(PaymentActivity.cards.get(selected_card).cvc.toString().equals(binding.cvv.text.toString())){

                        val bundle = Bundle()
                        bundle.putDouble("taglio", selected_taglio.toDouble())
                        val supportFragmentManager: FragmentManager = getParentFragmentManager()

                        val transaction = supportFragmentManager.beginTransaction()
                        val success = SuccessReload()
                        success.setArguments(bundle)

                        transaction.replace(R.id.fragmentContainerView, success,"SUCCESS")
                        transaction.commitNow()

                    }else{
                        Toast.makeText(PaymentActivity.inst,"CVV NON VALIDO",Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                PaymentActivity.layoutb!!.visibility = View.INVISIBLE
                var dialog = onCreateDialogFailed()
                dialog.show()
                val imm: InputMethodManager = PaymentActivity.inst!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm.isActive){imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)}
                dialog.findViewById<TextView>(R.id.errore).text="Errore transazione non valida!"

            }
        }

    }




    @SuppressLint("RestrictedApi")
    private fun showMenu(v: View, @MenuRes menuRes: Int, cards: ArrayList<String>): PopupMenu {
        val popup = PopupMenu(PaymentActivity.inst!!, v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        if (popup.menu is MenuBuilder) {
            val menuBuilder = popup.menu as MenuBuilder
            menuBuilder.setOptionalIconsVisible(true)
            Log.e("CARD SIZE",cards.size.toString())
            var i = 0
            if(cards.size > 0){
                for(card in cards){
                    menuBuilder.add(i,i,i,"**** "+card.toString().takeLast(4))
                    i++
                }
            }
            for (item in menuBuilder.visibleItems) {
                val iconMarginPx =
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics)
                        .toInt()
                if (item.icon != null) {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        item.icon = InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx,0)
                    } else {
                        item.icon =
                            @SuppressLint("RestrictedApi")
                            object : InsetDrawable(item.icon, iconMarginPx, 0, iconMarginPx, 0) {
                                override fun getIntrinsicWidth(): Int {
                                    return intrinsicHeight + iconMarginPx + iconMarginPx
                                }
                            }
                    }
                }
            }
        }
        return popup
    }

    override fun onClick(p0: View?) {
        var layouts: ArrayList<LinearLayout> = ArrayList<LinearLayout>()
        layouts.add(binding.layout5)
        layouts.add(binding.layout10)
        layouts.add(binding.layout20)
        layouts.add(binding.layout50)
        layouts.add(binding.layout100)
        when(p0!!.id){
            -1 -> {
                for(layout in layouts){ layout.background = context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_steps) };}}
            R.id.layout5 ->{
                    selected_taglio = 0
                    for(layout in layouts){
                        layout.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps)}
                    }
                    layouts.get(0).background = context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_steps_confirmed) };
                        }
            R.id.layout10->{
                selected_taglio = 10
                for(layout in layouts){
                    layout.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps)}
                }
                layouts.get(1).background = context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_steps_confirmed) };

            }
            R.id.layout20->{
                selected_taglio =20
                for(layout in layouts){
                    layout.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps)}
                }
                layouts.get(2).background = context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_steps_confirmed) };

            }
            R.id.layout50->{
                selected_taglio = 50
                for(layout in layouts){
                    layout.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps)}
                }
                layouts.get(3).background = context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_steps_confirmed) };

            }
            R.id.layout100->{
                selected_taglio = 100
                for(layout in layouts){
                    layout.background = context?.let { ContextCompat.getDrawable(it,R.drawable.rounded_steps)}
                }
                layouts.get(4).background = context?.let { ContextCompat.getDrawable(it, R.drawable.rounded_steps_confirmed) };

            }
        }
    }

    fun onCreateDialogFailed(): Dialog {
        return (activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_failed_registration, null))
                .setPositiveButton("Chiudi") { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity non può essere nulla"))
    }



}