package org.example.centralserver.repo.neo4j;

import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.AccountNeo4J;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface AccountNeo4jRepository extends Neo4jRepository<AccountNeo4J, String> {
}
