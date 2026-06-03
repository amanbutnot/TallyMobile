package org.prime.easykarobar.data.model

import kotlinx.serialization.Serializable
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.shared.globalShared.perms


@Serializable
data class LoginRequest(
    val Username: String,
    val Password: String,
    val DeviceId: String,
    val CompanyID: Int? = null
)

@Serializable
data class CompanyList(
    val CompanyDetails: List<Company>
)

@Serializable
data class Company(
    val CompanyID: Int,
    val CompanyName: String
)

@Serializable
data class LoginResponse(
    val ID: Int,
    val FirstName: String,
    val LastName: String,
    val Mobile: String,
    val Email: String,
    val Gender: String,
    val AddressLine1: String,
    val AddressLine2: String,
    val City: String,
    val State: String,
    val Country: String,
    val Pincode: String,
    val CreatedAt: String,
    val UpdatedAt: String,
    val C1: String,
    val C2: String,
    val C3: String,
    val C4: String,
    val C5: String,
    val C6: String,
    val C7: String,
    val C8: String,
    val C9: String,
    val C10: String,
    val token: String,
    val token_expiry: String,
    val role: String,
    val distributor: Distributor? = null,
    val permissions: Permissions? = null
)

@Serializable
data class Distributor(
    val UserName: String,
    val ledger_name: String,
    val ledger_GUID: String
)

@Serializable
data class Permissions(
    val FilterAGRP: String? = null,
    val ConfigAGRP: String? = null,
    val FilterAccounts: String? = null,
    val ConfigAccounts: String? = null,
    val FilterIGRP: String? = null,
    val ConfigIGRP: String? = null,
    val FilterItems: String? = null,
    val ConfigItems: String? = null,
    val FilterMobile: String? = null,
    val FilterGodown: String? = null,
    val ConfigGodown: String? = null,
    val FilterParam1: String? = null,
    val ConfigParam1: String? = null,
    val FilterBroker: String? = null,
    val ConfigBroker: String? = null,
    val FilterAmount: String? = null,
    val FilterQty: String? = null,
    val D1: Int? = null,
    val D2: Int? = null,
    val D3: Int? = null,
    val D4: Int? = null,
    val D5: Int? = null,
    val D6: Int? = null,
    val D7: Int? = null,
    val D8: Int? = null,
    val D9: Int? = null,
    val D10: Int? = null,
    val D11: Int? = null,
    val D12: Int? = null,
    val D13: Int? = null,
    val D14: Int? = null,
    val D15: Int? = null,
    val D16: Int? = null,
    val D17: Int? = null,
    val D18: Int? = null,
    val D19: Int? = null,
    val D20: Int? = null,
    val D21: Int? = null,
    val D22: Int? = null,
    val D23: Int? = null,
    val D24: Int? = null,
    val D25: Int? = null,
    val D26: Int? = null,
    val D27: Int? = null,
    val D28: Int? = null,
    val D29: Int? = null,
    val D30: Int? = null,
    val D31: Int? = null,
    val D32: Int? = null,
    val D33: Int? = null,
    val D34: Int? = null,
    val D35: Int? = null,
    val D36: Int? = null,
    val D37: Int? = null,
    val D38: Int? = null,
    val D39: Int? = null,
    val D40: Int? = null,
    val D41: Int? = null,
    val D42: Int? = null,
    val D43: Int? = null,
    val D44: Int? = null,
    val D45: Int? = null,
    val D46: Int? = null,
    val D47: Int? = null,
    val D48: Int? = null,
    val D49: Int? = null,
    val D50: Int? = null,
    val D51: Int? = null,
    val D52: Int? = null,
    val ED1: Int? = null,
    val ED2: Int? = null,
    val ED3: Int? = null,
    val ED4: Int? = null,
    val ED5: Int? = null,
    val ED6: Int? = null,
    val ED7: Int? = null,
    val ED8: Int? = null,
    val ED9: Int? = null,
    val ED10: Int? = null,
    val ED11: Int? = null,
    val ED12: Int? = null,
    val ED13: Int? = null,
    val ED14: Int? = null,
    val ED15: Int? = null,
    val ED16: Int? = null,
    val ED17: Int? = null,
    val ED18: Int? = null,
    val ED19: Int? = null,
    val ED20: Int? = null,
) {
    fun isEnabled(flag: String): Boolean {
        return when (flag) {
            "D1" -> D1 == 0
            "D2" -> D2 == 0
            "D3" -> D3 == 0
            "D4" -> D4 == 0
            "D5" -> D5 == 0
            "D6" -> D6 == 0
            "D7" -> D7 == 0
            "D8" -> D8 == 0
            "D9" -> D9 == 0
            "D10" -> D10 == 0
            "D11" -> D11 == 0
            "D12" -> D12 == 0
            "D13" -> D13 == 0
            "D14" -> D14 == 0
            "D15" -> D15 == 0
            "D16" -> D16 == 0
            "D17" -> D17 == 0
            "D18" -> D18 == 0
            "D19" -> D19 == 0
            "D20" -> D20 == 0
            "D21" -> D21 == 0
            "D22" -> D22 == 0
            "D23" -> D23 == 0
            "D24" -> D24 == 0
            "D25" -> D25 == 0
            "D26" -> D26 == 0
            "D27" -> D27 == 0
            "D28" -> D28 == 0
            "D29" -> D29 == 0
            "D30" -> D30 == 0
            "D31" -> D31 == 0
            "D32" -> D32 == 0
            "D33" -> D33 == 0
            "D34" -> D34 == 0
            "D35" -> D35 == 0
            "D36" -> D36 == 0
            "D37" -> D37 == 0
            "D38" -> D38 == 0
            "D39" -> D39 == 0
            "D40" -> D40 == 0
            "D41" -> D41 == 0
            "D42" -> D42 == 0
            "D43" -> D43 == 0
            "D44" -> D44 == 0
            "D45" -> D45 == 0
            "D46" -> D46 == 0
            "D47" -> D47 == 0
            "D48" -> D48 == 0
            "D49" -> D49 == 0
            "D50" -> D50 == 0
            "D51" -> D51 == 0
            "D52" -> D52 == 0
            "ED1" -> ED1 == 0
            "ED2" -> ED2 == 0
            "ED3" -> ED3 == 0
            "ED4" -> ED4 == 0
            "ED5" -> ED5 == 0
            "ED6" -> ED6 == 0
            "ED7" -> ED7 == 0
            "ED8" -> ED8 == 0
            "ED9" -> ED9 == 0
            "ED10" -> ED10 == 0
            "ED11" -> ED11 == 0
            "ED12" -> ED12 == 0
            "ED13" -> ED13 == 0
            "ED14" -> ED14 == 0
            "ED15" -> ED15 == 0
            "ED16" -> ED16 == 0
            "ED17" -> ED17 == 0
            "ED18" -> ED18 == 0
            "ED19" -> ED19 == 0
            "ED20" -> ED20 == 0
            else -> false
        }
    }
}

fun salesmanPermission(flag: String, accessDeniedBlock: () -> Unit, successBlock: () -> Unit) {
    println("Sending value of D: $flag")
    val permissions = SharedPrefs.Permissions.get()
    println("Filter Mobile value: ${permissions?.FilterMobile}")

    if (permissions?.FilterMobile == "N" || permissions == null) {
        successBlock()
    } else {
        if (permissions.isEnabled(flag) && permissions.FilterMobile == "Y") {
            successBlock()
        } else {
            accessDeniedBlock()
        }
    }
}

fun hasSalesmanPermission(flag: String): Boolean {
    val permissions = SharedPrefs.Permissions.get()

    return if (permissions?.FilterMobile == "N" || permissions == null) {
        true
    } else {
        if (permissions.isEnabled(flag) && permissions.FilterMobile == "Y") {
            true
        } else {
            false
        }
    }
}

fun receivableDashboardPerms(): Boolean {
    return perms?.D32 == 0
}

fun payableDashboardPerms(): Boolean {
    return perms?.D33 == 0
}

fun saleDashboardPerms(): Boolean {
    return perms?.D34 == 0
}

fun receiptsDashboardPerms(): Boolean {
    return perms?.D35 == 0
}

@Serializable
data class RegisterRequest(
    val FirstName: String,
    val LastName: String,
    val Mobile: String,
    val Email: String,
    val Password: String,
    val Gender: String,
    val City: String,
    val C1: String,
    val C2: String,
    val C9: String,
    val C10: String
)

@Serializable
data class RegisterResponse(
    val status: String,
    val message: String,
    val UserID: Int
)

@Serializable
data class ForgotResponse(
    val ID: Int,
    val FirstName: String
)

@Serializable
data class ResetRequest(
    val MobileNo: String,
    val NewPassword: String
)

@Serializable
data class WhatsAppSendResponse(
    val success: Boolean,
    val message: String,
    val reportId: Long,
    val messageId: String,
    val status: String,
    val results: List<ResultItem>,
    val subscription: Subscription
)

@Serializable
data class ResultItem(
    val messageId: String,
    val status: String
)

@Serializable
data class Subscription(
    val sms_count: Int,
    val expires_at: String
)