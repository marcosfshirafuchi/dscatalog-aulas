package com.devsuperior.dscatalog.services.validation;

import java.util.ArrayList;
import java.util.List;

import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.devsuperior.dscatalog.dto.UserInsertDTO;
import com.devsuperior.dscatalog.resources.exceptions.FieldMessage;
import org.springframework.beans.factory.annotation.Autowired;

// UserInsertValidator implementa ConstraintValidator para validar a anotação UserInsertValid
// e o objeto UserInsertDTO. Esta classe é responsável por validar regras de negócio específicas
// antes da inserção de um novo usuário.
public class UserInsertValidator implements ConstraintValidator<UserInsertValid, UserInsertDTO> {

    // @Autowired: Injeta o UserRepository para acessar o banco de dados e verificar a existência de usuários.
    @Autowired
    private UserRepository repository;

    // Método de inicialização do validador. Pode ser usado para configurar o validador com base na anotação.
    @Override
    public void initialize(UserInsertValid ann) {
        // Não há inicialização específica necessária para esta validação.
    }

    // Método principal de validação. Contém a lógica para verificar se o UserInsertDTO é válido.
    @Override
    public boolean isValid(UserInsertDTO dto, ConstraintValidatorContext context) {

        // Lista para armazenar mensagens de erro de validação personalizadas.
        List<FieldMessage> list = new ArrayList<>();

        // Busca um usuário no banco de dados com o email fornecido no DTO.
        User user = repository.findByEmail(dto.getEmail());

        // Verifica se um usuário com o email fornecido já existe.
        // Se existir, significa que o email já está em uso e adiciona uma mensagem de erro.
        if(user != null){
            list.add(new FieldMessage("email", "Email já existe"));
        }

        // Adiciona as mensagens de erro personalizadas à ConstraintValidatorContext.
        // Isso permite que o Spring MVC capture esses erros e os inclua na resposta da API.
        for (FieldMessage e : list) {
            context.disableDefaultConstraintViolation(); // Desabilita a mensagem de erro padrão.
            context.buildConstraintViolationWithTemplate(e.getMessage()).addPropertyNode(e.getFieldName())
                    .addConstraintViolation(); // Adiciona a mensagem de erro personalizada ao campo específico.
        }
        // Retorna true se a lista de erros estiver vazia (validação bem-sucedida), false caso contrário.
        return list.isEmpty();
    }
}