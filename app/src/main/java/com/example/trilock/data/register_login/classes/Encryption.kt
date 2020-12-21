import android.util.Log
import java.math.BigInteger
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object Encryption {
    fun hexStringToByteArray(hexInputString: String): ByteArray {
        val bts = ByteArray(hexInputString.length / 2)
        for (i in bts.indices) {
            bts[i] = hexInputString.substring(2 * i, 2 * i + 2).toInt(16).toByte()
        }
        return bts
    }

    fun byteArrayToString(byteArray: ByteArray): String {
        val str = StringBuilder()
        for (i in byteArray.indices) {
            str.append(byteArray[i].toChar())
        }
        return str.toString()
    }

    fun byteArrayToHexString(arg: ByteArray?): String {
        val l = arg!!.size * 2
        return String.format("%0" + l + "x", BigInteger(1, arg))
    }

    fun encrypt(key1: ByteArray?, key2: ByteArray?, value: ByteArray?): ByteArray? {
        try {
            val iv = IvParameterSpec(key2)
            val skeySpec = SecretKeySpec(key1, "AES")
            val cipher = Cipher.getInstance("AES/CBC/NOPADDING")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
            return cipher.doFinal(value)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun decrypt(key1: ByteArray?, key2: ByteArray?, encrypted: ByteArray?): ByteArray? {
        try {
            val iv = IvParameterSpec(key2)
            val skeySpec = SecretKeySpec(key1, "AES")
            val cipher = Cipher.getInstance("AES/CBC/NOPADDING")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            return cipher.doFinal(encrypted)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun toHex(arg: String): String {
        val l = arg.length * 2
        return String.format("%0" + l + "x", BigInteger(1, arg.toByteArray()))
    }

    fun HexStringToString(arg: String): String {
        val output = StringBuilder()
        var i = 0
        while (i < arg.length) {
            val str = arg.substring(i, i + 2)
            output.append(str.toInt(16).toChar())
            i += 2
        }
        return output.toString()
    }

    @JvmStatic
    fun main(key: String) {
        // source: http://www.inconteam.com/software-development/41-encryption/55-aes-test-vectors#aes-cbc-128
        val message = "6bc1bee22e409f96e93d7e117393172a" // 16 byte = 128 bit key
        //String message = toHex("Hello00000000000");
        val key1 = key
//        val key1 = "2b7e151628aed2a6abf7158809cf4f3c"
        val iv = "000102030405060708090A0B0C0D0E0F"
        val match = "7649abac8119b246cee98e9b12e9197d"
        Log.i("message", message)
        Log.i("key", key1)
        Log.i("iv", iv)
        Log.i("match", match)
//        print("message (hex):         ")
//        println(message)
//        print("key (hex):             ")
//        println(key1)
//        print("iv (hex):              ")
//        println(iv)
//        print("match (hex):           ")
//        println(match)
//        println()
        val enc_message_ba = encrypt(hexStringToByteArray(key1),
            hexStringToByteArray(iv),
            hexStringToByteArray(message))
        Log.i("Encrypted", byteArrayToHexString(enc_message_ba))
//        print("Encrypted (hex):       ")
//        println(byteArrayToHexString(enc_message_ba))
//        println()
        val dec_message_ba =
            decrypt(hexStringToByteArray(key1), hexStringToByteArray(iv), enc_message_ba)
        Log.i("Decrypted", byteArrayToHexString(dec_message_ba))
//        print("Decrypted (hex):       ")
//        println(byteArrayToHexString(dec_message_ba))
    }
}