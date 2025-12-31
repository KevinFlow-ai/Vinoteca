plugins {
    alias(libs.plugins.android.application) apply false // Permite construir la app Android.
    alias(libs.plugins.kotlin.android) apply false // Permite construir la app Android con Kotlin.
    alias(libs.plugins.ksp) apply false // Necesario para generar código de Room (DAOs, Entities, etc.).
}
