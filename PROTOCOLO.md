# Protocolo de Comunicación: Tres en Raya

Este documento describe el flujo de comunicación entre el Cliente y el Servidor.

## Flujo General

El protocolo sigue una secuencia estricta de mensajes para coordinar el estado del juego entre dos jugadores.

---

### 1. Fase de Conexión y Espera

El servidor espera a que se conecten dos jugadores para poder iniciar una partida.

| Paso | Origen    | Destino                 | Mensaje / Acción                                                                                                    |
| :--- | :-------- | :---------------------- | :------------------------------------------------------------------------------------------------------------------ |
| 1    | Cliente 1 | Servidor                | Inicia una conexión TCP en `localhost:3030`.                                                                        |
| 2    | Servidor  | Cliente 1               | Envía `ESPERANDO A OTRO JUGADOR`.                                                                                   |
| 3    | Cliente 2 | Servidor                | Inicia una conexión TCP en `localhost:3030`.                                                                        |
| 4    | Servidor  | Cliente 2               | Envía `ESPERANDO A OTRO JUGADOR`.                                                                                   |
| 5    | Servidor  | Cliente 1 y Cliente 2 | Detecta que hay dos jugadores y crea un nuevo hilo de `Juego` para gestionar la partida. |

---

### 2. Inicio de la Partida

Una vez que el hilo de `Juego` comienza, se envía a ambos jugadores la información inicial.

| Paso | Origen   | Destino               | Mensaje / Acción                                                                                                                                                                                                                                                        |
| :--- | :------- | :-------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1    | Servidor | Cliente 1 y Cliente 2 | Envía `OK`.                                                                                                                                                                                                                                                             |
| 2    | Servidor | Cliente 1 y Cliente 2 | Envía `== BIENVENID@ AL JUEGO DE: TRES EN RAYA ==`.                                                                                                                                                                                                                       |
| 3    | Servidor | Cliente 1 y Cliente 2 | Envía las normas del juego: <br> `Las normas del juego son sencillas:` <br> `- Cuando sea tu turno, escribe la casilla...` <br> `- El formato es: 1 1 (fila 1 columna 1)`<br> `- ...`<br> `- Introduce 'SALIR' para dejar de juegar.` <br> `¡¡Buena suerte!!` |

---

### 3. Ciclo de Juego por Turnos

El juego continúa en un bucle hasta que hay un ganador, un empate o un jugador abandona.

#### 3.1. Solicitar Movimiento

| Origen   | Destino                      | Mensaje / Acción                                                                    |
| :------- | :--------------------------- | :---------------------------------------------------------------------------------- |
| Servidor | **Jugador en Turno**         | Envía `\n== TE TOCA ==` (con códigos de color ANSI).                                |
| Servidor | **Jugador en Espera**        | Envía `\n TURNO DE X/O` (con códigos de color ANSI).                                |
| Servidor | Cliente 1 y Cliente 2        | Envía la representación actualizada del **tablero de juego**.                       |
| Servidor | **Jugador en Turno**         | Envía el prompt `> ` para indicar que espera una respuesta.                         |

#### 3.2. Respuesta del Cliente

| Origen             | Destino  | Mensaje / Acción                                                                 |
| :----------------- | :------- | :------------------------------------------------------------------------------- |
| **Jugador en Turno** | Servidor | Envía su movimiento en formato `FILA COLUMNA` (ej: `2 1`) o la palabra `SALIR`. |

#### 3.3. Validación del Servidor

El servidor procesa la respuesta y actúa en consecuencia.

| Caso                                 | Origen   | Destino              | Mensaje / Acción                                                                                                                                                       |
| :----------------------------------- | :------- | :------------------- | :--------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Formato Incorrecto**               | Servidor | **Jugador en Turno** | Envía un mensaje de error como `*FORMATO INCORRECTO*...` o `*ERROR*...`. <br> Vuelve a enviar el prompt `> ` para una nueva entrada.                                       |
| **Casilla Ocupada**                  | Servidor | **Jugador en Turno** | Envía `\nCasilla ocupada. Elige otra`. <br> Vuelve a enviar el prompt `> ` para una nueva entrada.                                                                       |
| **Comando `SALIR`**                  | Servidor | Cliente 1 y Cliente 2 | Envía `\n*Juego cancelado*`. <br> Procede a la fase de **Finalización**.                                                                                              |
| **Movimiento Válido**                | Servidor | (Interno)            | Actualiza el estado del tablero. <br> Comprueba si hay un ganador o empate. <br> Si el juego continúa, cambia el turno al otro jugador y repite el ciclo (3.1). |

---

### 4. Finalización de la Partida

La partida termina cuando se cumple una condición de fin de juego.

| Caso      | Origen   | Destino               | Mensaje / Acción                                                                |
| :-------- | :------- | :-------------------- | :------------------------------------------------------------------------------ |
| **Victoria**  | Servidor | **Ganador**           | Envía `\n\n¡¡FELICIDADES: HAS GANADO!!`                                       |
|           | Servidor | **Perdedor**          | Envía `\n\nHas perdido. Más suerte la próxima vez!`                           |
| **Empate**    | Servidor | Cliente 1 y Cliente 2 | Envía `\n== EMPATE ==`                                                        |
| **Fin**       | Servidor | Cliente 1 y Cliente 2 | Después del mensaje de victoria o empate, envía **`TERMINADO`**.              |
| **Cierre**    | Cliente  | (Interno)             | Al recibir `TERMINADO`, el cliente imprime `=== PARTIDA FINALIZADA ===` y cierra la conexión. |

---
**Nota:** Los mensajes a menudo incluyen códigos de color ANSI (ej: `\u001B[34m`) para mejorar la legibilidad en la consola.