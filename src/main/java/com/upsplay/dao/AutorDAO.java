package com.upsplay.dao;

import com.upsplay.model.Autor;
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
public class AutorDAO {

	@Inject
	private Driver neo4jDriver;

	private static final String DATABASE_NAME = "ups-play"; // <-- ¡REEMPLAZA!

	public Autor saveAutor(Autor autor) {
		if (autor.getUuid() == null || autor.getUuid().isEmpty()) {
			autor.setUuid(UUID.randomUUID().toString());
		}
		try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
			String cypherQuery = "CREATE (a:Autor {uuid: $uuid, nombre: $nombre, paisOrigen: $paisOrigen, edad: $edad}) RETURN a";
			Map<String, Object> params = Map.of("uuid", autor.getUuid(), "nombre", autor.getNombre(), "paisOrigen",
					autor.getPaisOrigen(), "edad", autor.getEdad());
			Record record = session.run(cypherQuery, params).single();
			return mapRecordToAutor(record.get("a").asMap());
		} catch (Exception e) {
			System.err.println("Error al guardar autor en Neo4j: " + e.getMessage());
			throw new RuntimeException("No se pudo guardar el autor.", e);
		}
	}

	public List<Autor> getAllAutores() {
		List<Autor> autores = new ArrayList<>();
		try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
			// CAMBIO CLAVE AQUÍ: De Artista a Autor
			String cypherQuery = "MATCH (a:Autor) RETURN a ORDER BY a.nombre ASC";
			Result result = session.run(cypherQuery);
			while (result.hasNext()) {
				// CAMBIO CLAVE AQUÍ: Llama al método auxiliar con el nombre correcto
				// y asegúrate de que el 'a' es un Nodo (Value) antes de convertirlo a Map
				autores.add(mapValueToAutor(result.next().get("a").asNode().asMap()));
			}
		} catch (Exception e) {
			System.err.println("Error al obtener todos los autores de Neo4j: " + e.getMessage());
			throw new RuntimeException("No se pudieron obtener los autores.", e);
		}
		return autores;
	}

	public Autor getAutorByUuid(String uuid) {
		try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
			String cypherQuery = "MATCH (a:Artista {uuid: $uuid}) RETURN a";
			Result result = session.run(cypherQuery, Map.of("uuid", uuid));
			return result.hasNext() ? mapRecordToAutor(result.single().get("a").asMap()) : null;
		} catch (Exception e) {
			System.err.println("Error al obtener autor por UUID en Neo4j: " + e.getMessage());
			throw new RuntimeException("No se pudo obtener el autor por UUID.", e);
		}
	}

	public List<Autor> getColaboradoresByAutorUuid(String targetAutorUuid) {
		List<Autor> colaboradores = new ArrayList<>();
		try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
			String cypherQuery = "MATCH (targetAutor:Autor {uuid: $targetAutorUuid})<-[:ESCRITA_POR]-(c:Cancion)-[:ESCRITA_POR]->(collaborator:Autor) "
					+ "WHERE targetAutor <> collaborator "
					+ "RETURN DISTINCT collaborator.uuid AS uuid, collaborator.nombre AS nombre, collaborator.edad AS edad, collaborator.paisOrigen AS paisOrigen "
					+ "ORDER BY collaborator.nombre ASC";

			Result result = session.run(cypherQuery, Map.of("targetAutorUuid", targetAutorUuid));

			while (result.hasNext()) {
				Record record = result.next();
				Autor colaborador = new Autor();
				colaborador.setUuid(record.get("uuid").asString());
				colaborador.setNombre(record.get("nombre").asString());
				colaborador.setEdad(record.get("edad").asInt());
				colaborador.setPaisOrigen(record.get("paisOrigen").asString());
				colaboradores.add(colaborador);
			}
		} catch (Exception e) {
			System.err.println(
					"Error al obtener colaboradores para el autor con UUID " + targetAutorUuid + ": " + e.getMessage());
			throw new RuntimeException("No se pudieron obtener los colaboradores.", e);
		}
		return colaboradores;
	}

	private Autor mapValueToAutor(Map<String, Object> properties) {
		Autor autor = new Autor();
		autor.setUuid((String) properties.getOrDefault("uuid", null));
		autor.setNombre((String) properties.getOrDefault("nombre", null));
		autor.setPaisOrigen((String) properties.getOrDefault("paisOrigen", null));
		autor.setEdad(((Number) properties.getOrDefault("edad", 0)).intValue());
		return autor;
	}

	private Autor mapRecordToAutor(Map<String, Object> properties) {
		Autor autor = new Autor();
		autor.setUuid((String) properties.getOrDefault("uuid", null));
		autor.setNombre((String) properties.getOrDefault("nombre", null));
		autor.setPaisOrigen((String) properties.getOrDefault("paisOrigen", null));
		return autor;
	}
}