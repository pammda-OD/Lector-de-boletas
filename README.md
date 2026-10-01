# Lector de boletas

API REST para analizar comprobantes de pago con Google Gemini. Recibe una imagen en Base64, identifica si corresponde a un comprobante y, si lo es, extrae sus datos principales. También puede buscar una operacion en el correo de Gmail configurado para ayudar a verificar un pago.

## Funcionalidades

- Clasifica imagenes como comprobantes de pago, boletas, facturas, recibos o vouchers.
- Extrae descripcion, monto, moneda, fecha, hora, metodo de pago, entidad emisora, numero de transaccion y datos personales visibles.
- Busca en la bandeja de entrada de Gmail un correo recibido alrededor de la fecha indicada cuyo contenido incluya el numero de operacion.
- Expone los servicios mediante una API HTTP construida con Ktor.

La verificacion por correo consulta mensajes por fecha y busca el numero de operacion en su contenido; no valida por si sola que el remitente sea autentico ni que el pago se haya liquidado en el banco. Las imagenes enviadas a analisis se procesan mediante Google Gemini.

## Requisitos

- JDK 21.
- Una clave de API de Google AI Studio con acceso a Gemini.
- Para verificar pagos: una cuenta de Gmail con IMAP habilitado y una contrasena de aplicacion.

## Configuracion

Configura las credenciales como variables de entorno antes de iniciar la aplicacion. No guardes claves ni contrasenas en `application.yaml` ni las subas al repositorio.

| Variable | Necesaria para | Descripcion |
| --- | --- | --- |
| `GOOGLE_API_KEY` | Ambos endpoints | Clave de Google Gemini. |
| `IMAP_USERNAME` | Verificacion de pagos | Direccion de correo de Gmail. |
| `IMAP_PASSWORD` | Verificacion de pagos | Contrasena de aplicacion de Gmail; no uses tu contrasena habitual. |

La conexion IMAP usa `imap.gmail.com` por SSL en el puerto `993`. El intervalo de busqueda predeterminado es desde un dia antes hasta un dia despues de la fecha indicada; se puede ajustar en `src/main/resources/application.yaml`.

### PowerShell

```powershell
$env:GOOGLE_API_KEY = "tu-clave-de-google"
$env:IMAP_USERNAME = "tu-cuenta@gmail.com"
$env:IMAP_PASSWORD = "tu-contrasena-de-aplicacion"
.\gradlew.bat run
```

### Linux o macOS

```bash
export GOOGLE_API_KEY="tu-clave-de-google"
export IMAP_USERNAME="tu-cuenta@gmail.com"
export IMAP_PASSWORD="tu-contrasena-de-aplicacion"
./gradlew run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints

### Analizar un comprobante

`POST /ai/extract-voucher`

Solicitud:

```json
{
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQ..."
}
```

`imageBase64` debe contener la imagen codificada en Base64. Se puede enviar como una cadena Base64 simple o como un data URI con el tipo MIME.

Respuesta de ejemplo:

```json
{
  "isVoucher": true,
  "data": {
    "descripcion": "Pago de servicio",
    "monto": "45.00",
    "numeroTransaccion": "123456789",
    "moneda": "PEN",
    "fecha": "2026-05-27",
    "hora": "14:35",
    "metodoPago": "YAPE",
    "entidad": "YAPE",
    "datosPersonales": {
      "nombre": null,
      "dni": null,
      "ruc": null,
      "email": null,
      "razonSocial": null
    }
  }
}
```

Los valores extraidos dependen de la informacion legible en la imagen; los campos que no se puedan identificar pueden ser `null`.

### Verificar una operacion en Gmail

`POST /ai/verify-payment`

Solicitud:

```json
{
  "fecha": "2026-05-27",
  "numeroTransaccion": "123456789"
}
```

Respuesta:

```json
{
  "verified": true,
  "message": "Operacion verificada en correo electronico."
}
```

## Compilar y probar

```powershell
.\gradlew.bat build
```

Para ejecutar las pruebas:

```powershell
.\gradlew.bat test
```

En Linux o macOS, reemplaza `.\gradlew.bat` por `./gradlew`.

## Ejecutar con Docker

Desde la raiz del proyecto:

```bash
docker build -t lector-de-boletas .
docker run --rm -p 8080:8080 \
  -e GOOGLE_API_KEY \
  -e IMAP_USERNAME \
  -e IMAP_PASSWORD \
  lector-de-boletas
```

Define las variables de entorno en la terminal antes de ejecutar `docker run`. La imagen utiliza Java 21 y expone el puerto `8080`.
