# Luz Barata

App Android (Jetpack Compose) con el precio de la luz PVPC por horas en España.

- Precio de ahora, mínimo, media y máximo del día.
- Mejor franja de 1 a 4 horas seguidas para poner la lavadora, el lavavajillas o cargar el coche, con el ahorro frente a la media.
- Gráfico y lista de las 24 horas, coloreadas por tercios (barata / media / cara).
- Precios de mañana en cuanto se publican (hacia las 20:15) y aviso diario opcional.
- Península, Canarias, Baleares, Ceuta y Melilla.
- Funciona sin conexión con los días ya descargados.
- Banner de AdMob con consentimiento RGPD (UMP).

## Datos

API pública de Red Eléctrica, [REData](https://www.ree.es/es/datos/apidatos), sin clave:

```
https://apidatos.ree.es/es/datos/mercados/precios-mercados-tiempo-real
  ?start_date=2026-10-13T00:00&end_date=2026-10-13T23:59
  &time_trunc=hour&geo_trunc=electric_system&geo_limit=peninsular&geo_ids=8741
```

Se usa la serie `PVPC (€/MWh)` (id `1001`) y se pasa a €/kWh. Si llegan cuartos de hora se promedian por hora. El precio no incluye impuestos.

## Estructura

```
core/   Kotlin puro, sin Android: modelo, lectura de REData, franjas y tercios (con tests)
app/    Android: Compose, ViewModel, DataStore, WorkManager, OkHttp, AdMob
  data/           API, caché en disco, ajustes
  ui/             pantallas y componentes
  notifications/  aviso diario de los precios de mañana
  ads/            consentimiento y banner
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
2. Pon los IDs reales de AdMob en `~/.gradle/gradle.properties`:
   ```
   ADMOB_APP_ID=ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
   ADMOB_BANNER_ID=ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
   ```
   Sin ellos, el build de release usa las unidades de prueba de Google.
3. Publica `docs/privacy.html` (por ejemplo con GitHub Pages) y enlázala en la ficha de Play.

## Licencia

Los datos son de Red Eléctrica de España y se muestran con atribución.
