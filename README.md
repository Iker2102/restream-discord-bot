# Restream Discord Bot

Bot de Discord escrito en **Java puro** pensado para dos cosas muy claras:

1. Gestionar restreams de YouTube.
2. Servir como base sólida para un bot de Discord modular y mantenible.

El proyecto está orientado a desarrollo serio: estructura limpia, recarga en caliente en desarrollo y sin depender de Maven o Gradle.

---

## Qué hace el bot

- Detecta directos de YouTube y gestiona el restream.
- Sistema de **slash commands** completamente modular.
- Sistema de **componentes** (botones y menús) cargados automáticamente.
- Recarga de comandos y componentes sin reiniciar el bot.
- Separación clara entre lógica, Discord y utilidades.

---

## Estructura del proyecto

```text
src/
 ├─ bot/
 │  ├─ commands/        # Slash commands
 │  ├─ components/      # Botones y selects
 │  ├─ core/            # Contexto y estado global
 │  ├─ discord/         # Inicialización de JDA
 │  ├─ events/          # EventHandler central
 │  ├─ modules/         # Módulos (ej: restream)
 │  └─ Main.java
 │
 ├─ META-INF/services/  # ServiceLoader (commands y components)
 │
 └─ tools/              # Generadores y watchers
