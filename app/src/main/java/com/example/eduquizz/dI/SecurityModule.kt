package com.example.eduquizz.dI

import android.content.Context
import com.example.eduquizz.security.SecurePreferencesManager
import com.example.eduquizz.data_save.SecureDataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    /**
     * Provide SecureDataStoreManager
     */
    @Provides
    @Singleton
    fun provideSecureDataStoreManager(
        @ApplicationContext context: Context
    ): SecureDataStoreManager {
        return SecureDataStoreManager(context)
    }

    /**
     * Provide SecurePreferencesManager (object singleton)
     * Không cần provide vì nó là object, nhưng có thể wrap nếu cần
     */
}