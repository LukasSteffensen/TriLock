package com.example.trilock.data.register_login.classes

class User{

    var firstName: String? = null
    var lastName: String? = null
    var phone: String? = null
    var email: String? = null
    var isOwner: Boolean? = null

    constructor(firstName: String?, isOwner: Boolean?) {
        this.firstName = firstName
        this.isOwner = isOwner
    }





}