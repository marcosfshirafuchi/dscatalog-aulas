package com.devsuprior.dscatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Indica que esta classe é uma configuração do Spring
public class SecurityConfig {

    @Bean // Define que o método retorna um Bean gerenciado pelo Spring
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

                // 🔐 CSRF (Cross-Site Request Forgery)
                // Desabilita a proteção CSRF
                // 👉 Necessário em APIs REST (especialmente para testes com Postman)
                // 👉 Caso contrário, requisições POST, PUT e DELETE podem retornar 403 Forbidden
                .csrf(csrf -> csrf.disable())

                // 🔓 AUTORIZAÇÃO DAS REQUISIÇÕES
                // Define as regras de acesso da aplicação
                .authorizeHttpRequests(auth -> auth

                        // Permite TODAS as requisições sem autenticação
                        // 👉 Usado em ambiente de desenvolvimento/teste
                        .anyRequest().permitAll()
                )

                // 🧱 HEADERS DE SEGURANÇA
                .headers(headers -> headers

                        // Permite que páginas sejam carregadas dentro de frames (iframe)
                        // 👉 Necessário para o H2 Console funcionar corretamente
                        // 👉 Sem isso, pode ocorrer erro: "A conexão com localhost foi recusada"
                        .frameOptions(frame -> frame.sameOrigin())
                );

        // Constrói e retorna a configuração de segurança
        return http.build();
    }
}