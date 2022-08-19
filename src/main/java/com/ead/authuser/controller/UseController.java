package com.ead.authuser.controller;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/v1/users")
public class UseController {

    UserService userService;

    public UseController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserModel>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.findAll());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getOneUser(@PathVariable (value = "userId") UUID userId) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(userModelOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable (value = "userId") UUID userId) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isPresent()) {
            userService.deleteById(userId);
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
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isPresent()) {
            var userModel = userModelOptional.get();
            userModel.setFullname(userDto.getFullname());
            userModel.setPhoneNumber(userDto.getPhoneNumber());
            userModel.setCpf(userDto.getCpf());
            userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

            userService.save(userModel);
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
                return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully");
            } else {
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
            return ResponseEntity.status(HttpStatus.OK).body(userModel);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
}

