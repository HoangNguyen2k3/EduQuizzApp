package com.example.eduquizz.features.auth.components

import android.graphics.Bitmap
import android.util.Log
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/**
 * Dialog hiển thị Google reCAPTCHA v2 trong WebView
 * 
 * @param onDismiss Callback khi user đóng dialog
 * @param onTokenReceived Callback trả về reCAPTCHA token sau khi verify thành công
 * @param siteKey Google reCAPTCHA site key (public key)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecaptchaDialog(
    onDismiss: () -> Unit,
    onTokenReceived: (String) -> Unit,
    siteKey: String = "6LcsTjIsAAAAABrCKoTZAT2F5VsX32_4CcxmpPDG"
) {
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Timeout sau 15 giây
    LaunchedEffect(Unit) {
        delay(15000)
        if (isLoading) {
            Log.w("RecaptchaDialog", "⏱️ Load timeout after 15 seconds")
            isLoading = false
            hasError = true
            errorMessage = "Không thể tải reCAPTCHA. Vui lòng kiểm tra kết nối Internet."
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.7f),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                TopAppBar(
                    title = { Text("Xác thực bảo mật") },
                    actions = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Đóng")
                        }
                    }
                )
                
                // Loading indicator
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Đang tải reCAPTCHA...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                // Error message
                if (hasError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "⚠️",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onDismiss) {
                                Text("Đóng")
                            }
                        }
                    }
                }
                
                // WebView với reCAPTCHA
                if (!hasError) {
                    AndroidView(
                        factory = { context ->
                            Log.d("RecaptchaDialog", "📱 Creating WebView...")
                            WebView(context).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                }
                                
                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        Log.d("RecaptchaDialog", "📄 Page started: $url")
                                        isLoading = true
                                    }
                                    
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        Log.d("RecaptchaDialog", "✅ Page finished: $url")
                                        isLoading = false
                                        
                                        // Inject JavaScript để lấy token sau khi verify
                                        view?.evaluateJavascript(
                                            """
                                            (function() {
                                                console.log('Injecting callback handler...');
                                                var originalCallback = window.onCaptchaSuccess;
                                                window.onCaptchaSuccess = function(token) {
                                                    console.log('Captcha success! Token:', token);
                                                    if (window.Android) {
                                                        window.Android.onTokenReceived(token);
                                                    } else {
                                                        console.error('Android interface not found!');
                                                    }
                                                    if (originalCallback) originalCallback(token);
                                                };
                                            })();
                                            """.trimIndent()
                                        ) { result ->
                                            Log.d("RecaptchaDialog", "JavaScript injection result: $result")
                                        }
                                    }
                                    
                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        super.onReceivedError(view, request, error)
                                        Log.e("RecaptchaDialog", "❌ WebView error: ${error?.description}")
                                        isLoading = false
                                        hasError = true
                                        errorMessage = "Lỗi tải trang: ${error?.description ?: "Unknown error"}"
                                    }
                                }
                                
                                // Add JavaScript interface để nhận token
                                addJavascriptInterface(object {
                                    @android.webkit.JavascriptInterface
                                    fun onTokenReceived(token: String) {
                                        Log.d("RecaptchaDialog", "🎉 Received token: ${token.take(20)}...")
                                        onTokenReceived(token)
                                    }
                                }, "Android")
                                
                                // Load HTML với reCAPTCHA
                                Log.d("RecaptchaDialog", "🌐 Loading reCAPTCHA HTML with siteKey: ${siteKey.take(20)}...")
                                loadDataWithBaseURL(
                                    "https://www.google.com",
                                    createRecaptchaHtml(siteKey),
                                    "text/html",
                                    "UTF-8",
                                    null
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Tạo HTML chứa Google reCAPTCHA v2 checkbox
 */
private fun createRecaptchaHtml(siteKey: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <meta charset="UTF-8">
            <style>
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                body {
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    min-height: 100vh;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    padding: 16px;
                }
                .container {
                    text-align: center;
                    padding: 24px;
                    background: white;
                    border-radius: 16px;
                    box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                    max-width: 400px;
                    width: 100%;
                }
                h3 {
                    color: #333;
                    margin-bottom: 12px;
                    font-size: 20px;
                }
                .info {
                    color: #666;
                    font-size: 14px;
                    margin-bottom: 20px;
                    line-height: 1.5;
                }
                .loading {
                    color: #667eea;
                    font-size: 14px;
                    margin-top: 16px;
                }
                .g-recaptcha {
                    display: inline-block;
                }
                #error-msg {
                    display: none;
                    color: #e53e3e;
                    font-size: 13px;
                    margin-top: 12px;
                    padding: 12px;
                    background: #fff5f5;
                    border-radius: 8px;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <h3>🔒 Xác thực bảo mật</h3>
                <p class="info">Vui lòng xác nhận bạn không phải robot để tiếp tục</p>
                
                <div id="recaptcha-container">
                    <div class="g-recaptcha" 
                         data-sitekey="$siteKey"
                         data-callback="onCaptchaSuccess"
                         data-error-callback="onCaptchaError"
                         data-expired-callback="onCaptchaExpired">
                    </div>
                    <p class="loading" id="loading-msg">⏳ Đang tải reCAPTCHA...</p>
                </div>
                
                <div id="error-msg">
                    ⚠️ Không thể tải reCAPTCHA. Vui lòng kiểm tra kết nối Internet và thử lại.
                </div>
            </div>
            
            <script>
                console.log('🚀 reCAPTCHA HTML loaded');
                console.log('📍 Site Key:', '$siteKey'.substring(0, 20) + '...');
                console.log('🌐 Android interface available:', typeof window.Android !== 'undefined');
                
                // Hide loading message when reCAPTCHA loads
                var recaptchaCallback = function() {
                    console.log('✅ reCAPTCHA script loaded successfully');
                    document.getElementById('loading-msg').style.display = 'none';
                };
                
                function onCaptchaSuccess(token) {
                    console.log('🎉 reCAPTCHA verified! Token length:', token.length);
                    document.getElementById('loading-msg').style.display = 'none';
                    
                    if (window.Android && typeof window.Android.onTokenReceived === 'function') {
                        console.log('📤 Sending token to Android...');
                        window.Android.onTokenReceived(token);
                    } else {
                        console.error('❌ Android interface not available!');
                        alert('Error: Android interface not found');
                    }
                }
                
                function onCaptchaError() {
                    console.error('❌ reCAPTCHA error occurred');
                    document.getElementById('loading-msg').style.display = 'none';
                    document.getElementById('error-msg').style.display = 'block';
                }
                
                function onCaptchaExpired() {
                    console.warn('⏰ reCAPTCHA expired');
                    alert('reCAPTCHA đã hết hạn. Vui lòng thử lại.');
                }
                
                // Timeout check
                setTimeout(function() {
                    if (document.getElementById('loading-msg').style.display !== 'none') {
                        console.error('⏱️ reCAPTCHA load timeout');
                        document.getElementById('error-msg').style.display = 'block';
                    }
                }, 10000);
            </script>
            
            <!-- Load reCAPTCHA script with callback -->
            <script src="https://www.google.com/recaptcha/api.js?onload=recaptchaCallback&render=explicit" async defer></script>
        </body>
        </html>
    """.trimIndent()
}
