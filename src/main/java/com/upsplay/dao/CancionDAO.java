package com.upsplay.dao;

import com.upsplay.model.*;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value; // Para manejar los valores de los registros
import org.neo4j.driver.Values;
import org.neo4j.driver.types.Node;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Date; // Para el mapeo de fechas
import java.util.HashMap; 
import org.neo4j.driver.Value;
import static org.neo4j.driver.Values.parameters;

@ApplicationScoped
public class CancionDAO {

	@Inject
	private Driver neo4jDriver;

	private static final String DATABASE_NAME = "ups-play"; // <-- ¡REEMPLAZA CON EL NOMBRE DE TU BASE DE DATOS REAL!

	// Método para guardar una nueva canción y sus relaciones
	// src/main/java/com/upsplay/dao/CancionDAO.java
	// ...
	public Cancion saveCancion(Cancion cancion) {
	    if (cancion.getUuid() == null || cancion.getUuid().isEmpty()) {
	        cancion.setUuid(UUID.randomUUID().toString());
	    }

	    System.out.println("DEBUG DAO: Canción recibida antes de la transacción:");
	    System.out.println("  UUID: " + cancion.getUuid());
	    System.out.println("  Titulo: " + cancion.getTitulo());
	    System.out.println("  Fecha Estreno (Long): " + cancion.getFechaEstreno());
	    System.out.println("  Reproducciones: " + cancion.getReproducciones());
	    System.out.println("  Enlace Cancion: " + cancion.getEnlaceCancion()); // ¡Verifica si esto es null!

	    System.out.println("  Generos recibidos (" + cancion.getGeneros().size() + "):");
	    cancion.getGeneros().forEach(g -> System.out.println("    - Genero UUID: " + g.getUuid() + ", Nombre: " + g.getNombre())); // Asumiendo getNombre()
	    System.out.println("  Autores recibidos (" + cancion.getAutores().size() + "):");
	    cancion.getAutores().forEach(a -> System.out.println("    - Autor UUID: " + a.getUuid() + ", Nombre: " + a.getNombre())); // Asumiendo getNombre()
	    System.out.println("  Premios recibidos (" + cancion.getPremios().size() + "):");
	    cancion.getPremios().forEach(p -> System.out.println("    - Premio UUID: " + p.getUuid() + ", Anio: " + p.getAnio())); // Asumiendo getAnio() y getCategoria()


	    try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
	        session.writeTransaction(tx -> {
	            // 1. Crear el nodo de la Canción con sus propiedades directas
	            String createSongQuery = "CREATE (c:Cancion {uuid: $uuid, titulo: $titulo, "
	                    + "fechaEstreno: $fechaEstreno, reproducciones: $reproducciones, enlaceCancion: $enlaceCancion}) " + "RETURN c";
	            Map<String, Object> songParams = new HashMap<>();
	            songParams.put("uuid", cancion.getUuid());
	            songParams.put("titulo", cancion.getTitulo());
	            songParams.put("fechaEstreno", cancion.getFechaEstreno());
	            songParams.put("reproducciones", cancion.getReproducciones());
	            songParams.put("enlaceCancion", cancion.getEnlaceCancion()); // <--- ¡Asegúrate que cancion.getEnlaceCancion() no sea null AQUI!

	            System.out.println("DEBUG DAO Transacción: Parámetros para CREATE Song: " + songParams);
	            tx.run(createSongQuery, songParams);

	            // 2. Crear relaciones con Géneros
	            System.out.println("DEBUG DAO Transacción: Creando relaciones con Géneros...");
	            for (Genero genero : cancion.getGeneros()) {
	                // VERIFICA ESTAS LINEAS PARA VER SI GENERO.GETUUID() ES NULL
	                System.out.println("  -> Procesando Género con UUID: " + genero.getUuid());
	                String createGenreRelQuery = "MATCH (c:Cancion {uuid: $songUuid}) "
	                        + "MATCH (g:Genero {uuid: $genreUuid}) "
	                        + "MERGE (c)-[:ES_DE_GENERO]->(g)";
	                tx.run(createGenreRelQuery, Map.of("songUuid", cancion.getUuid(), "genreUuid", genero.getUuid()));
	            }

	            // 3. Crear relaciones con Autores (Artistas)
	            System.out.println("DEBUG DAO Transacción: Creando relaciones con Autores...");
	            for (Autor autor : cancion.getAutores()) {
	                 System.out.println("  -> Procesando Autor con UUID: " + autor.getUuid());
	                String createAuthorRelQuery = "MATCH (c:Cancion {uuid: $songUuid}) "
	                        + "MATCH (a:Autor {uuid: $authorUuid}) "
	                        + "MERGE (c)-[:ESCRITA_POR]->(a)";
	                tx.run(createAuthorRelQuery, Map.of("songUuid", cancion.getUuid(), "authorUuid", autor.getUuid()));
	            }

	            // 4. Crear relaciones con Premios
	            System.out.println("DEBUG DAO Transacción: Creando relaciones con Premios...");
	            for (Premio premio : cancion.getPremios()) {
	                 System.out.println("  -> Procesando Premio con UUID: " + premio.getUuid());
	                String createAwardRelQuery = "MATCH (c:Cancion {uuid: $songUuid}) "
	                        + "MATCH (p:Premio {uuid: $awardUuid}) "
	                        + "MERGE (c)-[:HA_GANADO]->(p)";
	                tx.run(createAwardRelQuery, Map.of("songUuid", cancion.getUuid(), "awardUuid", premio.getUuid()));
	            }

	            return null;
	        });

	        return getCancionByUuid(cancion.getUuid());

	    } catch (Exception e) {
	        System.err.println("Error al guardar canción y sus relaciones en Neo4j: " + e.getMessage());
	        throw new RuntimeException("No se pudo guardar la canción y sus relaciones.", e);
	    }
	}
	public Optional<Cancion> getCancionByTitulo(String titulo) {
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String query = "MATCH (c:Cancion {titulo: $titulo}) " +
                           "OPTIONAL MATCH (c)-[:ES_DE_GENERO]->(g:Genero) " +
                           "OPTIONAL MATCH (c)-[:ESCRITA_POR]->(a:Autor) " +
                           "OPTIONAL MATCH (c)-[:HA_GANADO]->(p:Premio) " +
                           "RETURN c, collect(g) as generos, collect(a) as autores, collect(p) as premios";

            Result result = session.run(query, parameters("titulo", titulo));

            if (result.hasNext()) {
                Record record = result.next();
                return Optional.of(mapRecordToCancion(record));
            } else {
                return Optional.empty(); // No se encontró ninguna canción con ese título
            }
        } catch (Exception e) {
            System.err.println("Error al obtener canción por título '" + titulo + "': " + e.getMessage());
            return Optional.empty(); // En caso de error, también retornamos vacío
        }
    }
	// Método para obtener todas las canciones con sus relaciones
	public List<Cancion> getAllCanciones() {
		List<Cancion> canciones = new ArrayList<>();
		try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
			String cypherQuery = "MATCH (c:Cancion) " + "OPTIONAL MATCH (c)-[:ES_DE_GENERO]->(g:Genero) "
					+ "OPTIONAL MATCH (c)-[:ESCRITA_POR]->(a:Autor) " + "OPTIONAL MATCH (c)-[:HA_GANADO]->(p:Premio) "
					+ "RETURN c, COLLECT(DISTINCT g) AS generos, COLLECT(DISTINCT a) AS autores, COLLECT(DISTINCT p) AS premios "
					+ "ORDER BY c.titulo ASC";
			Result result = session.run(cypherQuery);

			while (result.hasNext()) {
				Record record = result.next();
				canciones.add(mapRecordToCancion(record)); // Ahora pasamos el Record completo
			}
		} catch (Exception e) {
			System.err.println("Error al obtener todas las canciones de Neo4j: " + e.getMessage());
			throw new RuntimeException("No se pudieron obtener las canciones.", e);
		}
		return canciones;
	}

	// Método para buscar canciones por título (modificado para traer relaciones)
	public List<Cancion> searchCancionesByTitulo(String busqueda) {
		List<Cancion> canciones = new ArrayList<>();
		if (busqueda == null || busqueda.trim().isEmpty()) {
			return canciones;
		}

		try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
			String cypherQuery = "MATCH (c:Cancion) " + "WHERE toLower(c.titulo) CONTAINS toLower($busqueda) "
					+ "OPTIONAL MATCH (c)-[:ES_DE_GENERO]->(g:Genero) "
					+ "OPTIONAL MATCH (c)-[:ESCRITA_POR]->(a:Autor) " + "OPTIONAL MATCH (c)-[:HA_GANADO]->(p:Premio) "
					+ "RETURN c, COLLECT(DISTINCT g) AS generos, COLLECT(DISTINCT a) AS autores, COLLECT(DISTINCT p) AS premios "
					+ "ORDER BY c.titulo ASC";

			Result result = session.run(cypherQuery, Map.of("busqueda", busqueda));

			while (result.hasNext()) {
				Record record = result.next();
				canciones.add(mapRecordToCancion(record));
			}
		} catch (Exception e) {
			System.err.println("Error al buscar canciones por título en Neo4j: " + e.getMessage());
			throw new RuntimeException("No se pudieron buscar las canciones.", e);
		}
		return canciones;
	}

	// Método para obtener una canción por su UUID con sus relaciones
	public Cancion getCancionByUuid(String uuid) {
		try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
			String cypherQuery = "MATCH (c:Cancion {uuid: $uuid}) " + "OPTIONAL MATCH (c)-[:ES_DE_GENERO]->(g:Genero) "
					+ "OPTIONAL MATCH (c)-[:ESCRITA_POR]->(a:Autor) " + "OPTIONAL MATCH (c)-[:HA_GANADO]->(p:Premio) "
					+ "RETURN c, COLLECT(DISTINCT g) AS generos, COLLECT(DISTINCT a) AS autores, COLLECT(DISTINCT p) AS premios";
			Result result = session.run(cypherQuery, Map.of("uuid", uuid));

			if (result.hasNext()) {
				return mapRecordToCancion(result.single()); // Pasamos el Record completo
			} else {
				return null;
			}
		} catch (Exception e) {
			System.err.println("Error al obtener canción por UUID en Neo4j: " + e.getMessage());
			throw new RuntimeException("No se pudo obtener la canción por UUID.", e);
		}
	}

	public List<Cancion> getCancionesByAutorUuid(String autorUuid) {
		List<Cancion> canciones = new ArrayList<>();
		if (autorUuid == null || autorUuid.trim().isEmpty()) {
			return canciones;
		}

		try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
			// MATCH para encontrar el autor, luego MATCH para encontrar las canciones
			// relacionadas
			// y OPTIONAL MATCH para traer los géneros, otros autores y premios de esas
			// canciones.
			String cypherQuery = "MATCH (a:Autor {uuid: $autorUuid})<-[:ESCRITA_POR]-(c:Cancion) "
					+ "OPTIONAL MATCH (c)-[:ES_DE_GENERO]->(g:Genero) "
					+ "OPTIONAL MATCH (c)-[:ESCRITA_POR]->(relatedAutor:Autor) "
					+ "OPTIONAL MATCH (c)-[:HA_GANADO]->(p:Premio) "
					+ "RETURN c, COLLECT(DISTINCT g) AS generos, COLLECT(DISTINCT relatedAutor) AS autores, COLLECT(DISTINCT p) AS premios "
					+ "ORDER BY c.titulo ASC";

			Result result = session.run(cypherQuery, Map.of("autorUuid", autorUuid));

			while (result.hasNext()) {
				Record record = result.next();
				canciones.add(mapRecordToCancion(record)); // Reutilizamos el método de mapeo existente
			}
		} catch (Exception e) {
			System.err.println("Error al obtener canciones por autor UUID en Neo4j: " + e.getMessage());
			throw new RuntimeException("No se pudieron obtener las canciones del autor.", e);
		}
		return canciones;
	}

	public List<Cancion> getCancionesPorFecha(Date fechaInicio, Date fechaFin) {
        List<Cancion> canciones = new ArrayList<>();
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            StringBuilder cypherQuery = new StringBuilder();
            cypherQuery.append("MATCH (c:Cancion) ");

            // Lista para almacenar todas las condiciones del WHERE
            List<String> conditions = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();

            // Ajuste para el inicio del rango (fechaInicio)
            if (Objects.nonNull(fechaInicio)) {
                conditions.add("c.fechaEstreno >= $fechaInicioMillis");
                parameters.put("fechaInicioMillis", fechaInicio.getTime());
            }

            // Ajuste para el fin del rango (fechaFin) para incluir todo el último día
            if (Objects.nonNull(fechaFin)) {
                // Sumar casi un día completo para que el filtro incluya hasta el final del día de fechaFin
                long fechaFinIncluyendoDiaCompleto = fechaFin.getTime() + (24 * 60 * 60 * 1000L) - 1; // Añadido 'L' para asegurar operación long
                conditions.add("c.fechaEstreno <= $fechaFinMillis");
                parameters.put("fechaFinMillis", fechaFinIncluyendoDiaCompleto);
            }

            // Si hay condiciones, añadir la cláusula WHERE
            if (!conditions.isEmpty()) {
                cypherQuery.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
            }

            cypherQuery.append("OPTIONAL MATCH (c)-[:ES_DE_GENERO]->(g:Genero) ");
            cypherQuery.append("OPTIONAL MATCH (c)-[:ESCRITA_POR]->(a:Autor) "); // Asegúrate de que las flechas aquí estén correctas si cambiaste la dirección
            cypherQuery.append("OPTIONAL MATCH (c)-[:HA_GANADO]->(p:Premio) ");

            cypherQuery.append("RETURN c, COLLECT(DISTINCT g) AS generos, COLLECT(DISTINCT a) AS autores, COLLECT(DISTINCT p) AS premios ");
            cypherQuery.append("ORDER BY c.titulo ASC");

            // --- DEBUGGING LOGS ---
            System.out.println("DEBUG CancionesPorFecha - Cypher Query: " + cypherQuery.toString());
            System.out.println("DEBUG CancionesPorFecha - Parameters: " + parameters);
            // --- FIN DEBUGGING LOGS ---

            Result result = session.run(cypherQuery.toString(), parameters);

            while (result.hasNext()) {
                Record record = result.next();
                canciones.add(mapRecordToCancion(record));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener canciones por rango de fechas de Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudieron obtener las canciones por rango de fechas.", e);
        }
        return canciones;
    }


	public  Optional<Cancion>  getCancionMayorD(Date fechaInicio, Date fechaFin) {
	    try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
	        StringBuilder cypherQuery = new StringBuilder();
	        cypherQuery.append("MATCH (c:Cancion) ");
	        List<String> conditions = new ArrayList<>();
	        Map<String, Object> parameters = new HashMap<>();

	        if (Objects.nonNull(fechaInicio)) {
	            conditions.add("c.fechaEstreno >= $fechaInicioMillis");
	            parameters.put("fechaInicioMillis", fechaInicio.getTime());
	        }

	        if (Objects.nonNull(fechaFin)) {
	            long fechaFinIncluyendoDiaCompleto = fechaFin.getTime() + (24 * 60 * 60 * 1000) - 1; 
	            conditions.add("c.fechaEstreno <= $fechaFinMillis");
	            parameters.put("fechaFinMillis", fechaFinIncluyendoDiaCompleto); // Usar el valor ajustado
	            // O si quieres el 00:00:00 exacto como límite:
	            // parameters.put("fechaFinMillis", fechaFin.getTime());
	        }

	        // Si hay condiciones, añadir la cláusula WHERE
	        if (!conditions.isEmpty()) {
	            cypherQuery.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
	        }

	        cypherQuery.append("OPTIONAL MATCH (c)-[:ES_DE_GENERO]->(g:Genero) ");
	        cypherQuery.append("OPTIONAL MATCH (c)-[:ESCRITA_POR]->(a:Autor) ");
	        cypherQuery.append("OPTIONAL MATCH (c)-[:HA_GANADO]->(p:Premio) ");

	        // Cláusulas para encontrar la canción con mayor reproducción
	        cypherQuery.append("RETURN c, COLLECT(DISTINCT g) AS generos, COLLECT(DISTINCT a) AS autores, COLLECT(DISTINCT p) AS premios ");
	        cypherQuery.append("ORDER BY c.reproducciones DESC "); // Ordenar por reproducciones de mayor a menor
	        cypherQuery.append("LIMIT 1"); // Tomar solo la primera (la de mayor reproducción)

	        // Imprime la consulta final antes de ejecutarla para depuración
	        System.out.println("DEBUG Cypher Query: " + cypherQuery.toString());
	        System.out.println("DEBUG Parameters: " + parameters);
	        
	        Result result = session.run(cypherQuery.toString(), parameters);

	        if (result.hasNext()) {
	            Record record = result.next();
	            return Optional.of(mapRecordToCancion(record));
	        } else {
	            return Optional.empty(); // No se encontró ninguna canción
	        }
	    } catch (Exception e) {
	        System.err.println("Error al obtener la canción de mayor reproducción por rango de fechas de Neo4j: " + e.getMessage());
	        throw new RuntimeException("No se pudo obtener la canción de mayor reproducción.", e);
	    }
	}
	
	public List<Cancion> getCancionesPorGeneroID(String uuid) {
        List<Cancion> canciones = new ArrayList<>();
        try (Session session = neo4jDriver.session(SessionConfig.forDatabase(DATABASE_NAME))) {
            String cypherQuery = "MATCH (c:Cancion)-[:ES_DE_GENERO]->(g:Genero {uuid: $uuid}) " + // Filtra por el UUID del género
                                 "OPTIONAL MATCH (c)-[:ESCRITA_POR]->(a:Autor) " +
                                 "OPTIONAL MATCH (c)-[:HA_GANADO]->(p:Premio) " +
                                 "RETURN c, COLLECT(DISTINCT g) AS generos, COLLECT(DISTINCT a) AS autores, COLLECT(DISTINCT p) AS premios " +
                                 "ORDER BY c.titulo ASC";

            // Pasamos el UUID del género como parámetro
            Result result = session.run(cypherQuery, Map.of("uuid", uuid));

            while (result.hasNext()) {
                Record record = result.next();
                canciones.add(mapRecordToCancion(record));
            }
        } catch (Exception e) {
            System.err.println("Error al obtener canciones por UUID de género de Neo4j: " + e.getMessage());
            throw new RuntimeException("No se pudieron obtener las canciones por UUID de género.", e);
        }
        return canciones;
    }

	// Método auxiliar para mapear un Record completo a un objeto Cancion
	private Cancion mapRecordToCancion(Record record) {
		Cancion cancion = new Cancion();
        Node cNode = record.get("c").asNode();
		Map<String, Object> cProps = record.get("c").asMap();

		cancion.setUuid((String) cProps.getOrDefault("uuid", null));
		cancion.setTitulo((String) cProps.getOrDefault("titulo", null));
        cancion.setReproducciones(cNode.get("reproducciones").asInt());
        cancion.setEnlaceCancion(cNode.get("enlaceCancion").asString());
		Value fechaEstrenoValue = cNode.get("fechaEstreno");
        if (fechaEstrenoValue != Values.NULL) {
            cancion.setFechaEstreno(fechaEstrenoValue.asLong()); // Leer como Long
        }

        Value enlaceCancionValue = cNode.get("enlaceCancion");
        if (enlaceCancionValue != Values.NULL) {
            cancion.setEnlaceCancion(enlaceCancionValue.asString());
        }

		cancion.setReproducciones(((Number) cProps.getOrDefault("reproducciones", 0L)).longValue());

		// Mapear géneros relacionados
		List<Genero> generos = new ArrayList<>();
		List<Object> generoList = record.get("generos").asList();
		for (Object item : generoList) {
			if (item instanceof Node) { // ¡Cambiado aquí! Ahora verifica directamente si es un Node
				Node node = (Node) item; // Castea a Node
				Genero g = mapValueToGenero(node.asMap());
				generos.add(g);
			}
			else if (item instanceof Map) {
				Genero g = mapValueToGenero((Map<String, Object>) item);
				generos.add(g);
			} else {
			}
		}
		cancion.setGeneros(generos);

		// Mapear autores relacionados
		List<Autor> autores = new ArrayList<>();
		List<Object> autorList = record.get("autores").asList();
		for (Object item : autorList) {
			if (item instanceof Node) { // ¡Cambiado aquí!
				Node node = (Node) item;
				Autor a = mapValueToAutor(node.asMap());
				autores.add(a);
			} else if (item instanceof Map) {
				Autor a = mapValueToAutor((Map<String, Object>) item);
				autores.add(a);
			} else {
			}
		}
		cancion.setAutores(autores);

		// Mapear premios relacionados
		List<Premio> premios = new ArrayList<>();
		List<Object> premioList = record.get("premios").asList();
		for (Object item : premioList) {
			if (item instanceof Node) { // ¡Cambiado aquí!
				Node node = (Node) item;
				Premio p = mapValueToPremio(node.asMap());
				premios.add(p);
			} else if (item instanceof Map) {
				Premio p = mapValueToPremio((Map<String, Object>) item);
				premios.add(p);
			} else {
			}
		}
		cancion.setPremios(premios);

		return cancion;
	}

	// Métodos auxiliares para mapear Maps de propiedades a objetos de modelo
	// Estos son idénticos a los que ya tienes en tus otros DAOs (GeneroDAO,
	// AutorDAO, PremioDAO)
	// Se incluyen aquí para que mapRecordToCancion pueda usarlos directamente.
	private Genero mapValueToGenero(Map<String, Object> properties) {
		Genero genero = new Genero();
		genero.setUuid((String) properties.getOrDefault("uuid", null));
		genero.setNombre((String) properties.getOrDefault("nombre", null));
		return genero;
	}

	private Autor mapValueToAutor(Map<String, Object> properties) {
		Autor autor = new Autor();
		autor.setUuid((String) properties.getOrDefault("uuid", null));
		autor.setNombre((String) properties.getOrDefault("nombre", null));
		autor.setPaisOrigen((String) properties.getOrDefault("paisOrigen", null));
		return autor;
	}

	private Premio mapValueToPremio(Map<String, Object> properties) {
		Premio premio = new Premio();
		premio.setUuid((String) properties.getOrDefault("uuid", null));
		premio.setNombrePremio((String) properties.getOrDefault("nombrePremio", null));
		premio.setAnio(((Number) properties.getOrDefault("anio", 0)).intValue());
		premio.setCategoria((String) properties.getOrDefault("categoria", null));
		return premio;
	}

}