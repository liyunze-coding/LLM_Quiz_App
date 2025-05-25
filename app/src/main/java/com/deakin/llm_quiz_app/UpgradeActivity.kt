package com.deakin.llm_quiz_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.deakin.llm_quiz_app.data.DatabaseHelper
import com.deakin.llm_quiz_app.databinding.ActivityUpgradeBinding
import com.deakin.llm_quiz_app.viewmodel.CheckoutViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale

class UpgradeActivity : AppCompatActivity() {
    companion object {
        private const val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    }
    private var layoutBinding: ActivityUpgradeBinding? = null
    private var googlePayButton: View? = null
    private var googlePayButton2: View? = null
    private var model: CheckoutViewModel? = null
    private var tierPurchase: Int = 0
    private var userId = 0
    private var db = DatabaseHelper(this, null)
    private var cancelIntermediateButton: Button? = null
    private var cancelAdvancedButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upgrade)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
        initializeUi()

        userId = intent.getIntExtra("userId", -1)
        cancelIntermediateButton = findViewById<Button>(R.id.cancelIntermediateButton)
        cancelAdvancedButton = findViewById<Button>(R.id.cancelAdvancedButton)

        model = ViewModelProvider(this).get(CheckoutViewModel::class.java)
        model!!.canUseGooglePay.observe(
            this,
            { available: Boolean -> this.setGooglePayAvailable(available) } as (Boolean?) -> Unit)

        // BACK
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            val profileIntent = Intent(this, ProfileActivity::class.java)
            profileIntent.putExtra("userId", userId)
            startActivity(profileIntent)
        }

        cancelIntermediateButton!!.setOnClickListener {
            db.setTier(userId, 0)
            val profileIntent = Intent(this, ProfileActivity::class.java)
            profileIntent.putExtra("userId", userId)
            startActivity(profileIntent)
        }

        cancelAdvancedButton!!.setOnClickListener {
            db.setTier(userId, 0)
            val profileIntent = Intent(this, ProfileActivity::class.java)
            profileIntent.putExtra("userId", userId)
            startActivity(profileIntent)
        }
    }

    private fun initializeUi() {
        // Use view binding to access the UI elements
        layoutBinding = ActivityUpgradeBinding.inflate(getLayoutInflater())
        setContentView(layoutBinding!!.getRoot())

        googlePayButton = findViewById<View>(R.id.googlePayButton)
        googlePayButton2 = findViewById<View>(R.id.googlePayButton2)
        if (googlePayButton != null) {
            googlePayButton!!.setOnClickListener(this::requestPayment)
            googlePayButton2!!.setOnClickListener(this::requestPayment2)
        } else {
            // Handle the case where the button is not found, e.g., log an error
            Log.e("InitializeUi", "Google Pay button with ID R.id.googlePayButton not found.");
        }
    }

    /**
     * If isReadyToPay returned `true`, show the button and hide the "checking" text.
     * Otherwise, notify the user that Google Pay is not available. Please adjust to fit in with
     * your current user flow. You are not required to explicitly let the user know if isReadyToPay
     * returns `false`.
     *
     * @param available isReadyToPay API response.
     */
    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            val userTier = db.getUser(userId).tier
            if (userTier < 1) {
                googlePayButton!!.visibility = View.VISIBLE
            }
            if (userTier < 2 ) {
                googlePayButton2!!.visibility = View.VISIBLE
            }

            if (userTier == 1) {
                cancelIntermediateButton!!.visibility = View.VISIBLE
            } else if (userTier == 2) {
                cancelAdvancedButton!!.visibility = View.VISIBLE
            }
        } else {
            Toast.makeText(this, R.string.googlepay_status_unavailable, Toast.LENGTH_LONG).show()
        }
    }

    fun requestPayment(view: View?) {
        // Disables the button to prevent multiple clicks.
        googlePayButton!!.setClickable(false)
        googlePayButton2!!.setClickable(false)
        tierPurchase = 1

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val totalPriceCents: Long = 500
        val task: Task<PaymentData?> = model!!.getLoadPaymentDataTask(totalPriceCents)!!

        // Shows the payment sheet and forwards the result to the onActivityResult method.
        AutoResolveHelper.resolveTask<PaymentData?>(
            task,
            this,
            LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    fun requestPayment2(view: View?) {
        // Disables the button to prevent multiple clicks.
        googlePayButton!!.setClickable(false)
        googlePayButton2!!.setClickable(false)
        tierPurchase = 2

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        val totalPriceCents: Long = 1000
        val task: Task<PaymentData?> = model!!.getLoadPaymentDataTask(totalPriceCents)!!

        // Shows the payment sheet and forwards the result to the onActivityResult method.
        AutoResolveHelper.resolveTask<PaymentData?>(
            task,
            this,
            LOAD_PAYMENT_DATA_REQUEST_CODE
        )
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode  Result code returned by the Google Pay API.
     * @param data        Intent from the Google Pay API containing payment or error data.
     * @see [Getting a result
     * from an Activity](https://developer.android.com/training/basics/intents/result)
     */
    @Suppress("deprecation") // Suppressing deprecation until `registerForActivityResult` can be used with the Google Pay API.
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        val paymentData = PaymentData.getFromIntent(data!!)
                        handlePaymentSuccess(paymentData)
                    }

                    RESULT_CANCELED -> {}
                    AutoResolveHelper.RESULT_ERROR -> {
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                        handleError(status)
                    }
                }

                // Re-enables the Google Pay payment button.
                googlePayButton!!.setClickable(true)
                googlePayButton2!!.setClickable(true)
            }
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     * @see [PaymentData](https://developers.google.com/pay/api/android/reference/
    object.PaymentData)
     */
    private fun handlePaymentSuccess(paymentData: PaymentData?) {
        val paymentInfo = paymentData!!.toJson()

        try {
            val paymentMethodData = JSONObject(paymentInfo).getJSONObject("paymentMethodData")

            // update tier
            val result = db.setTier(userId, tierPurchase)

            Log.d("result", "$result")

            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            val tokenizationData = paymentMethodData.getJSONObject("tokenizationData")
            val token = tokenizationData.getString("token")
            val info = paymentMethodData.getJSONObject("info")
            val billingName = info.getJSONObject("billingAddress").getString("name")
            Toast.makeText(
                this, getString(R.string.payments_show_name, billingName),
                Toast.LENGTH_LONG
            ).show()

            val profileIntent = Intent(this, ProfileActivity::class.java)
            profileIntent.putExtra("userId", userId)


            // Logging token string.
            Log.d("Google Pay token: ", token)

            startActivity(profileIntent)
        } catch (e: JSONException) {
            throw RuntimeException("The selected garment cannot be parsed from the list of elements")
        }
    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred. Normally,
     * only logging is required.
     *
     * @param status will hold the value of any constant from CommonStatusCode or one of the
     * WalletConstants.ERROR_CODE_* constants.
     * @see [Wallet Constants Library](https://developers.google.com/android/reference/com/google/android/gms/wallet/
    WalletConstants.constant-summary)
     */
    private fun handleError(status: Status?) {
        var errorString = "Unknown error."
        if (status != null) {
            val statusCode = status.getStatusCode()
            errorString = String.format(Locale.getDefault(), "Error code: %d", statusCode)
        }

        Log.e("loadPaymentData failed", errorString)
    }
}