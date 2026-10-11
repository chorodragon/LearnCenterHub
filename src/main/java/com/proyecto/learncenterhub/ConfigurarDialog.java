package com.proyecto.learncenterhub;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import com.formdev.flatlaf.FlatClientProperties;
import com.proyecto.learncenterhub.datos.AplicacionesDao;
import com.proyecto.learncenterhub.entidades.Categoria;
import com.proyecto.learncenterhub.util.Estilos;
import com.proyecto.learncenterhub.util.Locations;

public final class ConfigurarDialog extends JDialog {

    private final AplicacionesDao aplicacionesDao = new AplicacionesDao();

    private final JTextField txtCarpeta = new JTextField(30);
    private final SeccionCategoria seccionCursos = new SeccionCategoria(Categoria.CURSO, null);
    private final SeccionCategoria seccionMaterias = new SeccionCategoria(Categoria.MATERIA,
            seccionCursos.lista::getSelectedValue);
    private final SeccionCategoria seccionTipos = new SeccionCategoria(Categoria.TIPO, null);

    public ConfigurarDialog(JFrame owner, Runnable alCerrar) {
        super(owner, "Configurar", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                alCerrar.run();
            }
        });

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));
        int fila = 0;

        fila = agregarTitulo(contenido, fila, "Carpeta de recursos",
                "Variable de usuario " + Locations.VARIABLE);
        txtCarpeta.setText(Locations.getRootFolder());
        txtCarpeta.setEditable(false);
        txtCarpeta.setToolTipText("Por defecto: " + Locations.DEFAULT_ROOT_FOLDER);
        GridBagConstraints c = restricciones(fila++);
        c.insets = new Insets(4, 4, 18, 4);
        contenido.add(txtCarpeta, c);

        seccionCursos.lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seccionMaterias.cargar();
            }
        });
        JPanel categorias = new JPanel(new GridLayout(1, 3, 16, 0));
        categorias.add(seccionCursos);
        categorias.add(seccionMaterias);
        categorias.add(seccionTipos);
        c = restricciones(fila);
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        contenido.add(categorias, c);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new java.awt.Color(0xE5E7EB)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        botones.add(btnCerrar);

        setLayout(new BorderLayout());
        add(contenido, BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        cargarCategorias();
        pack();
        setSize(Math.max(getWidth(), 820), getHeight());
        setLocationRelativeTo(owner);
    }

    private static int agregarTitulo(JPanel panel, int fila, String titulo, String ayuda) {
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 14f));
        GridBagConstraints c = restricciones(fila++);
        c.insets = new Insets(0, 4, 0, 4);
        panel.add(lblTitulo, c);

        JLabel lblAyuda = new JLabel(ayuda);
        lblAyuda.setFont(lblAyuda.getFont().deriveFont(11f));
        Estilos.textoSecundario(lblAyuda);
        c = restricciones(fila++);
        c.insets = new Insets(0, 4, 6, 4);
        panel.add(lblAyuda, c);
        return fila;
    }

    private static GridBagConstraints restricciones(int fila) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = fila;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 4, 4);
        return c;
    }

    private void cargarCategorias() {
        seccionCursos.cargar();
        seccionMaterias.cargar();
        seccionTipos.cargar();
    }

    private final class SeccionCategoria extends JPanel {

        private final Categoria categoria;
        private final Supplier<String> curso;
        private final DefaultListModel<String> modelo = new DefaultListModel<>();
        private final JList<String> lista = new JList<>(modelo);
        private final JTextField txtNuevo = new JTextField(12);
        private final JButton btnAgregar = new JButton("Agregar");
        private final JButton btnEliminar = new JButton("Eliminar");

        SeccionCategoria(Categoria categoria, Supplier<String> curso) {
            super(new GridBagLayout());
            this.categoria = categoria;
            this.curso = curso;
            int fila = agregarTitulo(this, 0, categoria.getPlural(), curso == null
                    ? "Cada aplicación debe tener " + categoria.getConArticulo()
                    : "Del curso seleccionado");
            txtNuevo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, categoria.getNuevo());
            txtNuevo.addActionListener(e -> agregar());
            btnAgregar.setToolTipText("Agregar " + categoria.getConArticulo());
            btnAgregar.addActionListener(e -> agregar());
            Estilos.botonPrincipal(btnAgregar);
            JPanel filaNuevo = new JPanel(new BorderLayout(8, 0));
            filaNuevo.add(txtNuevo, BorderLayout.CENTER);
            filaNuevo.add(btnAgregar, BorderLayout.EAST);
            add(filaNuevo, restricciones(fila++));

            lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            lista.setVisibleRowCount(8);
            lista.addListSelectionListener(e -> btnEliminar.setEnabled(lista.getSelectedIndex() >= 0));
            GridBagConstraints c = restricciones(fila++);
            c.weighty = 1;
            c.fill = GridBagConstraints.BOTH;
            add(new JScrollPane(lista), c);

            btnEliminar.setEnabled(false);
            btnEliminar.addActionListener(e -> eliminar());
            Estilos.botonPeligro(btnEliminar);
            c = restricciones(fila);
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.EAST;
            add(btnEliminar, c);
        }

        void cargar() {
            String seleccionado = lista.getSelectedValue();
            modelo.clear();
            String delCurso = cursoActual();
            boolean habilitada = curso == null || delCurso != null;
            if (habilitada) {
                aplicacionesDao.getValores(categoria, delCurso).forEach(modelo::addElement);
                lista.setSelectedValue(seleccionado, true);
            }
            txtNuevo.setEnabled(habilitada);
            btnAgregar.setEnabled(habilitada);
            lista.setEnabled(habilitada);
            txtNuevo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                    habilitada ? categoria.getNuevo() : "Seleccione un curso");
        }

        private String cursoActual() {
            return curso == null ? null : curso.get();
        }

        private void agregar() {
            String valor = txtNuevo.getText().trim();
            if (valor.isEmpty()) {
                txtNuevo.requestFocusInWindow();
                return;
            }
            try {
                if (!aplicacionesDao.insertValor(categoria, cursoActual(), valor)) {
                    JOptionPane.showMessageDialog(ConfigurarDialog.this,
                            "Ya existe " + categoria.getConArticulo() + " \"" + valor + "\".",
                            "Duplicado", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                txtNuevo.setText("");
                cargar();
                lista.setSelectedValue(valor, true);
            } catch (IOException e) {
                e.printStackTrace();
                mostrarError("No se pudo guardar: " + e.getMessage());
            }
        }

        private void eliminar() {
            String valor = lista.getSelectedValue();
            if (valor == null) {
                return;
            }
            int respuesta = JOptionPane.showConfirmDialog(ConfigurarDialog.this,
                    "¿Eliminar " + categoria.getConArticuloDefinido() + " \"" + valor + "\"?"
                            + (categoria == Categoria.CURSO ? "\nTambién se eliminarán sus materias." : ""),
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                long enUso = aplicacionesDao.deleteValor(categoria, cursoActual(), valor);
                if (enUso > 0) {
                    JOptionPane.showMessageDialog(ConfigurarDialog.this,
                            "No se puede eliminar: está en uso por " + enUso
                                    + (enUso == 1 ? " aplicación." : " aplicaciones."),
                            categoria.getNombre() + " en uso", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                cargar();
            } catch (IOException e) {
                e.printStackTrace();
                mostrarError("No se pudo eliminar: " + e.getMessage());
            }
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
