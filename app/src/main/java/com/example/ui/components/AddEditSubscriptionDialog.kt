package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.CurrencyCode
import com.example.data.model.SubscriptionEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FinanceBorder
import com.example.ui.theme.FinanceCard
import com.example.ui.theme.FinanceCardElevated
import com.example.ui.theme.FinanceSurface
import com.example.ui.theme.KElectricGreen
import com.example.ui.theme.LescoOrange
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddEditSubscriptionDialog(
    initialItem: SubscriptionEntity? = null,
    isUtilityDefault: Boolean = false,
    currentCurrency: CurrencyCode,
    onSave: (
        id: String?,
        name: String,
        category: String,
        provider: String,
        isUtility: Boolean,
        utilityType: String?,
        consumerNumber: String?,
        amount: Double,
        currency: String,
        billingCycle: String,
        nextDueDate: String,
        supportEmail: String,
        notes: String?,
        units: Int?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var isUtility by remember { mutableStateOf(initialItem?.isUtility ?: isUtilityDefault) }
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: if (isUtility) "Electricity & Power" else "Streaming") }
    var provider by remember { mutableStateOf(initialItem?.provider ?: if (isUtility) "LESCO" else "NETFLIX") }
    var utilityType by remember { mutableStateOf(initialItem?.utilityType ?: if (isUtility) "LESCO" else null) }
    var consumerNumber by remember { mutableStateOf(initialItem?.consumerNumber ?: "") }
    var amountText by remember { mutableStateOf(initialItem?.amount?.toString() ?: "25.00") }
    var currency by remember { mutableStateOf(initialItem?.currency ?: currentCurrency.name) }
    var billingCycle by remember { mutableStateOf(initialItem?.billingCycle ?: "Monthly") }
    var nextDueDate by remember { mutableStateOf(initialItem?.nextDueDate ?: "2026-11-30") }
    var supportEmail by remember { mutableStateOf(initialItem?.supportEmail ?: "support@service.com") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }
    var unitsText by remember { mutableStateOf(initialItem?.unitsConsumed?.toString() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(FinanceSurface)
                .border(1.dp, FinanceBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("add_edit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialItem != null) "Edit Item" else if (isUtility) "Add Utility Bill" else "Add Subscription",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick Presets Selector
                Text(
                    text = "QUICK PRESETS (PAKISTANI & GLOBAL SET)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Utilities Presets Row (LESCO, K-Electric, Sui Gas)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // LESCO Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(LescoOrange.copy(alpha = 0.15f))
                            .border(1.dp, LescoOrange.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = true
                                name = "LESCO Electricity"
                                category = "Electricity & Power"
                                provider = "LESCO"
                                utilityType = "LESCO"
                                consumerNumber = "08 11223 3456701 U"
                                amountText = "48.50"
                                supportEmail = "billing@lesco.gov.pk"
                                notes = "Lahore Cantt Feeder • 320 Units"
                                unitsText = "320"
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ LESCO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LescoOrange
                        )
                    }

                    // K-Electric Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(KElectricGreen.copy(alpha = 0.15f))
                            .border(1.dp, KElectricGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = true
                                name = "K-Electric Karachi"
                                category = "Electricity & Power"
                                provider = "K_ELECTRIC"
                                utilityType = "K_ELECTRIC"
                                consumerNumber = "0400018942631"
                                amountText = "41.20"
                                supportEmail = "billingsupport@ke.com.pk"
                                notes = "Clifton Sub-Division • 285 Units"
                                unitsText = "285"
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ K-Electric",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = KElectricGreen
                        )
                    }

                    // Sui Gas Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF97316).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFF97316).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = true
                                name = "Sui Gas (SNGPL)"
                                category = "Gas & Fuel"
                                provider = "SNGPL"
                                utilityType = "GAS"
                                consumerNumber = "249811094321"
                                amountText = "18.50"
                                supportEmail = "billing@sngpl.com.pk"
                                notes = "Domestic Gas Connection • SNGPL Punjab"
                                unitsText = "140"
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔥 Sui Gas",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF97316)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Pakistani Networks Row (Jazz, Zong, Telenor, Ufone)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Jazz Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFDC2626).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFDC2626).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = false
                                name = "Jazz Super Duper"
                                category = "Mobile Packages"
                                provider = "JAZZ"
                                utilityType = null
                                consumerNumber = "0300-1234567"
                                amountText = "12.00"
                                supportEmail = "customercare@jazz.com.pk"
                                notes = "Jazz 30-Day All-in-One Super Duper Card"
                                unitsText = ""
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📱 Jazz",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF87171)
                        )
                    }

                    // Zong Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF84CC16).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF84CC16).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = false
                                name = "Zong Monthly Pro"
                                category = "Mobile Packages"
                                provider = "ZONG"
                                utilityType = null
                                consumerNumber = "0312-9876543"
                                amountText = "11.50"
                                supportEmail = "customercare@zong.com.pk"
                                notes = "Zong 40GB Monthly Super Star Package"
                                unitsText = ""
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📱 Zong",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFA3E635)
                        )
                    }

                    // Telenor Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = false
                                name = "Telenor EasyCard"
                                category = "Mobile Packages"
                                provider = "TELENOR"
                                utilityType = null
                                consumerNumber = "0345-5551234"
                                amountText = "10.00"
                                supportEmail = "telenorcare@telenor.com.pk"
                                notes = "Telenor Monthly EasyCard Mega"
                                unitsText = ""
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📱 Telenor",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF38BDF8)
                        )
                    }

                    // Ufone Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEA580C).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFEA580C).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = false
                                name = "Ufone Super Card"
                                category = "Mobile Packages"
                                provider = "UFONE"
                                utilityType = null
                                consumerNumber = "0333-7890123"
                                amountText = "9.50"
                                supportEmail = "customercare@ufone.com"
                                notes = "Ufone Super Card Gold 30 Days"
                                unitsText = ""
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📱 Ufone",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFFB923C)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Global Cloud Storage & Streaming Row (Google One, iCloud, OneDrive, Netflix)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Google One Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanAccent.copy(alpha = 0.15f))
                            .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = false
                                name = "Google One 2TB"
                                category = "Cloud Storage"
                                provider = "GOOGLE_ONE"
                                utilityType = null
                                consumerNumber = ""
                                amountText = "9.99"
                                supportEmail = "googleone-support@google.com"
                                notes = "2TB Google Cloud & Photos Storage"
                                unitsText = ""
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☁️ Google One",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyanAccent
                        )
                    }

                    // Apple iCloud+ Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFA855F7).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = false
                                name = "Apple iCloud+ 200GB"
                                category = "Cloud Storage"
                                provider = "ICLOUD"
                                utilityType = null
                                consumerNumber = ""
                                amountText = "2.99"
                                supportEmail = "support@apple.com"
                                notes = "iCloud Private Relay & Backup"
                                unitsText = ""
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☁️ iCloud+",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFC084FC)
                        )
                    }

                    // Microsoft OneDrive Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2563EB).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF2563EB).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = false
                                name = "OneDrive 365 1TB"
                                category = "Cloud Storage"
                                provider = "ONEDRIVE"
                                utilityType = null
                                consumerNumber = ""
                                amountText = "6.99"
                                supportEmail = "support@microsoft.com"
                                notes = "Microsoft 365 Cloud Storage"
                                unitsText = ""
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "☁️ OneDrive",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF60A5FA)
                        )
                    }

                    // Netflix Preset
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FinanceCard)
                            .border(1.dp, FinanceBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                isUtility = false
                                name = "Netflix Standard HD"
                                category = "Streaming"
                                provider = "NETFLIX"
                                utilityType = null
                                consumerNumber = ""
                                amountText = "15.49"
                                supportEmail = "support@netflix.com"
                                notes = "Family streaming plan"
                                unitsText = ""
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎬 Netflix",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Service or Bill Name", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = FinanceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = FinanceCard,
                        unfocusedContainerColor = FinanceCard
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Consumer / Reference Number (Critical for LESCO / K-Electric)
                OutlinedTextField(
                    value = consumerNumber,
                    onValueChange = { consumerNumber = it },
                    label = { Text(if (isUtility) "Consumer / Reference Number (14 digits)" else "Account / Subscriber ID", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth().testTag("dialog_consumer_no_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = FinanceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = FinanceCard,
                        unfocusedContainerColor = FinanceCard
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Amount, Currency & Units Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (${if (currency == "PKR") "Rs" else "$"})", color = TextMuted) },
                        modifier = Modifier.weight(1.3f).testTag("dialog_amount_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = FinanceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = FinanceCard,
                            unfocusedContainerColor = FinanceCard
                        )
                    )

                    // Currency toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FinanceCard)
                            .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable {
                                currency = if (currency == "USD") "PKR" else "USD"
                            }
                            .padding(horizontal = 10.dp, vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyanAccent
                        )
                    }

                    if (isUtility) {
                        OutlinedTextField(
                            value = unitsText,
                            onValueChange = { unitsText = it },
                            label = { Text("Units (kWh)", color = TextMuted) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = FinanceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = FinanceCard,
                                unfocusedContainerColor = FinanceCard
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Due Date field
                OutlinedTextField(
                    value = nextDueDate,
                    onValueChange = { nextDueDate = it },
                    label = { Text("Next Due / Renewal Date (YYYY-MM-DD)", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = FinanceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = FinanceCard,
                        unfocusedContainerColor = FinanceCard
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Support / Billing Email (used for AI cancellation)
                OutlinedTextField(
                    value = supportEmail,
                    onValueChange = { supportEmail = it },
                    label = { Text("Support / Cancellation Email", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = FinanceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = FinanceCard,
                        unfocusedContainerColor = FinanceCard
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull() ?: 20.0
                        val parsedUnits = unitsText.toIntOrNull()
                        onSave(
                            initialItem?.id,
                            if (name.isBlank()) (if (isUtility) "LESCO Electricity" else "Subscription") else name,
                            category,
                            provider,
                            isUtility,
                            utilityType,
                            if (consumerNumber.isBlank()) null else consumerNumber,
                            parsedAmount,
                            currency,
                            billingCycle,
                            nextDueDate,
                            supportEmail,
                            if (notes.isBlank()) null else notes,
                            parsedUnits
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_subscription_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialItem != null) "Update Item" else "Save to Dashboard",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
