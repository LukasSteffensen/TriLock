package com.example.trilock.data.register_login.classes

class Event {

    var firstName: String? = null
    var timeStamp: String? = null
    var isLocked: Boolean = false

    constructor(){

    }

    constructor(firstName: String, timeStamp: String, locked: Boolean) {
        this.firstName = firstName
        this.timeStamp = timeStamp
        this.isLocked = locked
    }


}