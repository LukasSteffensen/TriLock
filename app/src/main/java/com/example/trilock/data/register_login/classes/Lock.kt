package com.example.trilock.data.register_login.classes

class Lock {

    var isLocked: Boolean = false
    var owners: ArrayList<String> = ArrayList()
    var guests: ArrayList<String> = ArrayList()

    constructor(){

    }

    constructor(firstName: String, timeStamp: String, isUnlocked: Boolean) {

    }


}