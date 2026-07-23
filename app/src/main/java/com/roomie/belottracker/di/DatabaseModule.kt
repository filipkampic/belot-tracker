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
            .addMigrations(MIGRATION_3_4)
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
        db.execSQL(
            "ALTER TABLE rounds ADD COLUMN trumpPickerIndex INTEGER NOT NULL DEFAULT 0"
        )
    }
}
