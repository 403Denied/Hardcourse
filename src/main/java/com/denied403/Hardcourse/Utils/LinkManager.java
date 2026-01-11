package com.denied403.Hardcourse.Utils;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.denied403.Hardcourse.Hardcourse.checkpointDatabase;

public class LinkManager {
    private final SecureRandom random = new SecureRandom();
    private final Map<String, UUID> codeToUUID = new ConcurrentHashMap<>();
    private final Map<UUID, String> UUIDToCode = new ConcurrentHashMap<>();

    public String generateCode(){
        int num = random.nextInt(900000) + 100000;
        return String.valueOf(num);
    }
    public String createLinkCode(UUID uuid){
        if(checkpointDatabase.isLinked(uuid)) return null;
        if(UUIDToCode.containsKey(uuid)){
            String old = UUIDToCode.remove(uuid);
            codeToUUID.remove(old);
        }
        String code = generateCode();
        UUIDToCode.put(uuid, code);
        codeToUUID.put(code, uuid);
        return code;
    }
    public UUID getUUIDFromCode(String code){
        return codeToUUID.get(code);
    }
    public void clearCode(UUID uuid){
        if(UUIDToCode.containsKey(uuid)){
            String code =  UUIDToCode.remove(uuid);
            codeToUUID.remove(code);
        }
    }
}
