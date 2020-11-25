package com.example.trilock.data.register_login.classes

class Event {

    lateinit var firstName: String
    lateinit var timeStamp: String
    var locked: Boolean = false

    constructor(){

    }

    constructor(firstName: String, timeStamp: String, locked: Boolean) {
        this.firstName = firstName
        this.timeStamp = timeStamp
        this.locked = locked
    }


}