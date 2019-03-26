package ro.expectations.radio

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
    }
}
