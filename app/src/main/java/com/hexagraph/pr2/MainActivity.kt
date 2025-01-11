package com.hexagraph.pr2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hexagraph.pr2.ui.theme.PR2Theme
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Web3ClientVersion
import org.web3j.protocol.http.HttpService
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PR2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }


                // https://mainnet.infura.io/v3/a9a5a65a808d493bb68ff68472476f36



//                val web3 = Web3j.build(HttpService("http://192.168.209.82:8545"))
                val web3 = Web3j.build(HttpService("http://192.168.156.243:8545"))
                thread {
                    try {
                        // Test connection
                        val clientVersion: Web3ClientVersion = web3.web3ClientVersion().send()
                        val version: String = clientVersion.web3ClientVersion
                        Log.d("Web3j", "Client Version: $version")
                    } catch (e: Exception) {
                        Log.e("Web3j", "Error: ${e.message}", e)
                    }
                }
                interactWithContract(web3)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PR2Theme {
        Greeting("Android")
    }
}