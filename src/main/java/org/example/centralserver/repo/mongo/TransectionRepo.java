package org.example.centralserver.repo.mongo;

import jakarta.transaction.Transaction;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransectionRepo extends MongoRepository<Transection, String> {

    long countBySuspiciousTrue();

    List<Transection> findBySender_User_IdOrReceiver_User_Id(String senderId, String receiverId);



}
