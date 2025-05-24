package com.deakin.llm_quiz_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.deakin.llm_quiz_app.util.PaymentsUtil
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentsClient
import kotlinx.coroutines.tasks.await

class CheckoutViewModel(application: Application) : AndroidViewModel(application) {
    private val paymentsClient: PaymentsClient = PaymentsUtil.createPaymentsClient(application)

    private suspend fun fetchCanUseGooglePay(): Boolean {
        val request = IsReadyToPayRequest.fromJson(PaymentsUtil.isReadyToPayRequest().toString())
        return paymentsClient.isReadyToPay(request).await()
    }
}