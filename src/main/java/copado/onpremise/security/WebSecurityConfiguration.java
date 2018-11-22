package copado.onpremise.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationProviderCopado authenticationProviderCopado;


    @Autowired
    private AuthenticationRestEntryPoint authenticationRestEntryPoint;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                /* All requests are protected by default */
                .antMatcher("/**").authorizeRequests()

                /* All other end-points require an authenticated user */
                .anyRequest().authenticated().and()

                /* Unauthenticated users are re-directed to the home page */
                .exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/")).and()

                .logout().logoutSuccessUrl("/").permitAll()

                /* CRSF Token for session */
                .and().csrf().disable().rememberMe().disable()

                .addFilterBefore(new WebSecurityFilter(), BasicAuthenticationFilter.class)

                .exceptionHandling().authenticationEntryPoint(authenticationRestEntryPoint)
        ;
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authenticationProviderCopado);
    }
}
