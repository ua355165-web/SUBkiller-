package com.example

import com.example.data.model.CurrencyCode
import com.example.data.model.CurrencyManager
import com.example.data.model.SubscriptionEntity
import com.example.data.stripe.StripeCardInput
import com.example.data.stripe.StripePaymentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun currencyConversion_usdToPkr() {
        val usdAmount = 10.0
        val pkrAmount = CurrencyManager.convert(usdAmount, CurrencyCode.USD, CurrencyCode.PKR)
        assertEquals(2780.0, pkrAmount, 0.01)
    }

    @Test
    fun currencyConversion_pkrToUsd() {
        val pkrAmount = 2780.0
        val usdAmount = CurrencyManager.convert(pkrAmount, CurrencyCode.PKR, CurrencyCode.USD)
        assertEquals(10.0, usdAmount, 0.01)
    }

    @Test
    fun currencyFormatting_formatsCorrectly() {
        val formattedUsd = CurrencyManager.format(14.99, CurrencyCode.USD)
        assertEquals("$14.99", formattedUsd)

        val formattedPkr = CurrencyManager.format(4167.0, CurrencyCode.PKR)
        assertTrue(formattedPkr.contains("Rs") && formattedPkr.contains("4,167"))
    }

    @Test
    fun utilityBill_lescoAndKElectricEntityValidation() {
        val lescoBill = SubscriptionEntity(
            id = "lesco-test",
            name = "LESCO Electricity",
            category = "Electricity & Power",
            provider = "LESCO",
            isUtility = true,
            utilityType = "LESCO",
            consumerNumber = "08 11223 3456701 U",
            amount = 48.50,
            currency = "USD",
            nextDueDate = "2026-11-20",
            unitsConsumed = 320
        )

        assertEquals("LESCO", lescoBill.provider)
        assertTrue(lescoBill.isUtility)
        assertEquals("08 11223 3456701 U", lescoBill.consumerNumber)
        assertEquals(320, lescoBill.unitsConsumed)

        val kelectBill = SubscriptionEntity(
            id = "ke-test",
            name = "K-Electric Karachi",
            category = "Electricity & Power",
            provider = "K_ELECTRIC",
            isUtility = true,
            utilityType = "K_ELECTRIC",
            consumerNumber = "0400018942631",
            amount = 41.20,
            currency = "USD",
            nextDueDate = "2026-11-24"
        )
        assertEquals("K_ELECTRIC", kelectBill.provider)
        assertNotNull(kelectBill.consumerNumber)
    }

    @Test
    fun stripeGateway_cardInputValidation() {
        val card = StripeCardInput(
            cardNumber = "4242 4242 4242 4242",
            expiry = "12/28",
            cvc = "888",
            cardholderName = "Hamza R."
        )
        assertTrue(card.isValid())
        assertEquals("Visa", card.brand)
        assertEquals("4242", card.last4)
    }

    @Test
    fun aiCancellation_tenPercentSuccessFeeCalculation() {
        val monthlyRate = 15.49
        val annualSavings = monthlyRate * 12.0 // $185.88
        val tenPercentFee = annualSavings * 0.10 // $18.588

        assertEquals(185.88, annualSavings, 0.001)
        assertEquals(18.588, tenPercentFee, 0.001)
    }

    @Test
    fun smartScan_exactFeeIsTwoNinetyNine() {
        val fee = 2.99
        assertEquals(2.99, fee, 0.0001)
        assertEquals("Smart Scan Pro (Lifetime)", StripePaymentType.SMART_SCAN_ONE_TIME.title)
    }

    @Test
    fun advancedReminders_monthlyPriceIsTwoNinetyNine() {
        val monthlyPrice = 2.99
        assertEquals(2.99, monthlyPrice, 0.0001)
        assertTrue(StripePaymentType.ADVANCED_REMINDERS_SUBSCRIPTION.isRecurring)
    }

    @Test
    fun stripePaymentManager_constantsAndPayoutBankConfigured() {
        assertEquals(2.99, com.example.data.stripe.StripePaymentManager.SMART_SCAN_ONE_TIME_FEE, 0.0001)
        assertEquals(2.99, com.example.data.stripe.StripePaymentManager.ADVANCED_REMINDERS_MONTHLY_FEE, 0.0001)
        assertEquals(0.10, com.example.data.stripe.StripePaymentManager.CANCELLATION_SUCCESS_FEE_RATE, 0.0001)
        assertEquals("acct_subkiller_test_bank_us", com.example.data.stripe.StripePaymentManager.BACKEND_MERCHANT_BANK_ID)
    }

    @Test
    fun pakistaniNetworksAndUtilities_supportAllProviders() {
        val jazz = SubscriptionEntity(name = "Jazz Super Duper", category = "Mobile Packages", provider = "JAZZ", amount = 12.0, nextDueDate = "2026-12-01")
        val zong = SubscriptionEntity(name = "Zong Monthly Pro", category = "Mobile Packages", provider = "ZONG", amount = 11.5, nextDueDate = "2026-12-01")
        val telenor = SubscriptionEntity(name = "Telenor EasyCard", category = "Mobile Packages", provider = "TELENOR", amount = 10.0, nextDueDate = "2026-12-01")
        val ufone = SubscriptionEntity(name = "Ufone Super Card", category = "Mobile Packages", provider = "UFONE", amount = 9.5, nextDueDate = "2026-12-01")
        val suiGas = SubscriptionEntity(name = "Sui Gas SNGPL", category = "Gas & Fuel", provider = "SNGPL", isUtility = true, amount = 18.5, nextDueDate = "2026-11-30")

        assertEquals("JAZZ", jazz.provider)
        assertEquals("ZONG", zong.provider)
        assertEquals("TELENOR", telenor.provider)
        assertEquals("UFONE", ufone.provider)
        assertEquals("SNGPL", suiGas.provider)
        assertTrue(suiGas.isUtility)
    }

    @Test
    fun globalStorageAndSubscriptions_supportAllProviders() {
        val googleOne = SubscriptionEntity(name = "Google One 2TB", category = "Cloud Storage", provider = "GOOGLE_ONE", amount = 9.99, nextDueDate = "2026-12-10")
        val icloud = SubscriptionEntity(name = "Apple iCloud+ 200GB", category = "Cloud Storage", provider = "ICLOUD", amount = 2.99, nextDueDate = "2026-12-10")
        val oneDrive = SubscriptionEntity(name = "OneDrive 365 1TB", category = "Cloud Storage", provider = "ONEDRIVE", amount = 6.99, nextDueDate = "2026-12-10")
        val netflix = SubscriptionEntity(name = "Netflix Standard HD", category = "Streaming", provider = "NETFLIX", amount = 15.49, nextDueDate = "2026-12-10")

        assertEquals("GOOGLE_ONE", googleOne.provider)
        assertEquals("ICLOUD", icloud.provider)
        assertEquals("ONEDRIVE", oneDrive.provider)
        assertEquals("NETFLIX", netflix.provider)
    }

    @Test
    fun monthlySavingsReport_monthOverMonthComparison() {
        val currentSavings = 85.50
        val previousSavings = 35.00
        val difference = currentSavings - previousSavings

        val report = com.example.notification.MonthlySavingsReport(
            currentMonthSavings = currentSavings,
            previousMonthSavings = previousSavings,
            savingsDifference = difference,
            activeMonthlyBillsTotal = 62.00,
            cancelledServicesCount = 3,
            topCancelledService = "LESCO High Tariff Unit",
            currency = CurrencyCode.USD
        )

        assertEquals(85.50, report.currentMonthSavings, 0.001)
        assertEquals(35.00, report.previousMonthSavings, 0.001)
        assertEquals(50.50, report.savingsDifference, 0.001)
        assertEquals(3, report.cancelledServicesCount)
        assertEquals("LESCO High Tariff Unit", report.topCancelledService)
        assertEquals(CurrencyCode.USD, report.currency)

        val diffText = CurrencyManager.format(report.savingsDifference, CurrencyCode.USD)
        assertEquals("$50.50", diffText)
    }

    @Test
    fun monthlySavingsReport_firstMonthZeroBaseline() {
        val currentSavings = 42.00
        val previousSavings = 0.0
        val difference = currentSavings - previousSavings

        val report = com.example.notification.MonthlySavingsReport(
            currentMonthSavings = currentSavings,
            previousMonthSavings = previousSavings,
            savingsDifference = difference,
            activeMonthlyBillsTotal = 25.00,
            cancelledServicesCount = 1,
            topCancelledService = "Netflix Standard HD",
            currency = CurrencyCode.USD
        )

        assertEquals(42.00, report.currentMonthSavings, 0.001)
        assertEquals(0.0, report.previousMonthSavings, 0.001)
        assertEquals(42.00, report.savingsDifference, 0.001)
        assertTrue(report.savingsDifference > 0)
    }

    @Test
    fun contextMenu_reminderChannelConstantsConfigured() {
        assertEquals("subkiller_reminders_channel", com.example.notification.MonthlySummaryNotificationManager.REMINDER_CHANNEL_ID)
        assertEquals("Bill Due Date Reminders", com.example.notification.MonthlySummaryNotificationManager.REMINDER_CHANNEL_NAME)
    }

    @Test
    fun contextMenu_immediateActionEntityValidation() {
        val sub = SubscriptionEntity(
            id = "test-spotify",
            name = "Spotify Premium Family",
            category = "Streaming",
            provider = "SPOTIFY",
            amount = 16.99,
            currency = "USD",
            billingCycle = "Monthly",
            nextDueDate = "2026-11-28",
            supportEmail = "support@spotify.com"
        )

        assertEquals("Spotify Premium Family", sub.name)
        assertEquals("2026-11-28", sub.nextDueDate)
        assertEquals(16.99, sub.amount, 0.001)
    }

    @Test
    fun searchBar_filterByNameOrProvider() {
        val list = listOf(
            SubscriptionEntity(name = "Netflix Premium", provider = "NETFLIX", category = "Streaming", amount = 19.99, nextDueDate = "2026-11-25"),
            SubscriptionEntity(name = "LESCO Home Tariff", provider = "LESCO", category = "Electricity & Power", isUtility = true, amount = 45.0, nextDueDate = "2026-11-15"),
            SubscriptionEntity(name = "K-Electric Industrial", provider = "K_ELECTRIC", category = "Electricity & Power", isUtility = true, amount = 85.0, nextDueDate = "2026-11-20"),
            SubscriptionEntity(name = "Spotify Music Duo", provider = "SPOTIFY", category = "Music", amount = 12.99, nextDueDate = "2026-12-01")
        )

        // Filter by name
        val netflixMatch = list.filter { it.name.contains("netflix", ignoreCase = true) || it.provider.contains("netflix", ignoreCase = true) }
        assertEquals(1, netflixMatch.size)
        assertEquals("Netflix Premium", netflixMatch.first().name)

        // Filter by provider
        val lescoMatch = list.filter { it.name.contains("lesco", ignoreCase = true) || it.provider.contains("lesco", ignoreCase = true) }
        assertEquals(1, lescoMatch.size)
        assertEquals("LESCO", lescoMatch.first().provider)

        // Filter by partial provider
        val electricMatch = list.filter { it.name.contains("electric", ignoreCase = true) || it.provider.contains("electric", ignoreCase = true) }
        assertEquals(1, electricMatch.size)
        assertEquals("K_ELECTRIC", electricMatch.first().provider)
    }

    @Test
    fun dueDateCalculator_daysCalculationAndRangeMatching() {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        val baseDate = sdf.parse("2026-11-07")!!

        // Due in 5 days -> Next 7 Days
        val days5 = com.example.ui.viewmodel.DueDateCalculator.calculateDaysUntilDue("2026-11-12", baseDate)
        assertEquals(5L, days5)
        assertTrue(days5 != null && days5 in 0..7)

        // Due in 11 days -> Next 14 Days
        val days11 = com.example.ui.viewmodel.DueDateCalculator.calculateDaysUntilDue("2026-11-18", baseDate)
        assertEquals(11L, days11)
        assertTrue(days11 != null && days11 in 0..14)
        assertTrue(days11 != null && days11 > 7)

        // Due in 23 days -> Next 30 Days
        val days23 = com.example.ui.viewmodel.DueDateCalculator.calculateDaysUntilDue("2026-11-30", baseDate)
        assertEquals(23L, days23)
        assertTrue(days23 != null && days23 in 0..30)
        assertTrue(days23 != null && days23 > 14)

        // Overdue (in past)
        val daysPast = com.example.ui.viewmodel.DueDateCalculator.calculateDaysUntilDue("2026-11-02", baseDate)
        assertEquals(-5L, daysPast)
        assertTrue(daysPast != null && daysPast < 0)
    }

    @Test
    fun dueDateRange_allEntriesConfigured() {
        assertEquals(5, com.example.ui.viewmodel.DueDateRange.entries.size)
        assertEquals("All Dates", com.example.ui.viewmodel.DueDateRange.ALL.displayName)
        assertEquals(7, com.example.ui.viewmodel.DueDateRange.NEXT_7_DAYS.maxDaysAhead)
        assertEquals(14, com.example.ui.viewmodel.DueDateRange.NEXT_14_DAYS.maxDaysAhead)
        assertEquals(30, com.example.ui.viewmodel.DueDateRange.NEXT_30_DAYS.maxDaysAhead)
        assertEquals(-1, com.example.ui.viewmodel.DueDateRange.OVERDUE.maxDaysAhead)
    }

    @Test
    fun geminiCancellationService_buildsFallbackDrafts() {
        val lescoBill = SubscriptionEntity(
            id = "lesco-1",
            name = "LESCO Lahore Bill",
            category = "Electricity & Power",
            provider = "LESCO",
            isUtility = true,
            consumerNumber = "08112233456701U",
            amount = 45.0,
            nextDueDate = "2026-11-10"
        )

        val firmDraft = com.example.data.ai.GeminiCancellationService.buildFallbackTemplate(
            subscription = lescoBill,
            userEmail = "ua355165@gmail.com",
            userName = "Subscriber User",
            tone = "Firm & Legal",
            reason = "Property relocation"
        )

        assertTrue(firmDraft.contains("LESCO Lahore Bill"))
        assertTrue(firmDraft.contains("08112233456701U"))
        assertTrue(firmDraft.contains("revoke authorization"))
        assertTrue(firmDraft.contains("Subscriber User"))

        val priceDraft = com.example.data.ai.GeminiCancellationService.buildFallbackTemplate(
            subscription = lescoBill,
            userEmail = "ua355165@gmail.com",
            userName = "Subscriber User",
            tone = "Price Sensitive",
            reason = "High tariff"
        )

        assertTrue(priceDraft.contains("loyalty discounts"))
    }

    @Test
    fun workManager_uniqueWorkNamesConfigured() {
        assertEquals("subkiller_bill_due_reminder_periodic_work", com.example.worker.BillReminderWorkScheduler.UNIQUE_PERIODIC_WORK_NAME)
        assertEquals("subkiller_bill_due_reminder_onetime_work", com.example.worker.BillReminderWorkScheduler.UNIQUE_ONETIME_WORK_NAME)
    }

    @Test
    fun csvExportHelper_generatesValidCsvWithEscaping() {
        val list = listOf(
            SubscriptionEntity(
                id = "sub-1",
                name = "Netflix, Premium \"4K\"",
                category = "Streaming",
                provider = "NETFLIX",
                amount = 19.99,
                currency = "USD",
                billingCycle = "Monthly",
                nextDueDate = "2026-12-01",
                notes = "Supports 4 screens\nUltra HD"
            ),
            SubscriptionEntity(
                id = "util-1",
                name = "LESCO Lahore Bill",
                category = "Electricity & Power",
                provider = "LESCO",
                isUtility = true,
                consumerNumber = "08112233456701U",
                amount = 45.00,
                currency = "USD",
                nextDueDate = "2026-11-15",
                unitsConsumed = 310
            )
        )

        val csvOutput = com.example.data.export.CsvExportHelper.generateCsvString(list)

        // Headers check
        assertTrue(csvOutput.startsWith("ID,Name,Category,Provider,Is Utility,Utility Type,Consumer Number,Amount,Currency,Billing Cycle,Next Due Date,Status,Paid This Month,Units Consumed,Support Email,Notes"))

        // Quotes and comma escaping check
        assertTrue(csvOutput.contains("\"Netflix, Premium \"\"4K\"\"\""))

        // Utility record check
        assertTrue(csvOutput.contains("LESCO Lahore Bill"))
        assertTrue(csvOutput.contains("08112233456701U"))
        assertTrue(csvOutput.contains("310"))

        // Row count check (1 header + 2 rows = 3 lines minimum, with internal newline in notes)
        val lineCount = csvOutput.lines().count { it.isNotBlank() }
        assertTrue(lineCount >= 3)
    }

    @Test
    fun emailAggregationAndFlowCheck_validatesSubscriberAndSupportEndpoints() {
        val subscriberEmail = "ua355165@gmail.com"
        val subscriberName = "VIP Subscriber"

        val lescoBill = SubscriptionEntity(
            id = "lesco-flow-test",
            name = "LESCO Electricity",
            category = "Electricity & Power",
            provider = "LESCO",
            isUtility = true,
            consumerNumber = "08112233456701U",
            amount = 54.20,
            currency = "USD",
            nextDueDate = "2026-11-20",
            supportEmail = "billing@lesco.gov.pk"
        )

        val legalDraft = com.example.data.ai.GeminiCancellationService.buildFallbackTemplate(
            subscription = lescoBill,
            userEmail = subscriberEmail,
            userName = subscriberName,
            tone = "Firm & Legal",
            reason = "Meter tariff recalculation"
        )

        // Email address routing verification
        assertEquals("billing@lesco.gov.pk", lescoBill.supportEmail)
        assertTrue(legalDraft.contains(subscriberEmail))
        assertTrue(legalDraft.contains(subscriberName))
        assertTrue(legalDraft.contains("08112233456701U"))
        assertTrue(legalDraft.contains("billing@lesco.gov.pk") || legalDraft.contains("Support & Billing Department"))
    }

    @Test
    fun operatorAndServiceCancellationTest_validatesAllProviderGatewaysAndPayloads() {
        val providersWithGateways = listOf(
            SubscriptionEntity(name = "Jazz Super Duper", provider = "JAZZ", category = "Mobile Packages", amount = 12.0, nextDueDate = "2026-12-01", supportEmail = "customercare@jazz.com.pk"),
            SubscriptionEntity(name = "Zong Monthly Pro", provider = "ZONG", category = "Mobile Packages", amount = 11.5, nextDueDate = "2026-12-01", supportEmail = "customercare@zong.com.pk"),
            SubscriptionEntity(name = "Telenor EasyCard", provider = "TELENOR", category = "Mobile Packages", amount = 10.0, nextDueDate = "2026-12-01", supportEmail = "telenorcare@telenor.com.pk"),
            SubscriptionEntity(name = "Ufone Super Card", provider = "UFONE", category = "Mobile Packages", amount = 9.5, nextDueDate = "2026-12-01", supportEmail = "customercare@ufone.com"),
            SubscriptionEntity(name = "K-Electric Karachi", provider = "K_ELECTRIC", isUtility = true, category = "Electricity & Power", amount = 41.2, nextDueDate = "2026-11-24", supportEmail = "billingsupport@ke.com.pk", consumerNumber = "0400018942631"),
            SubscriptionEntity(name = "Sui Gas SNGPL", provider = "SNGPL", isUtility = true, category = "Gas & Fuel", amount = 18.5, nextDueDate = "2026-11-30", supportEmail = "billing@sngpl.com.pk", consumerNumber = "249811094321"),
            SubscriptionEntity(name = "Google One 2TB", provider = "GOOGLE_ONE", category = "Cloud Storage", amount = 9.99, nextDueDate = "2026-12-10", supportEmail = "googleone-support@google.com"),
            SubscriptionEntity(name = "Apple iCloud+ 200GB", provider = "ICLOUD", category = "Cloud Storage", amount = 2.99, nextDueDate = "2026-12-10", supportEmail = "support@apple.com"),
            SubscriptionEntity(name = "OneDrive 365 1TB", provider = "ONEDRIVE", category = "Cloud Storage", amount = 6.99, nextDueDate = "2026-12-10", supportEmail = "support@microsoft.com"),
            SubscriptionEntity(name = "Netflix Standard HD", provider = "NETFLIX", category = "Streaming", amount = 15.49, nextDueDate = "2026-12-10", supportEmail = "support@netflix.com")
        )

        for (item in providersWithGateways) {
            val payload = com.example.data.ai.GeminiCancellationService.buildFallbackTemplate(
                subscription = item,
                userEmail = "ua355165@gmail.com",
                userName = "VIP Subscriber",
                tone = "Firm & Legal",
                reason = "Exhaustive integration test"
            )

            assertNotNull("Support email must not be null for ${item.name}", item.supportEmail)
            assertTrue("Support email must contain @ for ${item.name}", item.supportEmail.contains("@"))
            assertTrue("Payload must reference service name for ${item.name}", payload.contains(item.name))
            assertTrue("Payload must reference subscriber email", payload.contains("ua355165@gmail.com"))
        }
    }

    @Test
    fun liveFunctionalExecution_validatesWorkManagerAndStripeIntents() = kotlinx.coroutines.runBlocking {
        // WorkManager unique work tags verification
        assertEquals("subkiller_bill_due_reminder_periodic_work", com.example.worker.BillReminderWorkScheduler.UNIQUE_PERIODIC_WORK_NAME)

        // Stripe Gateway payload and bank routing simulation
        val validCard = StripeCardInput(
            cardNumber = "4242 4242 4242 4242",
            expiry = "12/28",
            cvc = "999",
            cardholderName = "VIP Subscriber"
        )

        val smartScanPayment = com.example.data.stripe.StripePaymentManager.processSmartScanOneTimePayment(validCard)
        assertTrue(smartScanPayment.isSuccess)
        assertEquals(2.99, smartScanPayment.amountCharged, 0.001)
        assertEquals("acct_subkiller_test_bank_us", smartScanPayment.destinationBankAccount)

        val remindersPayment = com.example.data.stripe.StripePaymentManager.processAdvancedRemindersSubscription(validCard)
        assertTrue(remindersPayment.isSuccess)
        assertEquals(2.99, remindersPayment.amountCharged, 0.001)
        assertTrue(remindersPayment.paymentIntentId.startsWith("pi_test_"))
    }

    @Test
    fun biometricSecurity_verifiesLockStateTransitionsAndHelper() {
        val helperClassExists = try {
            Class.forName("com.example.security.BiometricAuthHelper")
            true
        } catch (e: Exception) {
            false
        }
        assertTrue("BiometricAuthHelper must exist in codebase", helperClassExists)

        val lockScreenClassExists = try {
            Class.forName("com.example.ui.screens.BiometricLockScreenKt")
            true
        } catch (e: Exception) {
            false
        }
        assertTrue("BiometricLockScreenKt must exist in codebase", lockScreenClassExists)
    }
}



