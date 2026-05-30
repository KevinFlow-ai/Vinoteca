# Build de Vinoteca

## Requisitos
- Android Studio con SDK configurado.
- JDK 17+ o superior.
- Gradle Wrapper incluido en el repositorio.

## Compilar en debug
```bash
./gradlew.bat assembleDebug
```

Salida esperada:
- `app/build/outputs/apk/debug/app-debug.apk`

## Ejecutar pruebas unitarias
```bash
./gradlew.bat testDebugUnitTest
```

## Generar APK de release
```bash
./gradlew.bat assembleRelease
```

Salida esperada:
- `app/build/outputs/apk/release/`

Nota:
- El proyecto no define una firma release personalizada en el repositorio.
- Para distribución real conviene configurar signing propio antes de publicar.

## Generar AAB
```bash
./gradlew.bat bundleRelease
```

Salida esperada:
- `app/build/outputs/bundle/release/app-release.aab`

## Observación actual del entorno
El proyecto compila con `compileSdk = 36`, aunque el Android Gradle Plugin actual (`8.2.2`) muestra una advertencia de compatibilidad recomendando una versión más reciente.
