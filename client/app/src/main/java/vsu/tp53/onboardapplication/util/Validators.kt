package vsu.tp53.onboardapplication.util

import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

class Validators {
    companion object {
        fun checkEmail(email: String): Boolean {
            val regex: Pattern =
                Pattern.compile("^[a-z0-9.]+@([a-z0-9]+-?[a-z0-9]+)+\\.([a-z0-9]+-?[a-z0-9]+)+\$")
            return regex.matcher(email).find()
        }

        fun checkVk(vk: String): Boolean {
            val regex = Pattern.compile("^vk.com/.+\$")
            return regex.matcher(vk).find()
        }

        fun checkTg(tg: String): Boolean {
            val regex = Pattern.compile("^t.me/.+\$")
            return regex.matcher(tg).find()
        }

        fun checkDateAndTime(date: String): Boolean {
            val df = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            return try {
                df.parse(date)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}