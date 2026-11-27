package com.softcorp.SoftCorpBackend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AppInfoController {

    @Value("${spring.application.name:SoftCorpBackend}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.description:API del sistema de gestión de incidencias - Soft Corporation}")
    private String appDescription;

    // Endpoint raíz público para verificar disponibilidad rápida en local y Render
    @GetMapping("/")
    public Map<String, Object> obtenerInformacion() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", appName);
        info.put("description", appDescription);
        info.put("version", appVersion);
        info.put("status", "UP");
        info.put("message", "Backend funcionando correctamente");
        return info;
    }
}
