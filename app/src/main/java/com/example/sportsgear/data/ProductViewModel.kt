package com.example.sportsgear.data
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sportsgear.models.ProductModel
import com.example.sportsgear.navigation.ROUTE_VIEW_PRODUCTS
import com.example.sportsgear.network.ImgurService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class ProductViewModel : ViewModel() {

    private val database = com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("Products")

    private fun getImgurService(): ImgurService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder().addInterceptor(logging).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgur.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(ImgurService::class.java)
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File.createTempFile("temp_product_image", ".jpg", context.cacheDir)
            file.outputStream().use { output ->
                inputStream?.copyTo(output)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun uploadProductWithImage(
        uri: Uri,
        context: Context,
        name: String,
        category: String,
        price: Double,
        description: String,
        navController: NavController,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = getFileFromUri(context, uri)
            if (file == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", file.name, reqFile)

            val response = getImgurService().uploadImage(body, "Client-ID beac35a5df62e1a")

            if (response.isSuccessful) {
                val imageUrl = response.body()?.data?.link ?: ""
                val productId = database.push().key ?: ""

                val product = ProductModel(name, category,
                    price.toString(), description, imageUrl, productId)

                database.child(productId).setValue(product)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Product added successfully", Toast.LENGTH_SHORT).show()
                                navController.navigate(ROUTE_VIEW_PRODUCTS)
                            }
                        }
                    }.addOnFailureListener {
                        viewModelScope.launch {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Failed to save product", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun viewProducts(
        product: MutableState<ProductModel>,
        products: SnapshotStateList<ProductModel>,
        context: Context
    ): SnapshotStateList<ProductModel> {
        val ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("Products")

        ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                products.clear()
                for (snap in snapshot.children) {
                    val value = snap.getValue(ProductModel::class.java)
                    value?.let { products.add(it) }
                }
                if (products.isNotEmpty()) {
                    product.value = products.first()
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Toast.makeText(context, "Failed to fetch products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        return products
    }

    fun updateProduct(
        context: Context,
        navController: NavController,
        name: String,
        category: String,
        price: Double,
        description: String,
        productId: String
    ) {
        val dbRef = com.google.firebase.database.FirebaseDatabase.getInstance()
            .getReference("Products/$productId")

        val updatedProduct = ProductModel(name, category,
            price.toString(), description, "", productId)

        dbRef.setValue(updatedProduct).addOnCompleteListener { task ->
            val msg = if (task.isSuccessful) "Product updated" else "Product update failed"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            if (task.isSuccessful) navController.navigate(ROUTE_VIEW_PRODUCTS)
        }
    }

    fun deleteProduct(context: Context, productId: String, navController: NavController) {
        AlertDialog.Builder(context)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Yes") { _, _ ->
                val dbRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("Products/$productId")

                dbRef.removeValue().addOnCompleteListener { task ->
                    val msg = if (task.isSuccessful) "Product deleted" else "Delete failed"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (task.isSuccessful) navController.navigate(ROUTE_VIEW_PRODUCTS)
                }
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
