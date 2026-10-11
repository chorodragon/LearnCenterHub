package com.proyecto.learncenterhub;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import com.proyecto.learncenterhub.datos.AplicacionesDao;
import com.proyecto.learncenterhub.entidades.Aplicacion;
import com.proyecto.learncenterhub.util.Estilos;
import com.proyecto.learncenterhub.util.Lanzador;
import com.proyecto.learncenterhub.util.Locations;

public final class AplicacionDialog extends JDialog {

    private final Aplicacion aplicacion;
    private final Runnable alEliminar;
    private final AplicacionesDao aplicacionesDao = new AplicacionesDao();
    private final JButton btnEjecutar = new JButton("Ejecutar");
    private final JButton btnEliminar = new JButton("Eliminar");

    public AplicacionDialog(JFrame owner, Aplicacion aplicacion, Runnable alEliminar) {
        super(owner, valorOVacio(aplicacion.getNombre()), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.aplicacion = aplicacion;
        this.alEliminar = alEliminar;

        JPanel datos = new JPanel(new GridBagLayout());
        datos.setBorder(BorderFactory.createEmptyBorder(18, 20, 8, 20));
        int fila = 0;

        JLabel lblTitulo = new JLabel(valorOVacio(aplicacion.getNombre()));
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 18f));
        GridBagConstraints cabecera = new GridBagConstraints();
        cabecera.gridx = 0;
        cabecera.gridwidth = 2;
        cabecera.anchor = GridBagConstraints.WEST;
        cabecera.insets = new Insets(0, 4, 14, 4);
        cabecera.gridy = fila++;
        datos.add(lblTitulo, cabecera);

        fila = agregarCampo(datos, fila, "ID", String.valueOf(aplicacion.getId()));
        fila = agregarCampo(datos, fila, "Curso", aplicacion.getCurso());
        fila = agregarCampo(datos, fila, "Materia", aplicacion.getMateria());
        fila = agregarCampo(datos, fila, "Tipo", aplicacion.getTipo());
        fila = agregarCampo(datos, fila, "Comando", aplicacion.getComando());
        fila = agregarCampo(datos, fila, "Tags", aplicacion.getTags());
        fila = agregarCampo(datos, fila, "Carátula", aplicacion.getCaratula());
        agregarCampo(datos, fila, "Descripción", aplicacion.getDescripcion());

        btnEjecutar.addActionListener(e -> ejecutar());
        boolean tieneComando = aplicacion.getComando() != null && !aplicacion.getComando().isBlank();
        btnEjecutar.setEnabled(tieneComando);
        if (!tieneComando) {
            btnEjecutar.setToolTipText("La aplicación no tiene un comando configurado");
        }

        btnEliminar.addActionListener(e -> eliminar());
        Estilos.botonPeligro(btnEliminar);
        Estilos.botonPrincipal(btnEjecutar);

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izquierda.add(btnEliminar);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.add(btnEjecutar);
        JPanel botones = new JPanel(new BorderLayout());
        botones.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        botones.add(izquierda, BorderLayout.WEST);
        botones.add(derecha, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(datos, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(btnEjecutar);

        pack();
        setSize(Math.max(getWidth(), 520), getHeight());
        setLocationRelativeTo(owner);
    }

    private static int agregarCampo(JPanel panel, int fila, String etiqueta, String valor) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila;
        c.insets = new Insets(4, 4, 4, 12);
        c.anchor = GridBagConstraints.NORTHWEST;

        JLabel lblEtiqueta = new JLabel(etiqueta);
        Estilos.textoSecundario(lblEtiqueta);
        c.gridx = 0;
        panel.add(lblEtiqueta, c);

        JTextArea txtValor = new JTextArea(valorOVacio(valor));
        txtValor.setEditable(false);
        txtValor.setLineWrap(true);
        txtValor.setWrapStyleWord(true);
        txtValor.setOpaque(false);
        txtValor.setFont(lblEtiqueta.getFont().deriveFont(Font.PLAIN));
        txtValor.setBorder(null);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;

        if ("Descripción".equals(etiqueta)) {
            txtValor.setRows(5);
            txtValor.setColumns(30);
            JScrollPane scroll = new JScrollPane(txtValor);
            scroll.setBorder(null);
            scroll.setOpaque(false);
            scroll.getViewport().setOpaque(false);
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            panel.add(scroll, c);
        } else {
            txtValor.setColumns(30);
            panel.add(txtValor, c);
        }
        return fila + 1;
    }

    private void ejecutar() {
        btnEjecutar.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws IOException {
                Lanzador.ejecutar(aplicacion.getComando());
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    dispose();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    btnEjecutar.setEnabled(true);
                    JOptionPane.showMessageDialog(AplicacionDialog.this,
                            "No se pudo ejecutar: " + e.getCause().getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void eliminar() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Eliminar definitivamente \"" + valorOVacio(aplicacion.getNombre()) + "\"?\n"
                        + "Se borrarán el registro y la carátula. Esta acción no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        btnEliminar.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws IOException {
                aplicacionesDao.deleteAplicacion(aplicacion.getId());
                String caratula = aplicacion.getCaratula();
                if (caratula == null || caratula.isBlank()) {
                    return null;
                }
                File fichero = new File(Locations.getCoversFolder(), caratula);
                try {
                    Files.deleteIfExists(fichero.toPath());
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return fichero.getAbsolutePath();
                }
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    String noBorrado = get();
                    if (noBorrado != null) {
                        JOptionPane.showMessageDialog(AplicacionDialog.this,
                                "La aplicación se eliminó del catálogo, pero no se pudo borrar:\n" + noBorrado,
                                "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                    dispose();
                    alEliminar.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                    btnEliminar.setEnabled(true);
                    JOptionPane.showMessageDialog(AplicacionDialog.this,
                            "No se pudo eliminar la aplicación: " + e.getCause().getMessage(), "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private static String valorOVacio(String valor) {
        return valor == null ? "" : valor;
    }
}
