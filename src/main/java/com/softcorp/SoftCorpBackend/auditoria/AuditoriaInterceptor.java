package com.softcorp.SoftCorpBackend.auditoria;

import com.softcorp.SoftCorpBackend.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.UUID;

@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaInterceptor.class);

    @Value("${app.version:1.0.0}")
    private String appVersion;

    // Antes de entrar al controller, carga en ThreadLocal el usuario del JWT, IP real, endpoint y un UUID de correlación
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        AuditoriaContext.inicializar();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Usuario usuario) {
            AuditoriaContext.setIdUsuario(usuario.getId());
        } else {
            AuditoriaContext.setIdUsuario(0L);
        }

        // Con server.forward-headers-strategy=FRAMEWORK, Spring resuelve el proxy (Render)
        String ip = normalizarIp(request.getRemoteAddr());
        if ("::1".equals(ip) || "127.0.0.1".equals(ip)) {
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            } catch (java.net.UnknownHostException e) {
                // Conservar loopback si falla la resolución
            }
        }
        AuditoriaContext.setIpOrigen(ip);

        AuditoriaContext.setMetodoHttp(request.getMethod());
        AuditoriaContext.setEndpoint(request.getRequestURI());
        AuditoriaContext.setIdInteraccion(UUID.randomUUID());
        AuditoriaContext.setVersionAplicacion(appVersion);

        log.debug("[Auditoria] Context inicializado — usuario={} ip={} endpoint={}",
                AuditoriaContext.getIdUsuario(), ip, request.getRequestURI());

        return true;
    }

    // Quita sufijos de interfaz IPv6 (%...) y prefijos ::ffff: para que PostgreSQL acepte la IP en columna INET
    private String normalizarIp(String ip) {
        if (ip == null || ip.isBlank()) return ip;

        int zonaIdx = ip.indexOf('%');
        if (zonaIdx != -1) {
            ip = ip.substring(0, zonaIdx);
        }

        if (ip.toLowerCase().startsWith("::ffff:")) {
            return ip.substring(7);
        }

        try {
            InetAddress addr = InetAddress.getByName(ip);
            if (addr instanceof Inet4Address) {
                return addr.getHostAddress();
            }
            return addr.getHostAddress();
        } catch (java.net.UnknownHostException e) {
            return ip;
        }
    }

    // Siempre limpia el contexto al finalizar la petición (con éxito o error)
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        AuditoriaContext.limpiar();
    }
}
