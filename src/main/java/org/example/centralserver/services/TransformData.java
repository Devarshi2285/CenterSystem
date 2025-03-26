package org.example.centralserver.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.centralserver.entities.Account;
import org.example.centralserver.entities.Transection;
import org.example.centralserver.entities.TransectionUser;
import org.example.centralserver.entities.User;
import org.example.centralserver.entities.config.*;
import org.example.centralserver.repo.mongo.BankConfigRepo;
import org.example.centralserver.repo.mongo.TransectionRepo;
import org.neo4j.driver.TransactionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TransformData {

    @Autowired
    BankConfig bankConfig;

    @Autowired
    BankConfigRepo bankConfigRepo;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    TransectionRepo transectionRepo;

    private boolean isEmptyConfig(String configValue) {
        return configValue == null || configValue.isEmpty() || configValue.equalsIgnoreCase("none");
    }


    public List<Transection> convertAndProcessData(BankConfig bankConfig) {


        JsonNode rawTransaction = fetchRawData(bankConfig.getTransactionURI());
        List<Transection> transactions = processTransactions(bankConfig,rawTransaction);

        return transactions;

    }
    private JsonNode fetchRawData(String uri) {
        try {
            String response = restTemplate.getForObject(uri, String.class);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("Error fetching data", e);
        }
    }

    public List<Transection> processTransactions(BankConfig config, JsonNode rawData) {

        List<Transection> transformedTransactions = new ArrayList<>();

        if (rawData.isArray()) {
            // Handle array of transactions
            for (JsonNode transactionNode : rawData) {
                transformedTransactions.add(transformTransaction(transactionNode, config));
            }
        } else {
            // Handle single transaction
            transformedTransactions.add(transformTransaction(rawData, config));
        }

        return transformedTransactions;
    }

    private Transection transformTransaction(JsonNode rawData, BankConfig config) {
        Transection Transection = new Transection();
        TransectionConfig txConfig = config.getTransactionConfig();

        mapFields(Transection, rawData, Map.of(
                "id", txConfig.getId(),
                "amt", txConfig.getAmt(),
                "currency", txConfig.getCurrency(),
                "type", txConfig.getType(),
                "description", txConfig.getDescription(),
                "balanceAfterTransection", txConfig.getBalanceAfterTransection(),
                "createdDate", txConfig.getCreatedDate()
        ));

        // Transform sender and receiver
        String senderPath = txConfig.getSender();
        String receiverPath = txConfig.getReceiver();

        JsonNode senderNode = getNodeByPath(rawData, senderPath);
        JsonNode receiverNode = getNodeByPath(rawData, receiverPath);

        if (senderNode != null) {
            Transection.setSender(transformParty(senderNode, config));
        }

        if (receiverNode != null) {
            Transection.setReceiver(transformParty(receiverNode, config));
        }

        return Transection;
    }

    private TransectionUser transformParty(JsonNode partyNode, BankConfig config) {
        TransectionUser TransectionUser = new TransectionUser();
        TransectionUserConfig userConfig = config.getTransectionUserConfig();

        System.out.println(userConfig.toString());
        // Map party fields using configuration
        mapFields(TransectionUser, partyNode, Map.of(
                "bankName", userConfig.getBankName(),
                "ifsc", userConfig.getIfsc(),
                "branchName", userConfig.getBranchName()
        ));

        // Transform nested user data
        JsonNode userNode = getNodeByPath(partyNode, userConfig.getUser());
        if (userNode != null) {
            TransectionUser.setUser(transformUser(userNode, config.getUserConfig()));
        }

        // Transform nested account data
        JsonNode accountNode = getNodeByPath(partyNode, userConfig.getAccount());
        if (accountNode != null) {
            TransectionUser.setAccount(transformAccount(accountNode, config.getAccountConfig()));
        }

        return TransectionUser;
    }

    private User transformUser(JsonNode userNode, UserConfig config) {
        User User = new User();

        // Map user fields using configuration
        mapFields(User, userNode, Map.of(
                "id", config.getId(),
                "name", config.getName(),
                "govIdNum", config.getGovIdNum(),
                "email", config.getEmail(),
                "mobileNumber", config.getMobileNumber(),
                "idType", config.getIdType(),
                "address", config.getAddress()
        ));

        return User;
    }

    private Account transformAccount(JsonNode accountNode, AccountConfig config) {
        Account Account = new Account();

        // Map account fields using configuration
        mapFields(Account, accountNode, Map.of(
                "id", config.getId(),
                "balance", config.getBalance(),
                "accountNumber",config.getAccountNumber(),
                "type", config.getType(),
                "user", config.getUser(),
                "nominees",config.getNominees()
        ));

        return Account;
    }

    /**
     * Generic method to map fields based on configuration
     */
    private void mapFields(Object target, JsonNode source, Map<String, String> fieldMappings) {
        try {
            for (Map.Entry<String, String> mapping : fieldMappings.entrySet()) {
                String targetField = mapping.getKey();
                String sourcePath = mapping.getValue();

                // Check if the config mapping is empty/null/none
                if (isEmptyConfig(sourcePath)) {
                    Field field = target.getClass().getDeclaredField(targetField);
                    field.setAccessible(true);
                    field.set(target, null);
                    continue;
                }

                JsonNode valueNode = getNodeByPath(source, sourcePath);
                if (valueNode != null && !valueNode.isNull()) {
                    Field field = target.getClass().getDeclaredField(targetField);
                    field.setAccessible(true);

                    Object value = convertNodeToFieldType(valueNode, field.getType());
                    field.set(target, value);
                } else {
                    // If path exists in config but value not found in source, set null
                    Field field = target.getClass().getDeclaredField(targetField);
                    field.setAccessible(true);
                    field.set(target, null);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("Error mapping fields", e);
        }
    }


    /**
     * Converts JsonNode value to appropriate Java type
     */
    private Object convertNodeToFieldType(JsonNode node, Class<?> targetType) {
        if (targetType == String.class) {
            return node.asText();
        } else if (targetType == Double.class || targetType == double.class) {
            return node.asDouble();
        } else if (targetType == Integer.class || targetType == int.class) {
            return node.asInt();
        } else if (targetType == LocalDateTime.class) {
            return LocalDateTime.parse(node.asText());
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            return node.asBoolean();
        }
        // Add more type conversions as needed
        return node.asText();
    }

    private String getValueByPath(JsonNode node, String path) {
        JsonNode result = getNodeByPath(node, path);
        return result != null ? result.asText() : null;
    }

    private JsonNode getNodeByPath(JsonNode node, String path) {
        if (path == null || path.isEmpty()) return null;

        String[] parts = path.split("\\.");
        JsonNode current = node;

        for (String part : parts) {
            if (current == null) return null;
            current = current.get(part);
        }

        return current;
    }
}
