# Linterna-Android
# Motion Pattern Engine (MPE)

Motor de reconocimiento de patrones de movimiento para Android.  
Permite grabar, guardar y comparar secuencias de aceleración personalizadas por el usuario.

---

## ¿Qué resuelve?

Los gestos predefinidos (como el "doble chop" de Motorola) son fijos e iguales para todos.  
Este motor permite que **cada usuario grabe su propio patrón** y lo use como disparador para cualquier acción en cualquier app.

---

## Arquitectura del motor

```
┌─────────────────────────────────────────────────────┐
│                Motion Pattern Engine                │
│                                                     │
│  [Acelerómetro] → [Captura] → [Normalización]       │
│                                    ↓                │
│                             [Grabación / DTW]       │
│                                    ↓                │
│                    [Ventana deslizante en tiempo real]│
│                                    ↓                │
│                          [Callback → App cliente]   │
└─────────────────────────────────────────────────────┘
```

---

## Concepto técnico

### 1. Captura
Muestreo del acelerómetro (ejes X, Y, Z) durante una ventana configurable de 1.5 a 3 segundos.

### 2. Normalización
- Filtro de paso bajo para aislar aceleración lineal y eliminar la componente gravitacional.
- Detección de picos de energía para ignorar movimientos accidentales de baja intensidad.

### 3. Grabación
Se guarda una secuencia de vectores de aceleración que representa el patrón del usuario.  
El patrón se persiste con un identificador único para ser reutilizado entre sesiones.

### 4. Comparación
Algoritmo **DTW (Dynamic Time Warping)** con umbral de similitud configurable.  
Tolera variaciones naturales en tiempo e intensidad: el usuario nunca reproduce el patrón exactamente igual.

### 5. Detección
Ventana deslizante en tiempo real que compara el flujo del acelerómetro con el patrón grabado.  
Cuando la similitud supera el umbral, se dispara el callback hacia la app cliente.

---

## Características

- Grabación de patrón personalizado desde UI de demostración.
- Persistencia con `SharedPreferences` o archivo interno.
- Callback de detección desacoplado: el motor no sabe qué hace la app con el evento.
- Tolerancia configurable a variaciones de tiempo e intensidad.
- Optimización de batería: el sensor se apaga tras inactividad configurable.
- Umbral de energía mínima para evitar falsos positivos por movimientos leves.

---

## Integración en cualquier app

```kotlin
// 1. Inicializar el motor
val motionEngine = MotionPatternEngine(context)

// 2. Grabar patrón (el usuario agita el dispositivo)
motionEngine.startRecording()
// ... esperar ventana de captura ...
val patternId = motionEngine.stopRecordingAndSave("mi_patron")

// 3. Activar escucha
motionEngine.startListening(patternId) { detected ->
    if (detected) {
        ejecutarAccion()
    }
}

// 4. Ciclo de vida
override fun onPause() { motionEngine.pause() }
override fun onResume() { motionEngine.resume() }
```

El motor es **agnóstico de la acción**: solo detecta y notifica.  
La app cliente decide qué hacer con el evento.

---

## Apps del ecosistema MPE

| App | Patrón | Acción |
|-----|--------|--------|
| **GestureTorch** | Patrón libre del usuario | Enciende / apaga linterna |
| **GestureLock** | Patrón secreto | Desbloquea pantalla |
| **VaultCalc** | Patrón dentro de calculadora | Revela apps ocultas |
| **GestureAlert** | Patrón de emergencia | Llama a contacto o servicios de emergencia |

Cada app comparte el mismo motor MPE. Solo cambia la interfaz y la acción disparada.

---

---

# GestureTorch

Primera instancia del Motor MPE.  
Linterna activada por patrón gestual personalizado.

---

## ¿Qué hace?

GestureTorch reemplaza el gesto fijo de "doble chop" por **el patrón que vos elegís**.  
Agitás el teléfono como querés, guardás ese patrón, y desde ese momento es tu interruptor personal de linterna.

---

## Flujo de usuario

```
Primera vez:
┌──────────────────────────────────────┐
│  Pantalla de bienvenida              │
│  → "Grabá tu patrón"                │
│  → Usuario agita el teléfono        │
│  → Patrón guardado                  │
│  → "Ya podés usar tu linterna"      │
└──────────────────────────────────────┘

Uso cotidiano:
┌──────────────────────────────────────┐
│  Pantalla apagada / app en background│
│  → Usuario replica su patrón        │
│  → MPE detecta coincidencia         │
│  → Linterna ON / OFF                │
└──────────────────────────────────────┘
```

---

## Integración con MPE

```kotlin
class GestureTorchService : Service() {

    private lateinit var motionEngine: MotionPatternEngine
    private lateinit var cameraManager: CameraManager
    private var torchOn = false

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        motionEngine = MotionPatternEngine(this)

        val patternId = "gesturetorch_pattern"

        motionEngine.startListening(patternId) { detected ->
            if (detected) toggleTorch()
        }
    }

    private fun toggleTorch() {
        val cameraId = cameraManager.cameraIdList[0]
        torchOn = !torchOn
        cameraManager.setTorchMode(cameraId, torchOn)
    }

    override fun onDestroy() {
        motionEngine.stop()
        super.onDestroy()
    }
}
```

---

## Pantallas principales

### 1. Onboarding / Grabación
- Instrucción clara: "Agitá el teléfono con tu patrón."
- Indicador visual de captura activa.
- Confirmación y opción de repetir.
- Guardado del patrón.

### 2. Estado principal
- Indicador de escucha activa.
- Acceso a regrabar patrón.
- Ajuste de sensibilidad (umbral DTW).
- Toggle de servicio en background.

### 3. Configuración
- Sensibilidad: baja / media / alta.
- Ventana de captura: 1.5 / 2 / 3 segundos.
- Pausa automática: configurable.
- Patrón actual: ver / borrar / regrabar.

---

## Permisos requeridos

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.flash" android:required="true" />
<!-- No requiere permisos de red ni acceso a datos del usuario -->
```

---

## Consideraciones técnicas

**Background**: GestureTorch corre como `ForegroundService` con notificación persistente para mantener el sensor activo cuando la pantalla está apagada.

**Batería**: El motor implementa modo de bajo consumo cuando no detecta movimiento durante un período configurable. El sensor se reactiva con cualquier aceleración mínima.

**Falsos positivos**: El umbral de energía mínima filtra golpes accidentales (bolsillo, mesa). El DTW agrega una segunda capa de validación contra el patrón grabado.

**Dispositivos sin flash**: La app detecta ausencia de flash en `onCreate` y muestra aviso al usuario antes de completar el onboarding.

---

## Falsos positivos conocidos

El motor debe filtrar estos casos comunes:

| Caso | Característica | Filtro |
|------|---------------|--------|
| Teléfono en mochila al caminar | Patrón periódico de baja energía | Umbral de energía mínima |
| Golpe contra mesa | Pico único sin estructura temporal | DTW: no coincide con patrón grabado |
| Caída sobre superficie blanda | Aceleración sostenida sin picos definidos | Umbral + validación de estructura |

---

## Formato de persistencia del patrón

```json
{
  "id": "gesturetorch_pattern",
  "timestamp": 1700000000000,
  "windowDurationMs": 2000,
  "sampleRate": 50,
  "values": [[x1,y1,z1], [x2,y2,z2], "..."],
  "energyThreshold": 2.5
}
```

---

## DTW — pseudocódigo de referencia

```
distancia(i, j) = |a_i - b_j| + min(
    distancia(i-1, j),
    distancia(i, j-1),
    distancia(i-1, j-1)
)
similitud = 1 / (1 + distancia_normalizada)
```

Similitud = 1.0 es coincidencia perfecta. El umbral recomendado para arrancar es 0.75, ajustable por el usuario.

---

## Roadmap GestureTorch

- [ ] MVP: grabación + detección + toggle linterna.
- [ ] Servicio en background estable.
- [ ] UI onboarding.
- [ ] Ajuste de sensibilidad desde UI.
- [ ] Soporte multi-patrón (patrón A = ON, patrón B = OFF).
- [ ] Widget.
- [ ] Tile de accesibilidad rápida.
- [ ] Monetización: versión gratuita con ads / versión pro sin ads.

---

*Motor MPE — arquitectura escalable para el ecosistema gestual.*
