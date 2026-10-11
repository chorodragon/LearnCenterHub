package com.proyecto.learncenterhub.util;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Locale;

public final class Lanzador {

    private Lanzador() {
    }

    public static void ejecutar(String comando) throws IOException {
        if (comando == null || comando.isBlank()) {
            throw new IOException("La aplicación no tiene un comando configurado.");
        }
        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        List<String> argumentos = windows ? List.of("cmd", "/c", comando) : List.of("sh", "-c", comando);
        new ProcessBuilder(argumentos)
                .directory(new File(System.getProperty("user.home")))
                .redirectInput(Redirect.from(new File(windows ? "NUL" : "/dev/null")))
                .redirectOutput(Redirect.appendTo(new File(Locations.getLogFile())))
                .redirectErrorStream(true)
                .start();
    }
}
