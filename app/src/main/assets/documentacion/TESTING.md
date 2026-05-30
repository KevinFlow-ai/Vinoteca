# Testing de Vinoteca

## Estado actual de pruebas automatizadas
El proyecto incluye pruebas base de ejemplo:

- `app/src/test/java/com/example/vinoteca/ExampleUnitTest.kt`
- `app/src/androidTest/java/com/example/vinoteca/ExampleInstrumentedTest.kt`

Estas pruebas validan muy poco comportamiento real y sirven sobre todo como plantilla inicial del proyecto.

## Validación ejecutada tras el rediseño
- `./gradlew.bat assembleDebug`
- `./gradlew.bat testDebugUnitTest`

## Checklist manual recomendado
- Abrir la app con inventario ya cargado.
- Comprobar la búsqueda por nombre.
- Comprobar la búsqueda por código de barras.
- Cambiar entre categorías y subcategorías.
- Abrir una bebida existente y editarla.
- Crear una bebida nueva.
- Tomar una foto desde cámara.
- Seleccionar una foto desde galería.
- Escanear un código de barras.
- Eliminar una bebida.
- Crear y borrar categorías.
- Crear y borrar subcategorías.
- Exportar CSV.
- Importar CSV.
- Exportar ZIP.
- Importar ZIP.
- Probar la previsualización con zoom en imágenes.

## Qué falta por testear
- Tests unitarios reales para `BeverageViewModel`.
- Tests del repositorio para importación/exportación.
- Tests UI Compose para navegación y estados vacíos.
- Tests instrumentados para cámara, galería y FileProvider.
- Casos de error:
  - CSV mal formado
  - ZIP incompleto
  - imágenes inexistentes
  - categorías duplicadas
  - subcategorías duplicadas

## Riesgos actuales
- La mayor parte de la calidad funcional depende todavía de validación manual.
- La importación/exportación sería el primer candidato a recibir cobertura automatizada real.
