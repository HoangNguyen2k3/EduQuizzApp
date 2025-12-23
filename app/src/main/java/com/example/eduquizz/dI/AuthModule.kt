package com.example.eduquizz.dI

import com.example.eduquizz.features.auth.data.api.AuthApiService
import com.example.eduquizz.features.auth.data.repository.SecureAuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Context
import com.example.eduquizz.security.TokenManager
import com.example.eduquizz.security.JwtAuthInterceptor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthRetrofit

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthOkHttpClient(
        jwtAuthInterceptor: JwtAuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(jwtAuthInterceptor)  // JWT first - adds Authorization header
            .addInterceptor(loggingInterceptor)   // Logging second - logs full request
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(@AuthRetrofit okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.1.16:8080/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(@AuthRetrofit retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideGoogleSignInClient(
        @ApplicationContext context: Context
    ): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1026710210552-v084tbclfnhotnrv4lvf5i0hgppihk2r.apps.googleusercontent.com") // Lấy từ Firebase Console
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: AuthApiService,
        firebaseAuth: FirebaseAuth,
        googleSignInClient: GoogleSignInClient,
        tokenManager: TokenManager,
        @ApplicationContext context: Context
    ): com.example.eduquizz.features.auth.data.repository.SecureAuthRepository {
        return com.example.eduquizz.features.auth.data.repository.SecureAuthRepository(
            apiService,
            firebaseAuth,
            googleSignInClient,
            tokenManager,
            context
        )
    }
}