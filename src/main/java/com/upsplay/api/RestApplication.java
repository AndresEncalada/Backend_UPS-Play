package com.upsplay.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/api") // Esta es la ruta base para todos tus recursos REST
public class RestApplication extends Application {
    // No necesitas código adicional aquí para una configuración básica
}