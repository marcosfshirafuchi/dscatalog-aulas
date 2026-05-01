package com.devsuperior.dscatalog.services.validation;

import com.devsuperior.dscatalog.dto.UserUpdateDTO;
import com.devsuperior.dscatalog.entities.User;
import com.devsuperior.dscatalog.repositories.UserRepository;
import com.devsuperior.dscatalog.resources.exceptions.FieldMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// UserUpdateValidator implementa ConstraintValidator para validar a anotação UserUpdateValid
// e o objeto UserUpdateDTO.
public class UserUpdateValidator implements ConstraintValidator<UserUpdateValid, UserUpdateDTO> {

    // @Autowired: Injeta o HttpServletRequest para acessar informações da requisição HTTP.
    // Isso é necessário para obter o ID do usuário da URI da requisição.
    @Autowired
    private HttpServletRequest request;

    // @Autowired: Injeta o UserRepository para acessar o banco de dados e verificar a existência de usuários.
    @Autowired
    private UserRepository repository;

    // Método de inicialização do validador. Pode ser usado para configurar o validador com base na anotação.
    @Override
    public void initialize(UserUpdateValid ann) {
        // Não há inicialização específica necessária para esta validação.
    }

    // Método principal de validação. Contém a lógica para verificar se o UserUpdateDTO é válido.
    @Override
    public boolean isValid(UserUpdateDTO dto, ConstraintValidatorContext context) {

        // Obtém as variáveis de template da URI (ex: {id} em /users/{id}).
        // O @SuppressWarnings é usado para ignorar o aviso de cast não verificado.
        @SuppressWarnings("unchecked")
        var uriVars = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        // Converte o ID do usuário da URI para Long.
        long userId = Long.parseLong(uriVars.get("id"));

        // Lista para armazenar mensagens de erro de validação personalizadas.
        List<FieldMessage> list = new ArrayList<>();

        // Busca um usuário no banco de dados com o email fornecido no DTO.
        User user = repository.findByEmail(dto.getEmail());

        // Verifica se um usuário com o email fornecido já existe E se o ID desse usuário é diferente do ID do usuário que está sendo atualizado.
        // Isso impede que um usuário altere seu email para um email já usado por outro usuário,
        // mas permite que ele mantenha seu próprio email sem erro de duplicidade.
        if(user != null && userId != user.getId()){
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