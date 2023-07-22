package it.am.gpsmodule.entity

class Card(var codice:String,var cvc:String,var anno:String) {
     fun equals(other: Card): Boolean {
        if((codice.toString()+cvc.toString()+anno.toString()).equals((other.codice.toString()+other.cvc.toString()+other.anno.toString())))
            return true
        return false
    }
}