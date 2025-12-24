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

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshTokenClient

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
        
        // Certificate Pinning cho ngrok - chống MITM attack
        val certificatePinner = okhttp3.CertificatePinner.Builder()
            .add("*.ngrok-free.dev", "sha256/I2i3UibJLgMPr8JYgCTQqaXNpz6rPhDS794lc8Y+QtA=")
            .build()
        
        // Interceptor cho ngrok header
        val ngrokInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)  // Certificate Pinning enabled
            .addInterceptor(ngrokInterceptor)      // Bypass ngrok warning
            .addInterceptor(jwtAuthInterceptor)    // JWT auth
            .addInterceptor(loggingInterceptor)    // Logging
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
            .baseUrl(com.example.eduquizz.config.ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(@AuthRetrofit retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    /**
     * OkHttpClient RIÊNG cho refresh token - KHÔNG có JwtAuthInterceptor
     * để tránh circular dependency
     */
    @Provides
    @Singleton
    @RefreshTokenClient
    fun provideRefreshOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // Certificate Pinning cho ngrok - chống MITM attack
        val certificatePinner = okhttp3.CertificatePinner.Builder()
            .add("*.ngrok-free.dev", "sha256/I2i3UibJLgMPr8JYgCTQqaXNpz6rPhDS794lc8Y+QtA=")
            .build()
        
        // Interceptor cho ngrok header
        val ngrokInterceptor = okhttp3.Interceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
        
        return OkHttpClient.Builder()
            .certificatePinner(certificatePinner)  // Certificate Pinning enabled
            .addInterceptor(ngrokInterceptor)      // Bypass ngrok warning
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    @RefreshTokenClient
    fun provideRefreshRetrofit(@RefreshTokenClient okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.example.eduquizz.config.ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * AuthApiService dùng cho refresh token - KHÔNG đi qua JWT interceptor
     */
    @Provides
    @Singleton
    @RefreshTokenClient
    fun provideRefreshAuthApiService(@RefreshTokenClient retrofit: Retrofit): AuthApiService {
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