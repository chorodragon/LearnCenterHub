package com.proyecto.learncenterhub.datos;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.proyecto.learncenterhub.entidades.Aplicacion;
import com.proyecto.learncenterhub.entidades.Catalogo;
import com.proyecto.learncenterhub.entidades.Categoria;
import com.proyecto.learncenterhub.entidades.Curso;
import com.proyecto.learncenterhub.util.Locations;

public class AplicacionesDao {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Object BLOQUEO = new Object();

    public Catalogo getCatalogo() {
        try {
            synchronized (BLOQUEO) {
                return leer();
            }
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
            return new Catalogo();
        }
    }

    public List<Aplicacion> getAplicaciones() {
        return getCatalogo().getAplicaciones();
    }

    public void insertAplicacion(Aplicacion aplicacion) throws IOException {
        synchronized (BLOQUEO) {
            Catalogo catalogo = leer();
            int maxId = catalogo.getAplicaciones().stream().mapToInt(Aplicacion::getId).max().orElse(0);
            aplicacion.setId(maxId + 1);
            catalogo.getAplicaciones().add(aplicacion);
            escribir(catalogo);
        }
    }

    public boolean existeAplicacionConNombre(String nombre) throws IOException {
        synchronized (BLOQUEO) {
            return leer().getAplicaciones().stream().anyMatch(a -> mismoNombre(a.getNombre(), nombre));
        }
    }

    public void deleteAplicacion(int id) throws IOException {
        synchronized (BLOQUEO) {
            Catalogo catalogo = leer();
            if (catalogo.getAplicaciones().removeIf(a -> a.getId() == id)) {
                escribir(catalogo);
            }
        }
    }

    public List<String> getValores(Categoria categoria, String curso) {
        Catalogo catalogo = getCatalogo();
        return categoria == Categoria.CURSO
                ? catalogo.getCursos().stream().map(Curso::getNombre).toList()
                : valores(catalogo, categoria, curso);
    }

    public boolean insertValor(Categoria categoria, String curso, String valor) throws IOException {
        synchronized (BLOQUEO) {
            Catalogo catalogo = leer();
            if (categoria == Categoria.CURSO) {
                if (buscarCurso(catalogo, valor) != null) {
                    return false;
                }
                catalogo.getCursos().add(new Curso(valor.trim()));
            } else {
                List<String> valores = valores(catalogo, categoria, curso);
                if (valores.stream().anyMatch(v -> mismoNombre(v, valor))) {
                    return false;
                }
                valores.add(valor.trim());
            }
            escribir(catalogo);
            return true;
        }
    }

    public long deleteValor(Categoria categoria, String curso, String valor) throws IOException {
        synchronized (BLOQUEO) {
            Catalogo catalogo = leer();
            long enUso = catalogo.getAplicaciones().stream()
                    .filter(a -> mismoNombre(categoria.valorDe(a), valor))
                    .filter(a -> categoria != Categoria.MATERIA || mismoNombre(a.getCurso(), curso))
                    .count();
            if (enUso > 0) {
                return enUso;
            }
            boolean eliminado = categoria == Categoria.CURSO
                    ? catalogo.getCursos().removeIf(c -> mismoNombre(c.getNombre(), valor))
                    : valores(catalogo, categoria, curso).removeIf(v -> mismoNombre(v, valor));
            if (eliminado) {
                escribir(catalogo);
            }
            return 0;
        }
    }

    private static List<String> valores(Catalogo catalogo, Categoria categoria, String curso) {
        return switch (categoria) {
            case TIPO -> catalogo.getTipos();
            case MATERIA -> {
                Curso encontrado = buscarCurso(catalogo, curso);
                yield encontrado == null ? new ArrayList<>() : encontrado.getMaterias();
            }
            case CURSO -> throw new IllegalArgumentException("Los cursos no son una lista de nombres");
        };
    }

    private static Curso buscarCurso(Catalogo catalogo, String nombre) {
        return catalogo.getCursos().stream().filter(c -> mismoNombre(c.getNombre(), nombre)).findFirst().orElse(null);
    }

    public static void migrarFormatoAnterior() throws IOException {
        Path anterior = Path.of(Locations.getRootFolder(), "aplicaciones.json");
        synchronized (BLOQUEO) {
            if (Files.exists(fichero()) || !Files.isRegularFile(anterior)) {
                return;
            }
            Catalogo catalogo = new Catalogo();
            try (Reader reader = Files.newBufferedReader(anterior, StandardCharsets.UTF_8)) {
                List<Aplicacion> aplicaciones = GSON.fromJson(reader, new TypeToken<List<Aplicacion>>() {
                }.getType());
                if (aplicaciones != null) {
                    catalogo.getAplicaciones().addAll(aplicaciones);
                }
            }
            escribir(catalogo);
            Files.move(anterior, anterior.resolveSibling("aplicaciones.json.bak"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static boolean mismoNombre(String a, String b) {
        return a != null && b != null && a.trim().toLowerCase(Locale.ROOT).equals(b.trim().toLowerCase(Locale.ROOT));
    }

    private static Path fichero() {
        return Path.of(Locations.getDataFile());
    }

    private static Catalogo leer() throws IOException {
        Path fichero = fichero();
        if (!Files.exists(fichero) || Files.size(fichero) == 0) {
            return new Catalogo();
        }
        try (Reader reader = Files.newBufferedReader(fichero, StandardCharsets.UTF_8)) {
            Catalogo catalogo = GSON.fromJson(reader, Catalogo.class);
            return catalogo == null ? new Catalogo() : catalogo;
        }
    }

    private static void escribir(Catalogo catalogo) throws IOException {
        Path fichero = fichero();
        Path temporal = fichero.resolveSibling(fichero.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
            GSON.toJson(catalogo, writer);
        }
        Files.move(temporal, fichero, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }

}
