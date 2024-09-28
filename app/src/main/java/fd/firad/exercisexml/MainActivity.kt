package fd.firad.exercisexml

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import fd.firad.exercisexml.databinding.ActivityMainBinding
import fd.firad.exercisexml.room.AppDatabase
import fd.firad.exercisexml.room.ImageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID
import javax.inject.Inject
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Initial load of the last image
        lifecycleScope.launch {
            val lastImage = database.imageDao().getLastImage()
            if (lastImage != null) {
                // Convert ByteArray back to Bitmap and set it in ImageView
                val bitmap = BitmapFactory.decodeByteArray(lastImage.imageData, 0, lastImage.imageData.size)
                binding.imageView.setImageBitmap(bitmap)
            } else {
                fetchAndDisplayNewImage()
            }
        }

        binding.fetchButton.setOnClickListener {
            lifecycleScope.launch {
                fetchAndDisplayNewImage()
            }
        }
    }

    private suspend fun fetchAndDisplayNewImage() {
        if (isNetworkAvailable(this@MainActivity)) {
            val randomImageUrl = fetchRandomImageUrl()
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeStream(URL(randomImageUrl).openConnection().getInputStream())
            }

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()

            // Store image data in Room
            database.imageDao().insertImage(ImageEntity(imageData = imageData))

            // Display the newly fetched image
            binding.imageView.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this@MainActivity, "No connection available. Showing last cached image.", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun fetchRandomImageUrl(): String = withContext(Dispatchers.IO) {
        return@withContext "https://picsum.photos/200/300?random=${System.currentTimeMillis()}"
    }
}
