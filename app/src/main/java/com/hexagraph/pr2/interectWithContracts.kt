package com.hexagraph.pr2

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger
import java.util.*

fun interactWithContract(web3: Web3j) {
    val contractAddress =
        "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266" // Replace with the deployed contract address
    val privateKey =
        "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80" // Replace with your private key

    // Setup credentials and transaction manager
    val credentials = Credentials.create(privateKey)
    val transactionManager = RawTransactionManager(web3, credentials)

    // Example 1: Call upload_image (write to the blockchain)
    CoroutineScope(Dispatchers.IO).launch {
        val imageUrl =
            "https://raw.githubusercontent.com/elliesheny/Web3j_project/refs/heads/master/Interface_overview.png"

        Log.d("Web3j", Utf8String(imageUrl).toString())
        val uploadImageFunction = Function(
            "upload_image",
            listOf(
                Address(credentials.address), // First parameter: address
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
                Log.d("Web3j", "Transaction successful: ${receipt.transactionHash}")
            }
        } catch (e: Exception) {
            Log.e("Web3j", "Error in upload_image: ${e.message}", e)
        }

        // Example 2: Call get_final_images (read from the blockchain)
        val getFinalImagesFunction = Function(
            "get_final_images",
            emptyList(), // No input parameters
            listOf(org.web3j.abi.TypeReference.create(Utf8String::class.java))
        )

        try {
            val encodedReadFunction = FunctionEncoder.encode(getFinalImagesFunction)
            val response: EthCall = web3.ethCall(
                org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
                    credentials.address,
                    contractAddress,
                    encodedReadFunction
                ),
                org.web3j.protocol.core.DefaultBlockParameterName.LATEST
            ).send()

            val finalImages: List<Utf8String> = org.web3j.abi.FunctionReturnDecoder.decode(
                response.value,
                getFinalImagesFunction.outputParameters
            ) as List<Utf8String>
            println(finalImages.toString())
            finalImages.forEach { image ->
                println("Hii")
                Log.d("ContractInteraction", "Final Image URL: ${image.value}")
            }
        } catch (e: Exception) {
            Log.e("ContractInteraction", "Error in get_final_images: ${e.message}", e)
        }
    }
}
