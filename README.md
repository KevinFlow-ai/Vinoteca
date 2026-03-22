# 🍷 Vinoteca: De la Estantería al Código

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-green.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue.svg)](https://developer.android.com/topic/libraries/architecture/viewmodel)

**Vinoteca** es una solución móvil diseñada para optimizar la eficiencia logística en la reposición de productos en grandes superficies. 
El proyecto nace de una experiencia real en el sector retail, transformando un problema operativo en una herramienta digital de alto impacto.

---

## 🚀 El Origen: Un Problema de 1.5 Horas casi dos

Durante mi experiencia como reponedor en **LIDL**, me enfrenté a un reto diario: gestionar la entrada de más de 30 cajas de vino de distintas variedades. 

*   **El Reto:** El pasillo de vinos es denso y la distribución no siempre es intuitiva. Un empleado nuevo o alguien cubriendo una tienda distinta perdía 
    la mayor parte del tiempo **buscando la ubicación** del producto, no reponiéndolo.
*   **La Métrica:** Inicialmente, completar la reposición tomaba **1 hora y media**. Tras memorizar las ubicaciones, el tiempo bajaba a **1 hora**.
*   **La Conclusión:** La ineficiencia no estaba en el esfuerzo físico, sino en la **latencia de información**. La memoria humana era el único "sistema de datos" disponible.

---

## 💡 La Solución: Digitalización del Pasillo

**Vinoteca** elimina la curva de aprendizaje y la necesidad de memorización. La aplicación permite a cualquier operario 
escanear o buscar un producto y saber **exactamente dónde debe colocarlo** en cuestión de segundos.

### Características Principales:
*   🔍 **Búsqueda Instantánea:** Localiza cualquier vino por nombre o mediante escaneo de código de barras.
*   📍 **Ubicación Precisa:** Información detallada sobre el estante o sección donde debe ir el producto.
*   📸 **Referencia Visual:** Visualización de imágenes con funciones de zoom para asegurar que el producto coincida con la etiqueta del lineal.
*   🗂️ **Gestión Jerárquica:** Organización por categorías y subcategorías para reflejar fielmente la estructura de la tienda.
*   📦 **Sistema de Backups Inteligentes:** Exportación e importación de datos completos (incluyendo imágenes) mediante archivos comprimidos (.zip) para facilitar el despliegue en nuevos dispositivos.

---

## 🛠️ Stack Tecnológico

El proyecto ha sido desarrollado siguiendo las mejores prácticas modernas de Android:

*   **Lenguaje:** Kotlin.
*   **UI:** Jetpack Compose (Material 3) para una interfaz moderna, reactiva y eficiente.
*   **Persistencia:** Room Database para el almacenamiento local de alta disponibilidad.
*   **Arquitectura:** MVVM (Model-View-ViewModel) que garantiza una separación clara de responsabilidades y facilidad de mantenimiento.
*   **Procesamiento Asíncrono:** Corrutinas de Kotlin y Flow para una experiencia de usuario fluida sin bloqueos.
*   **Gestión de Imágenes:** Coil para la carga asíncrona y eficiente de recursos visuales.

---

## 📈 Impacto Real

El uso de esta herramienta permite:
1.  **Reducción del 33% del tiempo operativo** en tareas de reposición.
2.  **Onboarding inmediato**: Un empleado nuevo es tan eficiente como uno veterano desde el primer minuto.
3.  **Escalabilidad**: Sistema preparado para importar inventarios completos de diferentes tiendas mediante paquetes de datos pre-configurados.

---

## 📦 Instalación y Despliegue

Este repositorio está preparado para ser clonado y ejecutado directamente en Android Studio.

1.  Clona el repositorio:
    ```bash
    git clone https://github.com/tu-usuario/vinoteca.git
    ```
    
2.  Importa el proyecto en **Android Studio**.
3.  La aplicación cuenta con un sistema de **Auto-Importación**: Al iniciarla por primera vez, detectará el archivo de respaldo en los assets y pre-cargará todo el inventario de prueba (incluyendo fotos y ubicaciones).

---

## 👨‍💻 Sobre mí

Soy un desarrollador con enfoque en **resolver problemas reales a través de la tecnología**.

¡Conectemos!

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Kevin_Flores-blue?style=for-the-badge&logo=linkedin)](https://www.linkedin.com/in/kevin-flores-full-stack-developer)


---

> *"La tecnología no sirve de nada si no ahorra tiempo y esfuerzo a las personas que la utilizan."*
