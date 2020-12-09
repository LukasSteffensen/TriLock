package com.example.trilock.data.register_login
import java.math.BigInteger
import java.util.*

object DiffieHellman {

    fun privateKey(p: BigInteger): BigInteger {
        var key = p
        while (key < BigInteger.ONE || key >= p) {
            key = BigInteger(p.bitLength(), Random())
        }
        return key
    }

    fun publicKey(p: BigInteger, g: BigInteger, privKey: BigInteger): BigInteger {
        return g.modPow(privKey, p)
    }

    fun secret(p: BigInteger, publicKey: BigInteger, privateKey: BigInteger): BigInteger {
        return publicKey.modPow(privateKey, p)
    }
}