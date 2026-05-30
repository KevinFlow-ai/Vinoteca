# Estructura del proyecto

## Vista general
```text
Vinoteca/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── androidTest/java/com/example/vinoteca/
│       │   └── ExampleInstrumentedTest.kt
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/
│       │   │   ├── documentacion/
│       │   │   │   ├── ARQUITECTURA.md
│       │   │   │   ├── BUILD.md
│       │   │   │   ├── ESTRUCTURA_PROYECTO.md
│       │   │   │   ├── NAVEGACION.md
│       │   │   │   ├── TESTING.md
│       │   │   │   ├── UI.md
│       │   │   │   └── imagenes/
│       │   │   ├── logos/
│       │   │   ├── prueba
│       │   │   └── vinoteca_backup.zip
│       │   ├── java/com/example/vinoteca/
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   ├── model/
│       │   │   ├── ui/
│       │   │   │   ├── agregar_editar_bebida/
│       │   │   │   ├── categoria/
│       │   │   │   ├── principal/
│       │   │   │   └── tema/
│       │   │   └── viewmodel/
│       │   └── res/
│       │       ├── drawable/
│       │       ├── mipmap-*/
│       │       ├── values/
│       │       └── xml/
│       └── test/java/com/example/vinoteca/
│           └── ExampleUnitTest.kt
├── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── gradle.properties
├── gradlew
├── gradlew.bat
├── README.md
└── settings.gradle.kts
```

## Explicación por carpetas

### `app/src/main/java/com/example/vinoteca/data`
- `AppDatabase.kt`: configuración Room.
- `BeverageDao.kt`: operaciones sobre bebidas.
- `CategoryDao.kt`: operaciones sobre categorías.
- `SubCategoryDao.kt`: operaciones sobre subcategorías.
- `BeverageRepository.kt`: coordinación de acceso a datos.

### `app/src/main/java/com/example/vinoteca/model`
- `Beverage.kt`: entidad principal de inventario.
- `Category.kt`: entidad de categoría.
- `SubCategory.kt`: entidad de subcategoría.

### `app/src/main/java/com/example/vinoteca/viewmodel`
- `BeverageViewModel.kt`: estado observable y operaciones de negocio de la app.
- `BeverageViewModelFactory.kt`: fábrica para construir el ViewModel.

### `app/src/main/java/com/example/vinoteca/ui/principal`
- `PantallaPrincipal.kt`: home rediseñada con búsqueda, métricas, filtros y listado.

### `app/src/main/java/com/example/vinoteca/ui/agregar_editar_bebida`
- `PantallaAgregarEditarBebida.kt`: formulario rediseñado para alta, edición y borrado.

### `app/src/main/java/com/example/vinoteca/ui/categoria`
- `PantallaGestionCategorias.kt`: alta, borrado y navegación a subcategorías.
- `PantallaGestionSubcategorias.kt`: alta y borrado por categoría.

### `app/src/main/java/com/example/vinoteca/ui/tema`
- `MarcaVinoteca.kt`: logo activo y tokens de identidad.
- `ColoresVinoteca.kt`: paleta base.
- `TipografiaVinoteca.kt`: escala tipográfica.
- `FormasVinoteca.kt`: radios y shapes.
- `TemaVinoteca.kt`: Material 3 dinámico + branding.

### `app/src/main/assets/logos`
- Contiene las propuestas de logo entregadas para la app.
- El rediseño selecciona `logo_vinoteca1.png` como referencia principal.

### `app/src/main/assets/documentacion`
- Reúne la documentación técnica interna del proyecto.
- Incluye Markdown y PNGs de arquitectura, navegación y UI.

### `app/src/main/res`
- `drawable/`: recursos vectoriales y launcher icon personalizado.
- `mipmap-*/`: iconos legacy generados previamente por Android Studio.
- `values/`: strings, colores XML y tema host.
- `xml/`: backup rules y FileProvider.

## Módulos
El proyecto usa un único módulo de aplicación:

- `app`: contiene UI, datos, modelos, assets y recursos Android.

## Carpetas no funcionales pero relevantes
- `.idea/`: configuración del IDE.
- `.gradle/`: cachés locales de Gradle.
- `build/` y `app/build/`: artefactos generados.

Estas carpetas no forman parte de la lógica del producto, pero sí del entorno de desarrollo.
