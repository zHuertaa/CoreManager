# Mantener las clases principales de tu plugin
-keep public class com.zhuerta.coremanager.** { *; }

# Mantener las clases relocalizadas
-keep class com.zhuerta.coremanager.shaded.** { *; }

# Mantener las clases de Velocity API que usamos
-keep class com.velocitypowered.api.** { *; }

# Mantener las clases de Adventure API que usamos
-keep class net.kyori.adventure.** { *; }

# Mantener las clases de Configurate que usamos
-keep class ninja.leaping.configurate.** { *; }

# Mantener todas las clases de JDA que usamos, incluidas las internas
-keep class net.dv8tion.jda.** { *; }
-keep class com.zhuerta.coremanager.shaded.jda.** { *; }

# Mantener las clases de OkHttp que usamos
-keep class okhttp3.** { *; }
-keep class com.zhuerta.coremanager.shaded.okhttp3.** { *; }

# Mantener las clases de Jackson que usamos
-keep class com.fasterxml.jackson.** { *; }
-keep class com.zhuerta.coremanager.shaded.jackson.** { *; }

# Mantener las clases de org.json
-keep class org.json.** { *; }

# Mantener clases relacionadas con WebSocket que JDA usa
-keep class com.neovisionaries.ws.client.** { *; }

# Evitar advertencias sobre clases no encontradas
-dontwarn **