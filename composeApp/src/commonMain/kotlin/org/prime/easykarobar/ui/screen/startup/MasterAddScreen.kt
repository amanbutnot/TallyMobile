package org.prime.easykarobar.ui.screen.startup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.prime.easykarobar.business.viewmodel.masters.AccountViewModel
import org.prime.easykarobar.data.expect.DatabaseHolder
import org.prime.easykarobar.data.utils.SharedPrefs
import org.prime.easykarobar.ui.screen.easymart.DispatchInfoScreen
import org.prime.easykarobar.ui.screen.home.Dashboard
import org.prime.easykarobar.ui.screen.transactions.sale.makeNegativeConditional
import kotlin.math.absoluteValue

data class MasterAddScreen(val number: String) : Screen {

    @Composable
    override fun Content() {

        val db = DatabaseHolder.instance
        val viewmodel: AccountViewModel = viewModel { AccountViewModel() }
        val state by viewmodel.listState
        val nav = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            if (SharedPrefs.IsEasyMart.get()) {
                SharedPrefs.IsEasyMart.saveLastCheckTime(kotlin.time.Clock.System.now().toEpochMilliseconds())
            }
            viewmodel.listAccount {
                state.data?.data?.forEach { dataState ->
                    db.ledgerMasterQueries.insertLedger(
                        code = dataState.ledger_guid?.toLong(),
                        name = dataState.name?.trim(),
                        alias = dataState.alias?.trim(),
                        groupName = dataState.parentGroupName,
                        groupCode = dataState.parentGroupGuid?.toDoubleOrNull() ?: 0.0,
                        opBal = dataState.openingBalance?.toDoubleOrNull() ?: 0.0,
                        address1 = dataState.addressLine1,
                        address2 = dataState.addressLine2,
                        address3 = dataState.addressLine3,
                        address4 = dataState.addressLine4,
                        country = dataState.country,
                        state = dataState.state,
                        gstin = dataState.gstNo,
                        email = dataState.email,
                        mobileNo = dataState.mobileNo,
                        alterId = 0,
                        guid = dataState.ledger_guid.toString(),
                        panNo = dataState.itPan
                    )
                    state.data?.data_items?.forEach { data ->

                        db.productsQueries.insertItem(
                            name = data.name,
                            alias = data.alias,
                            printName = data.printName,
                            parentGroup = data.parentGroup,
                            parentGroupGuid = data.parentGroupGuid.toDoubleOrNull()
                                ?: 0.0,
                            mainUnit = data.mainUnit,
                            mainUnitGuid = data.mainUnitGuid.toDoubleOrNull()
                                ?: 0.0,
                            opQty = data.opQty.toDoubleOrNull() ?: 0.0,
                            opAmount = data.opAmount.toDoubleOrNull() ?: 0.0,
                            taxCategoryName = data.taxCategoryName,
                            taxCategoryGuid = data.taxCategoryGuid.toDoubleOrNull()
                                ?: 0.0,
                            salePrice = data.salePrice.toDoubleOrNull() ?: 0.0,
                            purchPrice = data.purchPrice?.toDoubleOrNull()
                                ?: 0.0,
                            mrp = data.mrp.toDoubleOrNull() ?: 0.0,
                            minSalePrice = data.minSalePrice.toDoubleOrNull()
                                ?: 0.0,
                            selfValPrice = data.selfValPrice.toDoubleOrNull()
                                ?: 0.0,
                            saleDiscount = data.saleDiscount.toDoubleOrNull()
                                ?: 0.0,
                            purchDiscount = data.purchDiscount.toDoubleOrNull()
                                ?: 0.0,
                            product_guid = data.id.toString(),
                            altUnit = data.altUnit,
                            conFactor = data.conFactor.toDoubleOrNull() ?: 1.0,
                            conType = when (data.conType) {
                                "Main / Alt" -> 1.0
                                "Alt / Main" -> 2.0
                                else -> data.conType.toDoubleOrNull() ?: 1.0
                            },
                            salesPriceAlt = data.salePriceAlt?.toDoubleOrNull() ?: 0.0,
                            purcPriceAlt = data.purchPriceAlt?.toDoubleOrNull() ?: 0.0
                        )
                    }
                    state.data?.data_bills?.forEach { data ->
                        db.transaction {


                            db.voucherBillAllocationsQueries.insertBillAllocation(
                                guid = data.uniqueID.toString(),

                                vch_guid = data.uniqueID.toString(),

                                vchtype = data.vchType,

                                date = data.date,

                                duedate = data.dueDate,

                                billnumber = data.billNumber,

                                // order matters more than you think later
                                srno = data.SrNo?.toLong(),

                                // you're already storing cm1 in data → don’t ignore it
                                cm1 = data.cm1.toString(),

                                cm2 = data.cm2.toString(), // still hardcoded, your call

                                cm3 = "",

                                billid = data.billId?.toDoubleOrNull(),

                                //TODO: logic for sale me + purc me minus
                                d1 = when (data.vchType?.toIntOrNull()?:3) {
                                    9 -> {
                                        makeNegativeConditional( data.d1 ?:0.0)
                                    }

                                    3 -> {
                                        data.d1
                                    }

                                    2 -> {
                                        data.d1
                                    }

                                    10 -> {
                                        makeNegativeConditional( data.d1 ?:0.0)
                                    }

                                    14 -> {
                                        makeNegativeConditional( data.d1 ?:0.0)
                                    }
                                    19 -> {
                                        makeNegativeConditional( data.d1 ?:0.0)
                                    }
                                    16 -> {
                                        data.d1?.absoluteValue
                                    }

                                    else -> {
                                        data.d1?.absoluteValue
                                    }
                                },

                                d2 = null,


                                e2 = null
                            )
                        }

                    }
                }
                if (SharedPrefs.IsEasyMart.get()) {
                    try {
                        val db = DatabaseHolder.instance
                        val result = db.ledgerPricingQueries.selectChangePrice(number).executeAsOneOrNull()
                        println("verify otp result $number $result")
                        if (result != null) {
                            SharedPrefs.ChangePrice.save(result.L6 ?: 0.0)
                        } else {
                            val savedGst = SharedPrefs.DispatchInfo.getGst()
                            if (!savedGst.isNullOrBlank()) {
                                SharedPrefs.ChangePrice.save(102.0)
                            } else {
                                SharedPrefs.ChangePrice.save(101.0)
                            }
                        }

                        val isGuest = number == "6969696969" || SharedPrefs.RegisteredNumber.get() == "6969696969"
                        val exists = db.ledgerPricingQueries.existsByMobile(number).executeAsOne() > 0
                        println("Exists by mobile: $exists, Dispatch saved: ${SharedPrefs.DispatchInfo.isSaved()}")
                        if (!isGuest && !exists && !SharedPrefs.DispatchInfo.isSaved()) {
                            nav.replaceAll(DispatchInfoScreen(number))
                            return@listAccount
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    nav.replaceAll(Dashboard)
                } else {
                    nav.replaceAll(Dashboard)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {

            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {

                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    CircularProgressIndicator(
                        strokeWidth = 4.dp
                    )

                    Text(
                        text = "Importing Masters",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = "Please wait while we sync your ledger data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    state.data?.let {
                        Text(
                            text = "Records processed: ${it.data?.size?.plus(it.data_items?.size ?: 0)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
