# G2A Kotlin Store

App Android en Kotlin con arquitectura MVC básica para simular un ecommerce de juegos, keys y gift cards con look más cercano a un marketplace gaming.

## Funcionalidades

- Catálogo de productos cargado desde SQLite
- Login real con registro de usuarios en SQLite
- Búsqueda por texto y filtros por categoría
- Lista de favoritos y marcado con corazón
- Carrusel de productos destacados
- Ficha de producto
- Carrito con añadir, quitar, incrementar y decrementar cantidad
- Checkout simulado
- Generación de claves digitales al comprar
- Historial de pedidos
- Subida de foto de perfil
- Alta/edición de productos por parte del admin, incluyendo imagen

## Arquitectura

- **Modelo**: `Product`, `CartLine`, `Order`, `OrderItem`, `User`, `StoreDatabaseHelper`
- **Vista**: fragments de auth, catálogo, favoritos, ficha, carrito, checkout, pedidos y perfil
- **Controlador**: `StoreController`

## Base de datos

La app usa SQLite mediante `StoreDatabaseHelper` y se inicializa con datos demo en el primer arranque.

## Ejecutar

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\angel\AppData\Local\Android\Sdk"
.\gradlew assembleDebug
```

APK debug generado en:

`app\build\outputs\apk\debug\app-debug.apk`

## Cuentas demo

- Usuario: `buyer@g2a.local` / `1234`
- Admin: `admin@g2a.local` / `admin123`

El login y registro se realizan al abrir la app. El panel de administración aparece automáticamente al iniciar sesión como `admin`.

