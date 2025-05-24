package com.deakin.llm_quiz_app.util

import android.content.Context
import com.deakin.llm_quiz_app.Constants
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.Wallet.WalletOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 *
 * Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
object PaymentsUtil {
    val CENTS_IN_A_UNIT: BigDecimal = BigDecimal(100)

    @get:Throws(JSONException::class)
    private val baseRequest: JSONObject
        /**
         * Create a Google Pay API base request object with properties used in all requests.
         *
         * @return Google Pay API base request object.
         * @throws JSONException if the object is malformed.
         */
        get() = JSONObject().put("apiVersion", 2).put("apiVersionMinor", 0)

    /**
     * Creates an instance of [PaymentsClient] for use in an [Context] using the
     * environment and theme set in [Constants].
     *
     * @param context is the caller's context.
     */
    fun createPaymentsClient(context: Context): PaymentsClient {
        val walletOptions =
            WalletOptions.Builder().setEnvironment(Constants.PAYMENTS_ENVIRONMENT).build()
        return Wallet.getPaymentsClient(context, walletOptions)
    }

    @get:Throws(JSONException::class)
    private val gatewayTokenizationSpecification: JSONObject
        /**
         * Gateway Integration: Identify your gateway and your app's gateway merchant identifier.
         *
         *
         * The Google Pay API response will return an encrypted payment method capable of being charged
         * by a supported gateway after payer authorization.
         *
         *
         * TODO: Check with your gateway on the parameters to pass and modify them in Constants.java.
         *
         * @return Payment data tokenization for the CARD payment method.
         * @throws JSONException if the object is malformed.
         * @see [PaymentMethodTokenizationSpecification](https://developers.google.com/pay/api/android/reference/object.PaymentMethodTokenizationSpecification)
         */
        get() = object : JSONObject() {
            init {
                put("type", "PAYMENT_GATEWAY")
                put("parameters", object : JSONObject() {
                    init {
                        put("gateway", "example")
                        put("gatewayMerchantId", "exampleGatewayMerchantId")
                    }
                })
            }
        }

    @get:Throws(JSONException::class, RuntimeException::class)
    private val directTokenizationSpecification: JSONObject
        /**
         * `DIRECT` Integration: Decrypt a response directly on your servers. This configuration has
         * additional data security requirements from Google and additional PCI DSS compliance complexity.
         *
         *
         * Please refer to the documentation for more information about `DIRECT` integration. The
         * type of integration you use depends on your payment processor.
         *
         * @return Payment data tokenization for the CARD payment method.
         * @throws JSONException if the object is malformed.
         * @see [PaymentMethodTokenizationSpecification](https://developers.google.com/pay/api/android/reference/object.PaymentMethodTokenizationSpecification)
         */
        get() {
            val tokenizationSpecification = JSONObject()

            tokenizationSpecification.put("type", "DIRECT")
            val parameters: JSONObject = JSONObject(Constants.DIRECT_TOKENIZATION_PARAMETERS)
            tokenizationSpecification.put("parameters", parameters)

            return tokenizationSpecification
        }

    private val allowedCardNetworks: JSONArray
        /**
         * Card networks supported by your app and your gateway.
         *
         *
         * TODO: Confirm card networks supported by your app and gateway & update in Constants.java.
         *
         * @return Allowed card networks
         * @see [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
         */
        get() = JSONArray(Constants.SUPPORTED_NETWORKS)

    private val allowedCardAuthMethods: JSONArray
        /**
         * Card authentication methods supported by your app and your gateway.
         *
         *
         * TODO: Confirm your processor supports Android device tokens on your supported card networks
         * and make updates in Constants.java.
         *
         * @return Allowed card authentication methods.
         * @see [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
         */
        get() = JSONArray(Constants.SUPPORTED_METHODS)

    @get:Throws(JSONException::class)
    private val baseCardPaymentMethod: JSONObject
        /**
         * Describe your app's support for the CARD payment method.
         *
         *
         * The provided properties are applicable to both an IsReadyToPayRequest and a
         * PaymentDataRequest.
         *
         * @return A CARD PaymentMethod object describing accepted cards.
         * @throws JSONException if the object is malformed.
         * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
         */
        get() {
            val cardPaymentMethod = JSONObject()
            cardPaymentMethod.put("type", "CARD")

            val parameters = JSONObject()
            parameters.put("allowedAuthMethods", allowedCardAuthMethods)
            parameters.put("allowedCardNetworks", allowedCardNetworks)
            // Optionally, you can add billing address/phone number associated with a CARD payment method.
            parameters.put("billingAddressRequired", true)

            val billingAddressParameters = JSONObject()
            billingAddressParameters.put("format", "FULL")

            parameters.put("billingAddressParameters", billingAddressParameters)

            cardPaymentMethod.put("parameters", parameters)

            return cardPaymentMethod
        }

    @get:Throws(JSONException::class)
    private val cardPaymentMethod: JSONObject
        /**
         * Describe the expected returned payment data for the CARD payment method
         *
         * @return A CARD PaymentMethod describing accepted cards and optional fields.
         * @throws JSONException if the object is malformed.
         * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
         */
        get() {
            val cardPaymentMethod: JSONObject = baseCardPaymentMethod
            cardPaymentMethod.put(
                "tokenizationSpecification",
                gatewayTokenizationSpecification
            )

            return cardPaymentMethod
        }

    val isReadyToPayRequest: JSONObject?
        /**
         * An object describing accepted forms of payment by your app, used to determine a viewer's
         * readiness to pay.
         *
         * @return API version and payment methods supported by the app.
         * @see [IsReadyToPayRequest](https://developers.google.com/pay/api/android/reference/object.IsReadyToPayRequest)
         */
        get() {
            try {
                val isReadyToPayRequest: JSONObject = baseRequest
                isReadyToPayRequest.put(
                    "allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod)
                )

                return isReadyToPayRequest
            } catch (e: JSONException) {
                return null
            }
        }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status.
     *
     * @return information about the requested payment.
     * @throws JSONException if the object is malformed.
     * @see [TransactionInfo](https://developers.google.com/pay/api/android/reference/object.TransactionInfo)
     */
    @Throws(JSONException::class)
    private fun getTransactionInfo(price: String?): JSONObject {
        val transactionInfo = JSONObject()
        transactionInfo.put("totalPrice", price)
        transactionInfo.put("totalPriceStatus", "FINAL")
        transactionInfo.put("countryCode", Constants.COUNTRY_CODE)
        transactionInfo.put("currencyCode", Constants.CURRENCY_CODE)
        transactionInfo.put("checkoutOption", "COMPLETE_IMMEDIATE_PURCHASE")

        return transactionInfo
    }

    @get:Throws(JSONException::class)
    private val merchantInfo: JSONObject
        /**
         * Information about the merchant requesting payment information
         *
         * @return Information about the merchant.
         * @throws JSONException if the object is malformed.
         * @see [MerchantInfo](https://developers.google.com/pay/api/android/reference/object.MerchantInfo)
         */
        get() = JSONObject().put("merchantName", "Example Merchant")

    /**
     * An object describing information requested in a Google Pay payment sheet
     *
     * @return Payment data expected by your app.
     * @see [PaymentDataRequest](https://developers.google.com/pay/api/android/reference/object.PaymentDataRequest)
     */
    fun getPaymentDataRequest(priceCents: Long): JSONObject? {
        val price = centsToString(priceCents)

        try {
            val paymentDataRequest: JSONObject = baseRequest
            paymentDataRequest.put(
                "allowedPaymentMethods", JSONArray().put(cardPaymentMethod)
            )
            paymentDataRequest.put("transactionInfo", getTransactionInfo(price))
            paymentDataRequest.put("merchantInfo", merchantInfo)

            /* An optional shipping address requirement is a top-level property of the PaymentDataRequest
      JSON object. */
            paymentDataRequest.put("shippingAddressRequired", true)

            val shippingAddressParameters = JSONObject()
            shippingAddressParameters.put("phoneNumberRequired", false)

            val allowedCountryCodes: JSONArray = JSONArray(Constants.SHIPPING_SUPPORTED_COUNTRIES)

            shippingAddressParameters.put("allowedCountryCodes", allowedCountryCodes)
            paymentDataRequest.put("shippingAddressParameters", shippingAddressParameters)
            return paymentDataRequest
        } catch (e: JSONException) {
            return null
        }
    }

    /**
     * Converts cents to a string format accepted by [PaymentsUtil.getPaymentDataRequest].
     *
     * @param cents value of the price in cents.
     */
    fun centsToString(cents: Long): String? {
        return BigDecimal(cents)
            .divide(CENTS_IN_A_UNIT, RoundingMode.HALF_EVEN)
            .setScale(2, RoundingMode.HALF_EVEN)
            .toString()
    }
}