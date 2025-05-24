package com.deakin.llm_quiz_app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.deakin.llm_quiz_app.util.PaymentsUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import org.json.JSONObject

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {
    // A client for interacting with the Google Pay API.
    private val paymentsClient: PaymentsClient

    // LiveData with the result of whether the user can pay using Google Pay
    private val _canUseGooglePay = MutableLiveData<Boolean?>()
    val canUseGooglePay: LiveData<Boolean?> = _canUseGooglePay

    init {
        paymentsClient = PaymentsUtil.createPaymentsClient(application)

        fetchCanUseGooglePay()
    }

    /**
     * Determine the user's ability to pay with a payment method supported by your app and display
     * a Google Pay payment button.
     */
    private fun fetchCanUseGooglePay() {
        val isReadyToPayJson: JSONObject? = PaymentsUtil.isReadyToPayRequest
        if (isReadyToPayJson == null) {
            _canUseGooglePay.setValue(false)
            return
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        val request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString())
        val task = paymentsClient.isReadyToPay(request)
        task.addOnCompleteListener(
            OnCompleteListener { completedTask: Task<Boolean?>? ->
                if (completedTask!!.isSuccessful()) {
                    _canUseGooglePay.setValue(completedTask.getResult())
                } else {
                    Log.w("isReadyToPay failed", completedTask.getException())
                    _canUseGooglePay.setValue(false)
                }
            })
    }

    /**
     * Creates a Task that starts the payment process with the transaction details included.
     *
     * @param priceCents the price to show on the payment sheet.
     * @return a Task with the payment information.
     * )
     */
    fun getLoadPaymentDataTask(priceCents: Long): Task<PaymentData?>? {
        val paymentDataRequestJson: JSONObject? = PaymentsUtil.getPaymentDataRequest(priceCents)
        if (paymentDataRequestJson == null) {
            return null
        }

        val request =
            PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        return paymentsClient.loadPaymentData(request)
    }
}