package com.ead.authuser.controller;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/users")
public class UseController {

    UserService userService;

    public UseController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(SpecificationTemplate.UserSpec spec,
                                                       @PageableDefault(page =0,
                                                                        size = 10,
                                                                        sort ="userId",
                                                                        direction = Sort.Direction.ASC) Pageable pageable) {
        Page<UserModel> userModelPage = userService.findAll(pageable, spec);
        if (!userModelPage.isEmpty()) {
            for (UserModel user : userModelPage.toList()) {
                user.add(linkTo(methodOn(UseController.class).getOneUser(user.getUserId())).withSelfRel());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(userModelPage);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getOneUser(@PathVariable (value = "userId") UUID userId) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        return userModelOptional.<ResponseEntity<Object>>
                map(userModel -> ResponseEntity.status(HttpStatus.OK).body(userModel))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));

        // MODELO ANTIGO
     /* if (userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(userModelOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } */
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable (value = "userId") UUID userId) {

        log.debug("PUT deleteUser userId received {}", userId);
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isPresent()) {
            userService.deleteById(userId);

            log.debug("DELETE deleteUser userId {}", userId);
            log.info("DELETE User deleted successfully userId {}", userId);
            return ResponseEntity.status(HttpStatus.OK).body("User deleted");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Object> updateUser(@PathVariable (value = "userId") UUID userId,
                                             @RequestBody
                                             @Validated(UserDto.UserView.UserPut.class)
                                             @JsonView(UserDto.UserView.UserPut.class) UserDto userDto) {
        log.debug("PUT updateUser userDto received {}", userDto.toString());
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isPresent()) {
            var userModel = userModelOptional.get();
            userModel.setFullname(userDto.getFullname());
            userModel.setPhoneNumber(userDto.getPhoneNumber());
            userModel.setCpf(userDto.getCpf());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

            userService.save(userModel);

            log.debug("PUT updateUser userModel saved {}", userModel.toString());
            log.info("PUT User updated successfully userId {}", userModel.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(userModel);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Object> updatePassword(@PathVariable (value = "userId") UUID userId,
                                             @RequestBody
                                             @Validated(UserDto.UserView.PasswordPut.class)
                                             @JsonView(UserDto.UserView.PasswordPut.class) UserDto userDto) {

        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isPresent()) {
            if (!userModelOptional.get().getPassword().equals(userDto.getPassword())) {
                var userModel = userModelOptional.get();
                userModel.setPassword(userDto.getPassword());
                userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

                userService.save(userModel);

                log.debug("PUT userPassword saved ");
                log.info("PUT Password updated successfully");
                return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully");
            } else {
                log.warn("Mismatched old password userId {}", userDto.getId());
                return ResponseEntity.status(HttpStatus.CONFLICT).body("ERROR: Mismatched old password");
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PutMapping("/{userId}/image")
    public ResponseEntity<Object> updateImage(@PathVariable (value = "userId") UUID userId,
                                                @RequestBody
                                                @Validated(UserDto.UserView.ImagePut.class)
                                                @JsonView(UserDto.UserView.ImagePut.class) UserDto userDto) {

        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isPresent()) {
            var userModel = userModelOptional.get();
            userModel.setImageUrl(userDto.getImageUrl());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

            userService.save(userModel);

            log.debug("PUT updateImage userModel saved {}", userModel.toString());
            log.info("PUT Image updated successfully userId {}", userModel.getUserId());
            return ResponseEntity.status(HttpStatus.OK).body(userModel);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}

