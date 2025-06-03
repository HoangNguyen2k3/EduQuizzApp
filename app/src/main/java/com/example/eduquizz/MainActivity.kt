package com.example.eduquizz
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.eduquizz.navigation.NavGraph
import com.example.eduquizz.ui.theme.EduQuizzTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.FirebaseDatabase

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        enableEdgeToEdge()
        setContent {
            EduQuizzTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding) // <-- Truyền padding ở đây
                    )
                }
            }
        }
    }
}

/*@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Top
    ) {
        Row {
                text = "Hello $name!",
                modifier = modifier
            )
            Text(
                text = "Hello $name!",
                modifier = modifier
            )
        }
        Row {
            Text(
                text = "Hello $name!",
                modifier = modifier
            )
            Text(
                text = "Hello $name!",
                modifier = modifier
            )
        }
    }
    Box {

    }
}*/



/*
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EduQuizzTheme {
        Greeting("Android")
    }
}*/