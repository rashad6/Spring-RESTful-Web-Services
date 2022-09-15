package com.raschad.app.ws.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.raschad.app.ws.io.repositories.UserRepository;
import com.raschad.app.ws.service.UserService;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurity {

	private final UserService userDetilsService;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final UserRepository userRepository;

	@Autowired
	public WebSecurity(UserService userDetailsService, BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository) {
		this.userDetilsService = userDetailsService;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.userRepository = userRepository;
	}

	@Bean
	protected SecurityFilterChain configure(HttpSecurity http) throws Exception {

		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);
		authenticationManagerBuilder.userDetailsService(userDetilsService).passwordEncoder(bCryptPasswordEncoder);
		
		AuthenticationManager authenticationManager = authenticationManagerBuilder.build();
        http.authenticationManager(authenticationManager);
		
		http
		.cors().and()
		.csrf().disable().authorizeRequests()
		.antMatchers(HttpMethod.POST, SecurityConstants.SIGN_UP_URL)
		.permitAll()
		.antMatchers(HttpMethod.GET, SecurityConstants.VERIFICATION_EMAIL_URL)
		.permitAll()
		.antMatchers(HttpMethod.POST, SecurityConstants.PASSWORD_RESET_REQUEST_URL)
		.permitAll()
		.antMatchers(HttpMethod.POST, SecurityConstants.PASSWORD_RESET_URL)
		.permitAll()
		.anyRequest().authenticated().and()
		.addFilter(getAuthenticationFilter(authenticationManager))
		.addFilter(new AuthorizationFilter(authenticationManager,userRepository))
		.sessionManagement()
		.sessionCreationPolicy(SessionCreationPolicy.STATELESS);

		return http.build();
	}

	protected AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        final AuthenticationFilter filter = new AuthenticationFilter(authenticationManager);
        filter.setFilterProcessesUrl("/users/login");
        return filter;
    }
}
