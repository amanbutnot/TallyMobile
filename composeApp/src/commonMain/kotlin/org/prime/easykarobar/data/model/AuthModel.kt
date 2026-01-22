package org.prime.easykarobar.data.model

import kotlinx.serialization.Serializable
import org.prime.easykarobar.data.utils.SharedPrefs


@Serializable
data class LoginRequest(
    val Username: String,
    val Password: String,
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
    val FilterAGRP: String,
    val ConfigAGRP: String,
    val FilterAccounts: String,
    val ConfigAccounts: String,
    val FilterIGRP: String,
    val ConfigIGRP: String,
    val FilterItems: String,
    val ConfigItems: String,
    val FilterMobile: String,
    val FilterGodown:String?=null,
    val ConfigGodown: String?=null,
    val FilterParam1: String?=null,
    val ConfigParam1: String?=null,
    val FilterBroker: String?=null,
    val ConfigBroker: String?=null,
    val FilterAmount: String?=null,
    val D1: Int,
    val D2: Int,
    val D3: Int,
    val D4: Int,
    val D5: Int,
    val D6: Int,
    val D7: Int,
    val D8: Int,
    val D9: Int,
    val D10: Int,
    val D11: Int,
    val D12: Int,
    val D13: Int,
    val D14: Int,
    val D15: Int,
    val D16: Int,
    val D17: Int,
    val D18: Int,
    val D19: Int,
    val D20: Int,
    val D21: Int,
    val D22: Int,
    val D23: Int,
    val D24: Int,
    val D25: Int,
    val D26: Int,
    val D27: Int,
    val D28: Int,
    val D29: Int,
    val D30: Int,
    val D31: Int,
    val D32: Int,
    val D33: Int,
    val D34: Int,
    val D35: Int,
    val D36: Int,
    val D37: Int,
    val D38: Int,
    val D39: Int,
    val D40: Int,
    val D41: Int,
    val D42: Int,
    val D43: Int,
    val D44: Int,
    val D45: Int,
    val D46: Int,
    val D47: Int,
    val D48: Int,
    val D49: Int,
    val D50: Int
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