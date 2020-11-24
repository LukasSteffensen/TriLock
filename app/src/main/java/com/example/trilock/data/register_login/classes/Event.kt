package com.example.trilock.data.register_login.classes

class Event {

    lateinit var firstName: String
    lateinit var timeStamp: String
    var isUnlocked: Boolean = false

    constructor(){

    }

    constructor(firstName: String, timeStamp: String, isUnlocked: Boolean) {
        this.firstName = firstName
        this.timeStamp = timeStamp
        this.isUnlocked = isUnlocked
    }


}