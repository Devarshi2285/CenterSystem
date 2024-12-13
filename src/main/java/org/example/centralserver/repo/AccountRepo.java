package org.example.centralserver.repo;

import org.example.centralserver.entities.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AccountRepo extends MongoRepository<Account, String> {

    Optional<Account> findByAccId(String accId);

}
