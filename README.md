# Luz Barata

App Android (Jetpack Compose) con el precio de la luz PVPC por horas en España.

- Precio de ahora, mínimo, media y máximo del día.
- Mejor franja de 1 a 4 horas seguidas para poner la lavadora, el lavavajillas o cargar el coche, con el ahorro frente a la media.
- Gráfico y lista de las 24 horas, coloreadas por tercios (barata / media / cara).
- Precios de mañana en cuanto se publican (hacia las 20:15) y aviso diario opcional.
- Península, Canarias, Baleares, Ceuta y Melilla.
- Funciona sin conexión con los días ya descargados.
- Calculadora de coste por electrodoméstico: lavadora, lavavajillas, secadora, horno, aire acondicionado y coche eléctrico, ahora frente a la mejor hora (Premium o 24 h con un anuncio).
- Anuncios de AdMob y versión Premium sin anuncios.

## Datos

API pública de Red Eléctrica, [REData](https://www.ree.es/es/datos/apidatos), sin clave:

```
https://apidatos.ree.es/es/datos/mercados/precios-mercados-tiempo-real
  ?start_date=2026-10-13T00:00&end_date=2026-10-13T23:59
  &time_trunc=hour&geo_trunc=electric_system&geo_limit=peninsular&geo_ids=8741
```

Se usa la serie `PVPC (€/MWh)` (id `1001`) y se pasa a €/kWh. Si llegan cuartos de hora se promedian por hora. El precio no incluye impuestos.

## Anuncios y Premium

| Formato | Dónde | Límite |
|---|---|---|
| Banner adaptativo | Pantallas de consulta | — |
| Intersticial | Solo en pausas naturales (al volver de Ajustes a la pantalla principal) | Uno cada 3 min como mucho |
| App Open | Al volver a la app desde segundo plano, nunca en el primer arranque | Uno cada 5 min como mucho |
| Recompensado | Cuando el usuario lo pide para desbloquear la calculadora de coste por electrodoméstico durante 24 h | — |

Premium (Google Play Billing) quita todos los anuncios y desbloquea la calculadora de coste por electrodoméstico para siempre:

- `premium_monthly`: suscripción mensual.
- `premium_lifetime`: producto de compra única.

Créalos en Play Console con esos IDs. Al arrancar se consulta la tienda, así que si la suscripción caduca vuelven los anuncios. Para una app con muchos ingresos conviene añadir verificación de compras en servidor.

Los IDs de AdMob van en `admob.properties` (no se sube a git; plantilla en `admob.properties.example`). Sin ese archivo se usan las unidades de prueba de Google. El consentimiento RGPD se pide con UMP antes de cargar ningún anuncio.

## Idiomas

Castellano (por defecto), catalán, gallego e inglés.

## Estructura

```
core/   Kotlin puro, sin Android: modelo, lectura de REData, franjas y tercios (con tests)
app/    Android: Compose, ViewModel, DataStore, WorkManager, OkHttp, AdMob
  data/           API, caché en disco, ajustes
  ui/             pantallas y componentes
  notifications/  aviso diario de los precios de mañana
  monetization/   consentimiento, anuncios y compras de Premium
```

## Compilar

Requisitos: Android Studio reciente (JDK 17+), Android SDK 36.

```bash
./gradlew :core:test        # tests de la lógica
./gradlew assembleDebug     # APK de depuración (anuncios de prueba)
./gradlew bundleRelease     # AAB para Google Play
```

### Release

1. Crea el keystore de subida y un `keystore.properties` en la raíz (no se sube a git):
   ```
   storeFile=upload.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```
2. Copia `admob.properties.example` como `admob.properties` y pon los IDs reales de AdMob de esta app.
3. Crea en Play Console los productos `premium_monthly` (suscripción) y `premium_lifetime` (compra única).
4. Publica `docs/privacy.html` (por ejemplo con GitHub Pages) y enlázala en la ficha de Play.

## Licencia

Los datos son de Red Eléctrica de España y se muestran con atribución.
