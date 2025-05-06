package org.example.centralserver.repo.mongo;

import org.example.centralserver.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepo extends MongoRepository<User, String> {
}
