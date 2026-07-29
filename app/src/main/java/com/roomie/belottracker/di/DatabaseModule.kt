package com.roomie.belottracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.roomie.belottracker.data.BelotDatabase
import com.roomie.belottracker.data.dao.*
import com.roomie.belottracker.repository.GameRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BelotDatabase =
        Room.databaseBuilder(context, BelotDatabase::class.java, "belot_tracker_db")
            .fallbackToDestructiveMigration() // remove once shipping real data
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .build()

    @Provides fun provideGameDao(db: BelotDatabase): GameDao = db.gameDao()
    @Provides fun provideRoundDao(db: BelotDatabase): RoundDao = db.roundDao()
    @Provides fun provideScoreDao(db: BelotDatabase): ScoreDao = db.scoreDao()

    @Provides @Singleton
    fun provideGameRepository(
        gameDao: GameDao, roundDao: RoundDao, scoreDao: ScoreDao
    ): GameRepository = GameRepository(gameDao, roundDao, scoreDao)
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rounds ADD COLUMN trumpPickerIndex INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE games ADD COLUMN totals TEXT NOT NULL DEFAULT '[]'")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE rounds ADD COLUMN dealerIndex INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE rounds ADD COLUMN firstTrumpPickerIndex INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE games ADD COLUMN initialDealerIndex INTEGER NOT NULL DEFAULT 0")
    }
}
