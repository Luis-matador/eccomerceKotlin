# G2A Kotlin Store

App Android en Kotlin con arquitectura MVC básica para simular un ecommerce de juegos, keys y gift cards.

## Funcionalidades

- Catálogo de productos cargado desde SQLite
- Ficha de producto
- Carrito con añadir, quitar, incrementar y decrementar cantidad
- Checkout simulado
- Generación de claves digitales al comprar
- Historial de pedidos
- Cambio entre modo usuario y modo admin
- Subida de foto de perfil
- Alta/edición de productos por parte del admin, incluyendo imagen

## Arquitectura

- **Modelo**: `Product`, `CartLine`, `Order`, `OrderItem`, `User`, `StoreDatabaseHelper`
- **Vista**: fragments de catálogo, ficha, carrito, checkout, pedidos y perfil
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

- Usuario: `buyer@g2a.local`
- Admin: `admin@g2a.local`

El cambio entre ambos perfiles se hace desde la pestaña **Perfil**.

