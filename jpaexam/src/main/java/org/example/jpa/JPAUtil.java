package org.example.jpa;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JPAUtil {
    private static final EntityManagerFactory emfInstance = Persistence.createEntityManagerFactory("UserPU");

    // Java 애플리케이션이 종료될때 자동으로 close()메소드가 호출되도록 구현
    static {
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            if(emfInstance != null){
                log.info("---emf close---");
                emfInstance.close();
            }
        }));
    }

    private JPAUtil(){};

    public static EntityManagerFactory getEntityManagerFactory(){
        return emfInstance;
    }
}
