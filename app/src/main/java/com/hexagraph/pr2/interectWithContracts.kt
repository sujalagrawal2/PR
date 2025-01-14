package com.hexagraph.pr2

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger

fun interactWithContract(web3: Web3j) {
    val contractAddress =
        "0xB377a2EeD7566Ac9fCb0BA673604F9BF875e2Bab" // Replace with the deployed contract address
    val privateKey = "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80" // Replace with your private key

    // Setup credentials and transaction manager
    val credentials = Credentials.create(privateKey)
    val transactionManager = RawTransactionManager(web3, credentials)

    // Example 1: Call upload_image (write to the blockchain)
    CoroutineScope(Dispatchers.IO).launch {
        val imageUrl =
            "raven"

        Log.i("Web3j", Utf8String(imageUrl).toString())
        val uploadImageFunction = Function(
            "debug_upload",
            listOf(
                Utf8String(imageUrl)          // Second parameter: image URL as a string
            ),
            emptyList() // No output parameters
        )

        val encodedFunction = FunctionEncoder.encode(uploadImageFunction)
        try {
            val receipt: EthSendTransaction? = transactionManager.sendTransaction(
                DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT,
                contractAddress,
                encodedFunction,
                BigInteger.ZERO
            )
            if (receipt != null) {
                Log.d("Web3j", "Image Transaction successful: ${receipt.transactionHash}")
            }
        } catch (e: Exception) {
            Log.e("Web3j", "Error in upload_image: ${e.message}", e)
        }

//         Example 2: Call get_final_images (read from the blockchain)
        val getImageFunction = Function(
            "debug_image",
            emptyList(), // No input parameters
            listOf(TypeReference.create(Utf8String::class.java))
        )

        try {
            val encodedReadFunction = FunctionEncoder.encode(getImageFunction)
            val response: EthCall = web3.ethCall(
                org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                    credentials.address,
                    contractAddress,
                    encodedReadFunction
                ),
                org.web3j.protocol.core.DefaultBlockParameterName.LATEST
            ).send()
            val finalImage = org.web3j.abi.FunctionReturnDecoder.decode(
                response.value,
                getImageFunction.outputParameters
            )
            println(finalImage)
            Log.d("Web3j", "Final Image URL: $finalImage")
        } catch (e: Exception) {
            Log.e("Web3j", "Error in get_final_image: ${e.message}", e)
        }

        // Example 3: Call get_images (read from the blockchain)
        val getImagesFunction = Function(
            "debug_images",
            emptyList(), // No input parameters
            listOf(object : TypeReference<DynamicArray<Utf8String>>() {})
        )

        try {
            val encodedReadFunction2 = FunctionEncoder.encode(getImagesFunction)
            val response2: EthCall = web3.ethCall(
                org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                    credentials.address, // Empty sender address for ethCall
                    contractAddress,
                    encodedReadFunction2
                ),
                org.web3j.protocol.core.DefaultBlockParameterName.LATEST
            ).send()

            Log.d("Web3j", "Raw response data: ${response2.result}")

            val decodedResponse = FunctionReturnDecoder.decode(
                response2.result,
                getImagesFunction.outputParameters
            )
            Log.d("Web3j", "Decoded Response: ${decodedResponse[0].value}")

                val finalImages = decodedResponse[0] as DynamicArray<*>

                // Iterate over each element and decode the string values
                finalImages.value.forEachIndexed { index, utf8String ->
                    Log.d("Web3j", "Decoded Image URL[$index]: ${utf8String.value}")
                }

        } catch (e: Exception) {
            Log.e("Web3j", "Error in get_final_images: ${e.message}", e)
        }

    }
}
