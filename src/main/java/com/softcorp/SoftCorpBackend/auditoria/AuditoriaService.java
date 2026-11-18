package com.softcorp.SoftCorpBackend.auditoria;

import com.softcorp.SoftCorpBackend.model.auditoria.Cambio;
import com.softcorp.SoftCorpBackend.model.auditoria.Evento;
import com.softcorp.SoftCorpBackend.repository.auditoria.CambioRepository;
import com.softcorp.SoftCorpBackend.repository.auditoria.EventoRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final EventoRepository eventoRepository;
    private final CambioRepository cambioRepository;

    @Autowired(required = false)
    private HttpServletResponse httpResponse;

    public AuditoriaService(EventoRepository eventoRepository,
                            CambioRepository cambioRepository) {
        this.eventoRepository = eventoRepository;
        this.cambioRepository = cambioRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarCreacion(String esquema, String tabla,
                                  String idRegistro, String modulo) {
        Evento evento = construirEvento(esquema, tabla, idRegistro, modulo, "INSERT");
        eventoRepository.save(evento);
        log.debug("[Auditoria] INSERT registrado - tabla={}.{} id={}", esquema, tabla, idRegistro);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarActualizacion(String esquema, String tabla,
                                       String idRegistro, String modulo,
                                       Object estadoAnterior, Object estadoNuevo) {
        Evento evento = construirEvento(esquema, tabla, idRegistro, modulo, "UPDATE");
        eventoRepository.save(evento);
        List<Cambio> cambios = compararCampos(evento, estadoAnterior, estadoNuevo);
        if (!cambios.isEmpty()) cambioRepository.saveAll(cambios);
        log.debug("[Auditoria] UPDATE registrado - tabla={}.{} id={} cambios={}",
                esquema, tabla, idRegistro, cambios.size());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEliminacion(String esquema, String tabla,
                                     String idRegistro, String modulo,
                                     Object estadoAnterior) {
        Evento evento = construirEvento(esquema, tabla, idRegistro, modulo, "DELETE");
        eventoRepository.save(evento);
        List<Cambio> cambios = capturarEstado(evento, estadoAnterior);
        if (!cambios.isEmpty()) cambioRepository.saveAll(cambios);
        log.debug("[Auditoria] DELETE registrado - tabla={}.{} id={} campos={}",
                esquema, tabla, idRegistro, cambios.size());
    }

    private Evento construirEvento(String esquema, String tabla,
                                   String idRegistro, String modulo,
                                   String operacion) {
        return Evento.builder()
                .nombreEsquema(esquema)
                .nombreTabla(tabla)
                .idRegistro(idRegistro)
                .modulo(modulo)
                .operacion(operacion)
                .fechaEvento(OffsetDateTime.now())
                .resultado("SUCCESS")
                .idUsuario(nullSafeLong(AuditoriaContext.getIdUsuario()))
                .ipOrigen(AuditoriaContext.getIpOrigen())
                .metodoHttp(AuditoriaContext.getMetodoHttp())
                .endpoint(AuditoriaContext.getEndpoint())
                .codigoHttp(httpResponse != null ? httpResponse.getStatus() : null)
                .idInteraccion(AuditoriaContext.getIdInteraccion())
                .versionAplicacion(AuditoriaContext.getVersionAplicacion())
                .build();
    }

    private List<Cambio> compararCampos(Evento evento, Object anterior, Object nuevo) {
        List<Cambio> cambios = new ArrayList<>();
        if (anterior == null || nuevo == null) return cambios;
        Class<?> clazz = anterior.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                try {
                    Object va = field.get(anterior);
                    Object vn = field.get(nuevo);
                    if (!Objects.equals(va, vn)) {
                        cambios.add(Cambio.builder()
                                .evento(evento)
                                .nombreCampo(field.getName())
                                .valorAnterior(toStr(va))
                                .valorNuevo(toStr(vn))
                                .build());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("[Auditoria] No se pudo acceder al campo '{}': {}", field.getName(), e.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }
        return cambios;
    }

    private List<Cambio> capturarEstado(Evento evento, Object objeto) {
        List<Cambio> cambios = new ArrayList<>();
        if (objeto == null) return cambios;
        Class<?> clazz = objeto.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                field.setAccessible(true);
                try {
                    cambios.add(Cambio.builder()
                            .evento(evento)
                            .nombreCampo(field.getName())
                            .valorAnterior(toStr(field.get(objeto)))
                            .valorNuevo(null)
                            .build());
                } catch (IllegalAccessException e) {
                    log.warn("[Auditoria] No se pudo acceder al campo '{}': {}", field.getName(), e.getMessage());
                }
            }
            clazz = clazz.getSuperclass();
        }
        return cambios;
    }

    private String toStr(Object obj) {
        if (obj == null) return null;
        return obj.toString().replaceAll("passwordHash=[^,)]+", "passwordHash=[PROTEGIDO]");
    }

    private long nullSafeLong(Long value) {
        return value != null ? value : 0L;
    }
}
