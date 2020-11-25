package com.example.trilock.data.register_login.classes

class Lock {

    var locked: Boolean = false
    lateinit var owners: ArrayList<String>
    var isUnlocked: Boolean = false

    constructor(){

    }

    constructor(firstName: String, timeStamp: String, isUnlocked: Boolean) {

    }


}