package com.deakin.llm_quiz_app.util

import android.content.Context
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import org.json.JSONArray
import org.json.JSONObject
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONException

object PaymentsUtil {
    private val baseRequest = JSONObject()
        .put("apiVersion", 2)
        .put("apiVersionMinor", 0)

    private fun gatewayTokenizationSpecification(): JSONObject {
        return JSONObject().apply {
            put("type", "PAYMENT_GATEWAY")
            put("parameters", JSONObject(mapOf(
                "gateway" to "stripe",
                "stripe:version" to "2018-10-31",
                "gatewayMerchantId" to "pk_test_51NSdqNCK4oNdUTWnGrmxvA3hILiADJvaHcxCB8zAABmSblgNQcFegnTHq0ZYVfQLJGnHkHP481PApMwhG6djJegd008D1bgw1p")))
        }
    }

    private val allowedCardNetworks = JSONArray(listOf(
        "AMEX",
        "DISCOVER",
        "INTERAC",
        "JCB",
        "MASTERCARD",
        "VISA"))
    private val allowedCardAuthMethods = JSONArray(listOf(
        "PAN_ONLY",
        "CRYPTOGRAM_3DS"))

    private fun baseCardPaymentMethod(): JSONObject =
        JSONObject()
            .put("type", "CARD")
            .put("parameters", JSONObject()
                .put("allowedAuthMethods", allowedCardAuthMethods)
                .put("allowedCardNetworks", allowedCardNetworks)
                .put("billingAddressRequired", true)
                .put("billingAddressParameters", JSONObject()
                    .put("format", "FULL")
                )
            )
    private val cardPaymentMethod: JSONObject = baseCardPaymentMethod()
        .put("tokenizationSpecification", gatewayTokenizationSpecification())

    val allowedPaymentMethods: JSONArray = JSONArray().put(cardPaymentMethod)

    fun createPaymentsClient(context: Context): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }

    fun isReadyToPayRequest(): JSONObject? =
        try {
            baseRequest
                .put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
        } catch (e: JSONException) {
            null
        }
}