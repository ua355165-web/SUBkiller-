package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CancellationRequestEntity
import com.example.data.model.EventLogEntity
import com.example.data.model.SubscriptionEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubscriptionEntity::class,
        CancellationRequestEntity::class,
        EventLogEntity::class,
        UserProfileEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun cancellationDao(): CancellationDao
    abstract fun eventLogDao(): EventLogDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "subtrack_finance.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        ensureInitialProfile(database)
                        seedDemoSubscriptions(database)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        seedDemoSubscriptions(database)
                    }
                }
            }
        }

        private suspend fun ensureInitialProfile(database: AppDatabase) {
            val profileDao = database.userProfileDao()
            val existing = profileDao.getUserProfile()
            if (existing == null) {
                profileDao.insert(
                    UserProfileEntity(
                        id = "primary_user",
                        email = "user@subkiller.app",
                        name = "SubKiller User",
                        preferredCurrency = "USD",
                        isPremium = false,
                        scansPerformed = 0,
                        totalSaved = 0.0
                    )
                )
            }
        }

        private suspend fun seedDemoSubscriptions(database: AppDatabase) {
            val subDao = database.subscriptionDao()
            if (subDao.getSubscriptionCount() == 0) {
                val demoItems = listOf(
                    SubscriptionEntity(
                        id = "sub-jazz-01",
                        name = "Jazz World Monthly Super Duper",
                        category = "Telecom & Mobile",
                        provider = "Jazz",
                        isUtility = false,
                        amount = 1250.0,
                        currency = "PKR",
                        billingCycle = "Monthly",
                        nextDueDate = "2026-11-25",
                        status = "active",
                        isPaidThisMonth = false,
                        supportEmail = "support@jazz.com.pk",
                        notes = "Jazz World prepaid package - 10GB Data & 3000 Mins"
                    ),
                    SubscriptionEntity(
                        id = "sub-telenor-01",
                        name = "Simosa / Telenor EasyCard 950",
                        category = "Telecom & Mobile",
                        provider = "Telenor",
                        isUtility = false,
                        amount = 950.0,
                        currency = "PKR",
                        billingCycle = "Monthly",
                        nextDueDate = "2026-11-28",
                        status = "active",
                        isPaidThisMonth = false,
                        supportEmail = "customercare@telenor.com.pk",
                        notes = "Simosa Telenor App Monthly Hybrid Bundle"
                    ),
                    SubscriptionEntity(
                        id = "util-lesco-01",
                        name = "LESCO Lahore Electricity Bill",
                        category = "Electricity",
                        provider = "LESCO",
                        isUtility = true,
                        utilityType = "Electricity",
                        consumerNumber = "04-11234-5678901 U",
                        amount = 18450.0,
                        currency = "PKR",
                        billingCycle = "Monthly",
                        nextDueDate = "2026-11-22",
                        status = "active",
                        isPaidThisMonth = false,
                        unitsConsumed = 320,
                        supportEmail = "billinfo@lesco.gov.pk",
                        notes = "Residential A-1 Peak Hours Tariff (6pm - 10pm)"
                    ),
                    SubscriptionEntity(
                        id = "sub-zong-01",
                        name = "My Zong Monthly Digital Max",
                        category = "Telecom & Mobile",
                        provider = "Zong",
                        isUtility = false,
                        amount = 1400.0,
                        currency = "PKR",
                        billingCycle = "Monthly",
                        nextDueDate = "2026-12-05",
                        status = "active",
                        isPaidThisMonth = true,
                        supportEmail = "customercare@zong.com.pk",
                        notes = "My Zong App 100GB Data Bundle"
                    ),
                    SubscriptionEntity(
                        id = "sub-netflix-01",
                        name = "Netflix Premium 4K",
                        category = "Entertainment",
                        provider = "Netflix",
                        isUtility = false,
                        amount = 1100.0,
                        currency = "PKR",
                        billingCycle = "Monthly",
                        nextDueDate = "2026-11-18",
                        status = "active",
                        isPaidThisMonth = true,
                        supportEmail = "info@account.netflix.com",
                        notes = "Ultra HD 4-screen streaming"
                    )
                )
                subDao.insertAll(demoItems)
            }
        }
    }
}
