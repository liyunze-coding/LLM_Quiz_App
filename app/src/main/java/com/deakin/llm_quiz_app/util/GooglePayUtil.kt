package com.deakin.llm_quiz_app.util

import org.json.JSONArray
import org.json.JSONObject

object GooglePayUtil {
    fun getBaseRequest(): JSONObject = JSONObject().apply {
        put("apiVersion", 2)
        put("apiVersionMinor", 0)
    }

    fun getIsReadyToPayRequest(): JSONObject {
        val baseRequest = getBaseRequest()

        val allowedCardNetworks = JSONArray(listOf("VISA", "MASTERCARD"))
        val allowedAuthMethods = JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS"))

        val cardPaymentMethod = JSONObject().apply {
            put("type", "CARD")
            put("parameters", JSONObject().apply {
                put("allowedAuthMethods", allowedAuthMethods)
                put("allowedCardNetworks", allowedCardNetworks)
            })
        }

        baseRequest.put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
        return baseRequest
    }

    fun getPaymentDataRequest(): JSONObject {
        val baseRequest = getBaseRequest()

        val allowedCardNetworks = JSONArray(listOf("VISA", "MASTERCARD"))
        val allowedAuthMethods = JSONArray(listOf("PAN_ONLY", "CRYPTOGRAM_3DS"))

        val cardPaymentMethod = JSONObject().apply {
            put("type", "CARD")
            put("tokenizationSpecification", JSONObject().apply {
                put("type", "PAYMENT_GATEWAY")
                put("parameters", JSONObject().apply {
                    put("gateway", "example") // replace with your gateway
                    put("gatewayMerchantId", "exampleMerchantId")
                })
            })
            put("parameters", JSONObject().apply {
                put("allowedAuthMethods", allowedAuthMethods)
                put("allowedCardNetworks", allowedCardNetworks)
                put("billingAddressRequired", true)
                put("billingAddressParameters", JSONObject().apply {
                    put("format", "FULL")
                })
            })
        }

        baseRequest.put("allowedPaymentMethods", JSONArray().put(cardPaymentMethod))
        baseRequest.put("transactionInfo", JSONObject().apply {
            put("totalPrice", "10.00")
            put("totalPriceStatus", "FINAL")
            put("currencyCode", "USD")
        })

        baseRequest.put("merchantInfo", JSONObject().apply {
            put("merchantName", "Example Merchant")
        })

        return baseRequest
    }
}
