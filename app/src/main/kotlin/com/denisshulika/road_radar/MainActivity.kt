package com.denisshulika.road_radar

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Встановлюємо Edge-to-Edge режим
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Враховуємо Insets для контейнера
        val mainContainer = findViewById<View>(R.id.main_container)
        ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { view, insets ->
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                systemBarsInsets.top, // Відступ для статус-бару
                view.paddingRight,
                systemBarsInsets.bottom // Відступ для навігаційної панелі
            )
            insets
        }


    }
}