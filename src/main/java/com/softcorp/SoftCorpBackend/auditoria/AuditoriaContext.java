package com.softcorp.SoftCorpBackend.auditoria;

import java.util.UUID;

// Contexto por hilo (ThreadLocal) para pasar IP, usuario y UUID desde el interceptor hasta los servicios
public final class AuditoriaContext {

    private static final ThreadLocal<AuditoriaContext> HOLDER = new ThreadLocal<>();

    private Long idUsuario;
    private String ipOrigen;
    private String metodoHttp;
    private String endpoint;
    private UUID idInteraccion;
    private String versionAplicacion;

    private AuditoriaContext() {}

    public static void inicializar() {
        HOLDER.set(new AuditoriaContext());
    }

    // Libera el ThreadLocal al terminar el request y evita fugas o datos cruzados entre hilos de Tomcat
    public static void limpiar() {
        HOLDER.remove();
    }

    public static Long getIdUsuario() {
        AuditoriaContext ctx = HOLDER.get();
        return ctx != null ? ctx.idUsuario : 0L;
    }

    public static String getIpOrigen() {
        AuditoriaContext ctx = HOLDER.get();
        return ctx != null ? ctx.ipOrigen : null;
    }

    public static String getMetodoHttp() {
        AuditoriaContext ctx = HOLDER.get();
        return ctx != null ? ctx.metodoHttp : null;
    }

    public static String getEndpoint() {
        AuditoriaContext ctx = HOLDER.get();
        return ctx != null ? ctx.endpoint : null;
    }

    public static UUID getIdInteraccion() {
        AuditoriaContext ctx = HOLDER.get();
        return ctx != null ? ctx.idInteraccion : null;
    }

    public static String getVersionAplicacion() {
        AuditoriaContext ctx = HOLDER.get();
        return ctx != null ? ctx.versionAplicacion : null;
    }

    public static void setIdUsuario(Long idUsuario) {
        AuditoriaContext ctx = HOLDER.get();
        if (ctx != null) ctx.idUsuario = idUsuario;
    }

    public static void setIpOrigen(String ipOrigen) {
        AuditoriaContext ctx = HOLDER.get();
        if (ctx != null) ctx.ipOrigen = ipOrigen;
    }

    public static void setMetodoHttp(String metodoHttp) {
        AuditoriaContext ctx = HOLDER.get();
        if (ctx != null) ctx.metodoHttp = metodoHttp;
    }

    public static void setEndpoint(String endpoint) {
        AuditoriaContext ctx = HOLDER.get();
        if (ctx != null) ctx.endpoint = endpoint;
    }

    public static void setIdInteraccion(UUID idInteraccion) {
        AuditoriaContext ctx = HOLDER.get();
        if (ctx != null) ctx.idInteraccion = idInteraccion;
    }

    public static void setVersionAplicacion(String versionAplicacion) {
        AuditoriaContext ctx = HOLDER.get();
        if (ctx != null) ctx.versionAplicacion = versionAplicacion;
    }
}
