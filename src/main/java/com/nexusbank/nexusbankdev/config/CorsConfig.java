package com.nexusbank.nexusbankdev.config;

import com.nexusbank.nexusbankdev.model.Employee;
import com.nexusbank.nexusbankdev.model.Session;
import com.nexusbank.nexusbankdev.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Autowired
    @Lazy
    private AuthService authService;

    @Value("${nexusbank.auth.enforce:true}")
    private boolean enforceAuth;

    @Value("${nexusbank.cors.allowed-origins:*}")
    private String[] allowedOrigins;

    // Endpoints that work without a token
    private static final Pattern PUBLIC = Pattern.compile(
            "^/api/auth/(login/employee|login/customer|verify-otp|register/customer)$");

    // Endpoints only staff (employee sessions) may call
    private static final Pattern STAFF_ONLY = Pattern.compile(
            "^/api/(employees|security|audit-logs|dashboard|tickets|ledger)(/.*)?$"
                    + "|^/api/customers/search$"
                    + "|^/api/kyc/pending$"
                    + "|^/api/kyc/\\d+/(approve|reject)$"
                    + "|^/api/loans/(high-risk-queue|all|thresholds)$"
                    + "|^/api/loans/\\d+/(review|disburse)$"
                    + "|^/api/accounts/\\d+/(freeze|unfreeze)$"
                    + "|^/api/transactions/\\d+/verify$");

    // A customer may only read their own records
    private static final Pattern OWN_RECORD = Pattern.compile(
            "^/api/(?:accounts/customer|cards/customer|loans/customer|customers)/(\\d+)$");

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws IOException {
                if (!enforceAuth || "OPTIONS".equalsIgnoreCase(req.getMethod())) return true;

                String path = req.getRequestURI();
                if (PUBLIC.matcher(path).matches()) return true;

                String header = req.getHeader("Authorization");
                String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7).trim() : null;

                Session session = authService.validateToken(token);
                if (session == null) {
                    return reject(res, 401, "Not authenticated or session expired");
                }

                Employee emp = null;
                if (session.getEmployeeId() != null) {
                    emp = authService.findActiveEmployee(session.getEmployeeId());
                    if (emp == null) {
                        return reject(res, 401, "Account is no longer active");
                    }
                    req.setAttribute("auth.employee", emp);
                    req.setAttribute("auth.employeeId", emp.getEmployeeId());
                    req.setAttribute("auth.role", emp.getRole());
                }
                req.setAttribute("auth.session", session);

                boolean staffOnly = STAFF_ONLY.matcher(path).matches()
                        || ("POST".equalsIgnoreCase(req.getMethod()) && path.equals("/api/billers"));
                if (staffOnly && emp == null) {
                    return reject(res, 403, "Staff access only");
                }

                Matcher own = OWN_RECORD.matcher(path);
                if (emp == null && own.matches()
                        && (session.getCustomerId() == null || !session.getCustomerId().toString().equals(own.group(1)))) {
                    return reject(res, 403, "You can only access your own records");
                }
                return true;
            }
        }).addPathPatterns("/api/**");
    }

    private static boolean reject(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.getWriter().write("{\"message\":\"" + message + "\"}");
        return false;
    }
}