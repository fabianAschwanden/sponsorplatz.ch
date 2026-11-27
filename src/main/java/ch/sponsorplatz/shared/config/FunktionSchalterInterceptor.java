package ch.sponsorplatz.shared.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class FunktionSchalterInterceptor implements HandlerInterceptor, WebMvcConfigurer {
    private final FunktionSchalter schalter;

    public FunktionSchalterInterceptor(FunktionSchalter schalter) {
        this.schalter = schalter;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Funktion funktion = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), Funktion.class);
        if (funktion == null) {
            funktion = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), Funktion.class);
        }
        if (funktion == null) {
            return true;
        }

        for (FunktionSchalter.Bereich bereich : funktion.value()) {
            if (!schalter.istAktiv(bereich)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return false;
            }
        }
        return true;
    }
}
