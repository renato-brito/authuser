package com.ead.authuser.controller;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.enums.UserStatus;
import com.ead.authuser.enums.UserType;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser(@RequestBody
                                               @Validated(UserDto.UserView.RegistrationPost.class)
                                               @JsonView(UserDto.UserView.RegistrationPost.class) UserDto userDto) {
        log.debug("POST registerUser userDto received {}", userDto.toString());
        if(userService.existsByUserName(userDto.getUserName())) {
            log.warn("POST User name {} already exists", userDto.getUserName());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ERROR: User name already exists");
        }

        if(userService.existsByEmail(userDto.getEmail())) {
            log.warn("POST Email {} already exists", userDto.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ERROR: Email already exists");
        }

        var userModel = new UserModel();
        BeanUtils.copyProperties(userDto, userModel);
        userModel.setUserStatus(UserStatus.ACTIVE);
        userModel.setUserType(UserType.STUDENT);
        userModel.setCreationDate(LocalDateTime.now(ZoneId.of("UTC")));
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.save(userModel);

        log.debug("POST registerUser userModel saved {}", userModel.toString());
        log.info("POST User saved successfully userId {}", userModel.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(userModel);
    }

    @GetMapping("/")
    public String index() {
        log.trace("TRACE");    // utilizado para ter uma visualização mais fina do problema - traz detalhes
        log.debug("DEBUG");    // utilizado em ambiente de desenvolvimento (retirado em produção)
        log.info("INFO");      // utilizado em ambiente produtivo
        log.warn("WARN");      // um alerta
        log.error("ERROR");    // utilizado quando se tem erro na aplicação, usado em bloco try...catch

        // exemplo de lançamento de error
        try{
            throw new Exception("Exception message");
        }catch (Exception e) {
            log.error("-----ERROR-----", e);
        }
        return "Logging Spring Boot...";

    }
}
