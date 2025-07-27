package com.upsplay.api;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Result;
import org.neo4j.driver.Record; // Importar Record

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.TimeUnit; // Importar TimeUnit

@ApplicationScoped
public class Neo4jConnection {

    private static final String URI = "neo4j://127.0.0.1:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "admin2025"; // ¡Tu contraseña de Neo4j Desktop!

    private Driver driver;

    public Neo4jConnection() {
        Config config = Config.builder().withMaxConnectionLifetime(30, TimeUnit.MINUTES).build(); // Usar TimeUnit
        this.driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD), config);

        try (Session session = driver.session(SessionConfig.forDatabase("ups-play"))) {
            Result resultComponents = session.run("CALL dbms.components() YIELD name, versions, edition RETURN name, versions, edition");

 
            if (resultComponents.hasNext()) {
                Record componentRecord = resultComponents.next(); // Cambiado a .next() para tomar el primer registro
                List<Object> versionsList = componentRecord.get("versions").asList();
                String versionString = versionsList.isEmpty() ? "Desconocida" : versionsList.get(0).toString();

            } else {
                session.run("RETURN 1");
            }

            System.out.println("DEBUG NEO4J: Conexión a Neo4j establecida con éxito.");

        } catch (Exception e) {
            System.err.println("ERROR NEO4J: Fallo al conectar o verificar Neo4j: " + e.getMessage());
            if (driver != null) {
                driver.close();
                driver = null;
            }
            throw new RuntimeException("Fallo crítico al inicializar conexión con Neo4j. Verifica credenciales y servidor.", e);
        }
    }

    @Produces
    @ApplicationScoped
    public Driver getDriver() {
        return driver;
    }

    @PreDestroy
    public void closeDriver() {
        if (driver != null) {
            driver.close();
            System.out.println("DEBUG NEO4J: Driver de Neo4j cerrado.");
        }
    }
}