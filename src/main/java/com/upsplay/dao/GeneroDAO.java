package com.upsplay.dao;

import com.upsplay.model.Genero;
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
public class GeneroDAO {

    @Inject
    private Driver neo4jDriver;

    private static final String DATABASE_NAME = "ups-play"; // <-- ¡REEMPLAZA!

    public Genero saveGenero(Genero genero) {
        if (genero.getUuid() == null || genero.getUuid().isEmpty()) {
            genero.setUuid(UUID.randomUUID().toString());
        }
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "CREATE (g:Genero {uuid: $uuid, nombre: $nombre}) RETURN g";
            Record record = session.run(cypherQuery, Map.of("uuid", genero.getUuid(), "nombre", genero.getNombre())).single();
            return mapRecordToGenero(record.get("g").asMap());
        } catch (Exception e) {
            System.err.println("Error al guardar género en Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudo guardar el género.", e);
        }
    }

    public List<Genero> getAllGeneros() {
        List<Genero> generos = new ArrayList<>();
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "MATCH (g:Genero) RETURN g ORDER BY g.nombre ASC";
            Result result = session.run(cypherQuery);
            while (result.hasNext()) {
                generos.add(mapRecordToGenero(result.next().get("g").asMap()));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener todos los géneros de Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudieron obtener los géneros.", e);
        }
        return generos;
    }

    public Genero getGeneroByUuid(String uuid) {
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "MATCH (g:Genero {uuid: $uuid}) RETURN g";
            Result result = session.run(cypherQuery, Map.of("uuid", uuid));
            return result.hasNext() ? mapRecordToGenero(result.single().get("g").asMap()) : null;
        } catch (Exception e) {
            System.err.println("Error al obtener género por UUID en Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudo obtener el género por UUID.", e);
        }
    }

    public Genero updateGenero(Genero genero) {
        if (genero.getUuid() == null || genero.getUuid().isEmpty()) {
            throw new IllegalArgumentException("UUID del género es requerido para la actualización.");
        }
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "MATCH (g:Genero {uuid: $uuid}) SET g.nombre = $nombre RETURN g";
            Result result = session.run(cypherQuery, Map.of("uuid", genero.getUuid(), "nombre", genero.getNombre()));
            return result.hasNext() ? mapRecordToGenero(result.single().get("g").asMap()) : null;
        } catch (Exception e) {
            System.err.println("Error al actualizar género en Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudo actualizar el género.", e);
        }
    }

    public boolean deleteGenero(String uuid) {
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "MATCH (g:Genero {uuid: $uuid}) DETACH DELETE g RETURN count(g) > 0 AS deleted";
            Record record = session.run(cypherQuery, Map.of("uuid", uuid)).single();
            return record.get("deleted").asBoolean();
        } catch (Exception e) {
            System.err.println("Error al eliminar género en Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudo eliminar el género.", e);
        }
    }

    private Genero mapRecordToGenero(Map<String, Object> properties) {
        Genero genero = new Genero();
        genero.setUuid((String) properties.getOrDefault("uuid", null));
        genero.setNombre((String) properties.getOrDefault("nombre", null));
        return genero;
    }
}