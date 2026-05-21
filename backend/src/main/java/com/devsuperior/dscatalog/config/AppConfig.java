package com.devsuperior.dscatalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// @Configuration: Indica que esta classe contém definições de beans e é uma fonte de configuração para o Spring.
// O Spring irá escanear esta classe para encontrar métodos anotados com @Bean.
@Configuration
public class AppConfig {

    // @Bean: Indica que o método produz um bean a ser gerenciado pelo contêiner Spring.
    // O nome do método (passwordEncoder) será o ID do bean, a menos que especificado de outra forma.
    // Este bean será injetado onde for necessário um PasswordEncoder.
    @Bean
    public PasswordEncoder passwordEncoder(){
        // Retorna uma nova instância de BCryptPasswordEncoder.
        // BCryptPasswordEncoder é uma implementação robusta de PasswordEncoder que usa o algoritmo de hash BCrypt.
        // É amplamente recomendado para armazenar senhas de forma segura, pois adiciona um "salt" aleatório
        // e realiza múltiplas rodadas de hashing, tornando-o resistente a ataques de força bruta e tabelas rainbow.
        return new BCryptPasswordEncoder();
    }
}