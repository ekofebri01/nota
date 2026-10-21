package com.eco.nota.models

import kotlinx.serialization.Serializable

@Serializable
data class Nota(
    val id: String,
    val referenceCode: String = "",
    val customerName: String,
    val phoneNumber: String = "",
    val address: String = "",
    val customerLabel: String = "",
    val date: String,
    val total: String,
    val status: String,
    val subtotal: Long = 0L,
    val taxPercent: Double = 0.0,
    val discount: Long = 0L,
    val shippingCost: Long = 0L,
    val deposit: Long = 0L,
    val paymentMethod: String = "Tunai",
    val notes: String = "",
    val items: List<NotaItemInput> = emptyList()
)

@Serializable
data class NotaItemInput(
    val name: String = "",
    val quantity: String = "1",
    val unit: String = "",
    val price: String = ""
)

data class CustomerSuggestion(
    val name: String,
    val phoneNumber: String = "",
    val address: String = "",
    val label: String = ""
)

@Serializable
data class BusinessProfile(
    val name: String = "",
    val address: String = "",
    val notes: String = "",
    val logoUri: String? = null,
    val bankName: String = "",
    val bankAccount: String = "",
    val bankHolder: String = "",
    val qrisUri: String? = null,
    val eWalletInfo: String = ""
)

@Serializable
data class InvoiceConfig(
    val template: String = "Modern",
    val paperSize: String = "58mm",
    val showLogo: Boolean = true,
    val showFooter: Boolean = true,
    val showSignature: Boolean = false
)

fun normalizePaymentMethod(method: String?): String {
    val trimmed = method?.trim()
    return if (trimmed.isNullOrEmpty()) "Tunai" else trimmed
}

fun normalizeNotaStatus(status: String?): String {
    val trimmed = status?.trim()
    if (trimmed.isNullOrEmpty()) return "Lunas"
    return when (trimmed.lowercase()) {
        "lunas" -> "Lunas"
        "tagihan", "belum lunas", "piutang" -> "Tagihan"
        else -> trimmed
    }
}
