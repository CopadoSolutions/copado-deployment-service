package copado.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter based on header tokens.
 */
@Slf4j
public class WebSecurityFilter  extends OncePerRequestFilter {

    public static final String X_FORWARDED_PROTO = "x-forwarded-proto";

    /**
     * Just accepts request with "token" as header or parameter. The token must be valid.
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Check if it is a secure request and redirect to https if it is not.
        if (request.getHeader(X_FORWARDED_PROTO) != null) {
            if (request.getHeader(X_FORWARDED_PROTO).indexOf("https") != 0) {
                String pathInfo = (request.getPathInfo() != null) ? request.getPathInfo() : "";
                response.sendRedirect("https://" + request.getServerName() + pathInfo);
                log.info("Redirecting to https...");
                return;
            }
        }

        // Get user info
        final String token = StringUtils.isNotBlank(request.getHeader("token")) ? request.getHeader("token") : request.getParameter("token");

        log.info("Custom filter shielding mapping: {}", request.getRequestURI());

        if (StringUtils.isNotBlank(token)) {

            Authentication auth = new AuthenticationCopado(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
            filterChain.doFilter(request, response);

        } else {

            log.error("Unauthorized request. No credentials provided.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }
}
