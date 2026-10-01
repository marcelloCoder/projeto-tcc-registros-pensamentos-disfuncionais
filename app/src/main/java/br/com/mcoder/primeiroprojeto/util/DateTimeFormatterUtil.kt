package br.com.mcoder.primeiroprojeto.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormatterUtil {
    private val locale = Locale.forLanguageTag("pt-BR")

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy", locale).format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", locale).format(Date(timestamp))
    }

    fun formatFullDateTime(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yyyy, HH:mm", locale).format(Date(timestamp))
    }
}
