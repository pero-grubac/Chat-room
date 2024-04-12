package org.unibl.etfbl.ChatRoom.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.unibl.etfbl.ChatRoom.models.entities.UserEntity;
import org.unibl.etfbl.ChatRoom.repositories.UserEntityRepository;
import org.unibl.etfbl.ChatRoom.security.config.UserInfoService;

import java.io.IOException;
import java.util.Objects;

@Component

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserEntityRepository repository;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        if (StringUtils.isEmpty(authHeader) || !StringUtils.startsWith(authHeader, "Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(7);
        username = jwtService.extractUserName(jwt);
        if (StringUtils.isNotEmpty(username)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserEntity userEntity = userInfoService.loadUserByUsername(username);
            if (jwtService.isTokenValid(jwt, userEntity) && Objects.equals(jwt, userEntity.getJWT())) {
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userEntity, null, ((UserDetails) userEntity).getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            }
            else {
                userEntity.setJWT(null);
                repository.saveAndFlush(userEntity);
            }
        }
        for (String paramName : request.getParameterMap().keySet()) {
            String paramValue = request.getParameter(paramName);
          //  System.out.println("attributeName " + paramName);
        }
        filterChain.doFilter(request, response);
    }
}
