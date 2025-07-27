package com.upsplay.dao;

import com.upsplay.model.Premio;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class PremioDAO {

    @Inject
    private Driver neo4jDriver;

    private static final String DATABASE_NAME = "ups-play"; // <-- ¡REEMPLAZA!

    public Premio savePremio(Premio premio) {
        if (premio.getUuid() == null || premio.getUuid().isEmpty()) {
            premio.setUuid(UUID.randomUUID().toString());
        }
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "CREATE (p:Premio {uuid: $uuid, nombrePremio: $nombrePremio, anio: $anio, categoria: $categoria}) RETURN p";
            Map<String, Object> params = Map.of(
                "uuid", premio.getUuid(),
                "nombrePremio", premio.getNombrePremio(),
                "anio", premio.getAnio(),
                "categoria", premio.getCategoria()
            );
            Record record = session.run(cypherQuery, params).single();
            return mapRecordToPremio(record.get("p").asMap());
        } catch (Exception e) {
            System.err.println("Error al guardar premio en Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudo guardar el premio.", e);
        }
    }

    public List<Premio> getAllPremios() {
        List<Premio> premios = new ArrayList<>();
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "MATCH (p:Premio) RETURN p ORDER BY p.nombrePremio ASC";
            Result result = session.run(cypherQuery);
            while (result.hasNext()) {
                premios.add(mapRecordToPremio(result.next().get("p").asMap()));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener todos los premios de Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudieron obtener los premios.", e);
        }
        return premios;
    }

    public Premio getPremioByUuid(String uuid) {
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "MATCH (p:Premio {uuid: $uuid}) RETURN p";
            Result result = session.run(cypherQuery, Map.of("uuid", uuid));
            return result.hasNext() ? mapRecordToPremio(result.single().get("p").asMap()) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener premio por UUID en Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudo obtener el premio por UUID.", e);
        }
    }

    public Premio updatePremio(Premio premio) {
        if (premio.getUuid() == null || premio.getUuid().isEmpty()) {
            throw new IllegalArgumentException("UUID del premio es requerido para la actualización.");
        }
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "MATCH (p:Premio {uuid: $uuid}) SET p.nombrePremio = $nombrePremio, p.anio = $anio, p.categoria = $categoria RETURN p";
            Map<String, Object> params = Map.of(
                "uuid", premio.getUuid(),
                "nombrePremio", premio.getNombrePremio(),
                "anio", premio.getAnio(),
                "categoria", premio.getCategoria()
            );
            Result result = session.run(cypherQuery, params);
            return result.hasNext() ? mapRecordToPremio(result.single().get("p").asMap()) : null;
        } catch (Exception e) {
            System.err.println("Error al actualizar premio en Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudo actualizar el premio.", e);
        }
    }

    public boolean deletePremio(String uuid) {
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "MATCH (p:Premio {uuid: $uuid}) DETACH DELETE p RETURN count(p) > 0 AS deleted";
            Record record = session.run(cypherQuery, Map.of("uuid", uuid)).single();
            return record.get("deleted").asBoolean();
        } catch (Exception e) {
            System.err.println("Error al eliminar premio en Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudo eliminar el premio.", e);
        }
    }

    private Premio mapRecordToPremio(Map<String, Object> properties) {
        Premio premio = new Premio();
        premio.setUuid((String) properties.getOrDefault("uuid", null));
        premio.setNombrePremio((String) properties.getOrDefault("nombrePremio", null));
        premio.setAnio(((Number) properties.getOrDefault("anio", 0)).intValue());
        premio.setCategoria((String) properties.getOrDefault("categoria", null));
        return premio;
    }
}