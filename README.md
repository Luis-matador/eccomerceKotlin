# G2A Kotlin Store

Aplicación Android en Kotlin que simula un ecommerce digital estilo **G2A / marketplace gaming**, orientado a la venta de:

- juegos digitales
- keys
- gift cards
- suscripciones

La app está pensada como proyecto académico/técnico completo, con interfaz moderna, autenticación local, carrito, checkout, favoritos, pedidos, panel admin y persistencia local real.

---

## 1. Qué incluye actualmente

### Funcionalidades principales

- autenticación local con **login y registro**
- persistencia de sesión
- catálogo de productos
- buscador con **debounce**
- filtros por categoría
- ordenación por:
  - relevancia
  - precio ascendente
  - precio descendente
  - popularidad
- favoritos por usuario
- carrusel de productos destacados
- ficha de producto
- carrito con:
  - añadir productos
  - aumentar cantidad
  - reducir cantidad
  - eliminar del carrito
- checkout simulado
- creación de pedidos
- generación automática de claves digitales
- historial de pedidos
- detalle completo de pedido
- perfil de usuario
- subida de foto de perfil
- panel administrador para:
  - crear productos
  - editar productos
  - subir foto del producto
  - cambiar precio, stock, categoría, descripción y destacado

---

## 2. Stack tecnológico

### Lenguaje
- **Kotlin**

### UI / Android
- **Fragments**
- **RecyclerView**
- **Material Components**
- **ViewBinding**

### Persistencia
- **Room**
- **SQLite** (Room se apoya sobre SQLite)

### Listas eficientes
- **DiffUtil / ListAdapter**

### Build / entorno
- **Gradle**
- **Android Gradle Plugin 9.x**

---

## 3. Arquitectura del proyecto

La app mantiene una estructura inspirada en MVC, aunque actualmente está reforzada con un repositorio de datos.

### Capas principales

#### Modelo
Representa los datos y estados del negocio:

- `Product`
- `CartLine`
- `Order`
- `OrderItem`
- `OrderWithItems`
- `User`
- `AuthResult`
- `CheckoutResult`
- `StoreSortOption`

#### Vista
Pantallas implementadas con fragments:

- `AuthFragment`
- `CatalogFragment`
- `FavoritesFragment`
- `ProductDetailFragment`
- `CartFragment`
- `CheckoutFragment`
- `OrdersFragment`
- `OrderDetailFragment`
- `ProfileFragment`

#### Controlador
- `StoreController`

Centraliza la lógica que usan las vistas:

- login / registro / logout
- catálogo y filtros
- favoritos
- carrito
- checkout
- pedidos

#### Persistencia / acceso a datos

- `StoreRepository`
- `StoreDatabase`
- `StoreDao`
- entidades Room en `data/room/StoreEntities.kt`

---

## 4. Base de datos

## Qué base de datos usa

La app usa **Room sobre SQLite**.

### Nombre del archivo de base de datos

La base se crea localmente como:

- `g2a_store_room.db`

### Persistencia actual

La app guarda localmente:

- usuarios registrados
- sesión iniciada
- productos
- favoritos
- carrito
- pedidos
- items del pedido
- URIs de fotos seleccionadas

### ¿Está poblada?

Sí. La base de datos se **puebla automáticamente en el primer arranque** con datos demo.

#### Usuarios demo cargados

- Usuario:
  - email: `buyer@g2a.local`
  - contraseña: `1234`

- Administrador:
  - email: `admin@g2a.local`
  - contraseña: `admin123`

#### Productos demo cargados

Entre otros, se insertan automáticamente:

- Elden Ring Nightreign Key
- Cyberpunk 2077 Ultimate Edition
- Minecraft Java & Bedrock
- PlayStation Store 50€
- EA Sports FC 26
- Game Pass Ultimate 3 Meses
- Nintendo eShop 25€

### Esquema exportado

El esquema generado por Room se guarda en:

- `app/schemas/`

Esto permite inspeccionar la estructura de la base de datos versionada en el repositorio.

---

## 5. Tablas / entidades actuales

### `users`
Campos principales:

- `id`
- `name`
- `email`
- `password`
- `role`
- `photo_uri`

### `products`
Campos principales:

- `id`
- `title`
- `platform`
- `category`
- `description`
- `price`
- `stock`
- `image_uri`
- `featured`

### `favorites`
Relaciona usuario y producto favorito:

- `user_id`
- `product_id`

### `cart_items`
Guarda el carrito por usuario:

- `user_id`
- `product_id`
- `quantity`

### `orders`
Cabecera del pedido:

- `user_id`
- `buyer_name`
- `buyer_email`
- `payment_method`
- `total`
- `created_at`
- `status`

### `order_items`
Detalle del pedido:

- `order_id`
- `product_id`
- `title`
- `platform`
- `unit_price`
- `quantity`
- `generated_keys`

---

## 6. Flujo funcional de la app

## 6.1 Autenticación

La app arranca mostrando la pantalla de acceso.

El usuario puede:

- iniciar sesión con una cuenta existente
- registrarse con una nueva cuenta
- usar el acceso rápido al admin demo

Cuando el login es correcto:

- se guarda la sesión localmente
- se habilita la navegación principal

---

## 6.2 Catálogo

En el catálogo hay:

- un hero visual principal
- un carrusel horizontal de destacados
- buscador con debounce
- filtro por categoría
- switch de solo favoritos
- ordenación por relevancia, precio o popularidad

Desde el catálogo se puede:

- abrir la ficha del producto
- añadir al carrito
- marcar o desmarcar favorito

---

## 6.3 Favoritos

Pantalla dedicada para revisar solo los productos favoritos.

Incluye:

- buscador
- filtro por categoría
- ordenación
- acceso a ficha
- añadir al carrito
- quitar favorito

---

## 6.4 Ficha de producto

Muestra:

- imagen
- título
- plataforma / categoría
- precio
- stock
- descripción
- botón de favorito
- botón de añadir al carrito
- botón de ir al carrito

---

## 6.5 Carrito

Permite:

- ver líneas de carrito
- subir cantidad
- bajar cantidad
- eliminar producto
- ver total
- ir al checkout

---

## 6.6 Checkout

Formulario de pago simulado con:

- nombre
- correo
- método de pago

Al confirmar la compra:

1. se valida el carrito
2. se valida stock
3. se crea el pedido
4. se crean los items del pedido
5. se generan claves digitales
6. se descuenta stock
7. se vacía el carrito

---

## 6.7 Pedidos

La pantalla de pedidos lista:

- número de pedido
- fecha
- método de pago
- estado
- total
- cantidad de productos

Desde cada item se puede abrir el detalle.

---

## 6.8 Detalle de pedido

Muestra:

- datos generales del pedido
- comprador
- total
- productos comprados
- claves generadas por cada producto

---

## 6.9 Perfil

Permite:

- ver datos del usuario
- cambiar foto de perfil
- cerrar sesión

Si el usuario es admin, también se muestra el panel administrador.

---

## 6.10 Panel admin

Disponible al iniciar sesión como administrador.

Permite:

- crear productos
- editar productos existentes
- subir imagen del producto
- configurar:
  - título
  - plataforma
  - categoría
  - descripción
  - precio
  - stock
  - destacado

---

## 7. Estructura relevante del proyecto

### Código Kotlin

- `app/src/main/java/com/example/myapplication/MainActivity.kt`
- `app/src/main/java/com/example/myapplication/controller/StoreController.kt`
- `app/src/main/java/com/example/myapplication/data/StoreRepository.kt`
- `app/src/main/java/com/example/myapplication/data/room/`
- `app/src/main/java/com/example/myapplication/model/`
- `app/src/main/java/com/example/myapplication/view/`
- `app/src/main/java/com/example/myapplication/view/adapter/`

### Recursos

- `app/src/main/res/layout/`
- `app/src/main/res/drawable/`
- `app/src/main/res/menu/`
- `app/src/main/res/values/`

### Esquema Room

- `app/schemas/`

---

## 8. Cómo iniciarlo

## Opción A — desde Android Studio

1. Abre el proyecto en Android Studio.
2. Espera a que sincronice Gradle.
3. Selecciona un emulador o dispositivo físico.
4. Pulsa **Run**.

---

## Opción B — compilar desde terminal

En Windows PowerShell:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\angel\AppData\Local\Android\Sdk"
Set-Location "C:\Users\angel\AndroidStudioProjects\MyApplication"
.\gradlew assembleDebug
```

---

## 9. Cómo ejecutar tests

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\angel\AppData\Local\Android\Sdk"
Set-Location "C:\Users\angel\AndroidStudioProjects\MyApplication"
.\gradlew testDebugUnitTest
```

Para validación completa:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\angel\AppData\Local\Android\Sdk"
Set-Location "C:\Users\angel\AndroidStudioProjects\MyApplication"
.\gradlew clean assembleDebug testDebugUnitTest
```

---

## 10. Dónde queda la APK

Tras compilar en debug:

- `app/build/outputs/apk/debug/app-debug.apk`

---

## 11. Instalar la APK manualmente en un dispositivo / emulador

Si tienes `adb` disponible:

```powershell
$env:ANDROID_HOME = "C:\Users\angel\AppData\Local\Android\Sdk"
& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r "C:\Users\angel\AndroidStudioProjects\MyApplication\app\build\outputs\apk\debug\app-debug.apk"
```

Para comprobar dispositivos conectados:

```powershell
$env:ANDROID_HOME = "C:\Users\angel\AppData\Local\Android\Sdk"
& "$env:ANDROID_HOME\platform-tools\adb.exe" devices
```

---

## 12. Cuentas demo

### Usuario normal
- email: `buyer@g2a.local`
- contraseña: `1234`

### Administrador
- email: `admin@g2a.local`
- contraseña: `admin123`

---

## 13. Estado técnico actual

Actualmente el proyecto ya incluye:

- Room como capa de persistencia
- RecyclerView con DiffUtil / ListAdapter
- ViewBinding por fragment
- navegación por fragments
- persistencia local funcional
- datos demo iniciales

### Nota importante

En `gradle.properties` se dejó activada esta propiedad:

- `android.disallowKotlinSourceSets=false`

Se está usando para mantener compatibilidad entre el Kotlin integrado del AGP y `KSP`/`Room` en este proyecto. El build funciona correctamente, aunque Gradle lo marca como experimental.

---

## 14. Build validado

El proyecto ha sido validado con:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\angel\AppData\Local\Android\Sdk"
Set-Location "C:\Users\angel\AndroidStudioProjects\MyApplication"
.\gradlew clean assembleDebug testDebugUnitTest
```

Resultado validado:

- **BUILD SUCCESSFUL**
- tests unitarios correctos

---

## 15. Repositorio

Repositorio remoto:

- `https://github.com/Luis-matador/eccomerceKotlin.git`

---

## 16. Posibles mejoras futuras

Si se quiere seguir evolucionando el proyecto, una siguiente fase razonable sería:

- migración a **MVVM + ViewModel + StateFlow**
- operaciones Room fuera del hilo principal
- paginación real
- backend/API real
- autenticación remota
- imágenes remotas
- tests instrumentados de UI


