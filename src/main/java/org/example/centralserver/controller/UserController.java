package org.example.centralserver.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.centralserver.entities.DTO.UserDTO;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.entities.User;
import org.example.centralserver.repo.mongo.TransectionRepo;
import org.example.centralserver.repo.mongo.UserRepo;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/api/users")
public class UserController {

    private final UserRepo userRepo;
    private final TransectionRepo transectionRepo;

    public UserController(UserRepo userRepo, TransectionRepo transectionRepo) {
        this.userRepo = userRepo;
        this.transectionRepo = transectionRepo;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUser() throws JsonProcessingException {

        List<UserDTO> userDTOList = new ArrayList<>();


        List<User>users=userRepo.findAll();

        for(User user:users) {

            List<Transection>transectionList=transectionRepo.findBySender_User_IdOrReceiver_User_Id(user.getId(),user.getId());
            UserDTO userDTO=new UserDTO();
            userDTO.setTransections(transectionList);
            userDTO.setUser(user);
            userDTOList.add(userDTO);
        }

        return ResponseEntity.ok(userDTOList);
    }


}
