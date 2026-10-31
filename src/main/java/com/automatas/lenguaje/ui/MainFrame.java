package com.automatas.lenguaje.ui;

import com.automatas.lenguaje.grammar.FormalGrammar;
import com.automatas.lenguaje.model.*;
import com.automatas.lenguaje.pda.JFLAPExporter;
import com.automatas.lenguaje.pda.PushdownAutomaton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interfaz Gráfica de Usuario (GUI) para el Analizador Lingüístico del Español
 * mediante Autómata de Pila Formal.
 */
public class MainFrame extends JFrame {
    private final PushdownAutomaton automaton;
    private final JFLAPExporter jflapExporter;

    // Componentes de Entrada
    private JTextArea inputTextArea;
    private JComboBox<ExampleItem> exampleComboBox;
    private JButton analyzeButton;
    private JButton clearButton;
    private JButton exportJFlapButton;

    // Componentes del Banner de Estado
    private JPanel statusBanner;
    private JLabel statusLabel;
    private JLabel statusDetailsLabel;
    private JLabel statsLabel;

    // Componentes de Resultados
    private JTabbedPane tabbedPane;
    private DefaultTableModel lexicalTableModel;
    private JTable lexicalTable;
    private DefaultTableModel traceTableModel;
    private JTable traceTable;
    private JTextArea grammarArea;
    private DefaultTableModel ll1TableModel;
    private JTable ll1Table;
    private JTextArea formalTupleArea;
    private JTextArea jflapPreviewArea;

    public MainFrame() {
        this.automaton = new PushdownAutomaton();
        this.jflapExporter = new JFLAPExporter(automaton.getGrammar());

        setTitle("Analizador Lingüístico del Español | Autómata de Pila Formal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 820);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);

        initComponents();
        loadGrammarInfo();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // 1. Panel Superior: Encabezado y Entrada de Texto
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));

        // Header Title
        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        JLabel titleLabel = new JLabel("Analizador Lingüístico del Español mediante Autómata de Pila");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLabel.setForeground(new Color(25, 50, 95));
        JLabel subtitleLabel = new JLabel("Proyecto Universitario de Teoría de Autómatas y Lenguajes Formales | Gramática LL(1) y PDA 7-Tupla");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(Color.GRAY);
        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);
        topPanel.add(headerPanel, BorderLayout.NORTH);

        // Input & Controls
        JPanel inputContainer = new JPanel(new BorderLayout(8, 8));
        inputContainer.setBorder(new TitledBorder("Entrada de Oración en Español"));

        inputTextArea = new JTextArea(3, 60);
        inputTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setText("El perro corre rápido.");
        JScrollPane inputScroll = new JScrollPane(inputTextArea);
        inputContainer.add(inputScroll, BorderLayout.CENTER);

        // Controls bar
        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));

        exampleComboBox = new JComboBox<>(getExampleItems());
        exampleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        exampleComboBox.addActionListener(e -> {
            ExampleItem selected = (ExampleItem) exampleComboBox.getSelectedItem();
            if (selected != null && !selected.text.isEmpty()) {
                inputTextArea.setText(selected.text);
            }
        });

        analyzeButton = new JButton("Analizar Oración");
        analyzeButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        analyzeButton.setBackground(new Color(20, 105, 185));
        analyzeButton.setForeground(Color.WHITE);
        analyzeButton.addActionListener(e -> executeAnalysis());

        clearButton = new JButton("Limpiar");
        clearButton.addActionListener(e -> {
            inputTextArea.setText("");
            resetResults();
        });

        exportJFlapButton = new JButton("Exportar JFLAP (.jff)");
        exportJFlapButton.addActionListener(e -> exportToJFlap());

        controlBar.add(new JLabel("Ejemplos:"));
        controlBar.add(exampleComboBox);
        controlBar.add(analyzeButton);
        controlBar.add(clearButton);
        controlBar.add(exportJFlapButton);

        inputContainer.add(controlBar, BorderLayout.SOUTH);
        topPanel.add(inputContainer, BorderLayout.CENTER);

        // Banner de Estado
        statusBanner = new JPanel(new BorderLayout(6, 4));
        statusBanner.setBorder(new EmptyBorder(8, 12, 8, 12));
        statusBanner.setBackground(new Color(238, 242, 246));

        statusLabel = new JLabel("Listo para analizar.");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        statusDetailsLabel = new JLabel("Ingrese una oración y presione 'Analizar Oración' o seleccione un ejemplo predefinido.");
        statusDetailsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        statsLabel = new JLabel("Métricas: -");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statsLabel.setForeground(Color.DARK_GRAY);

        JPanel statusTextPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        statusTextPanel.setOpaque(false);
        statusTextPanel.add(statusLabel);
        statusTextPanel.add(statusDetailsLabel);

        statusBanner.add(statusTextPanel, BorderLayout.CENTER);
        statusBanner.add(statsLabel, BorderLayout.SOUTH);

        topPanel.add(statusBanner, BorderLayout.SOUTH);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // 2. Panel Central: Pestañas de Resultados
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Tab 1: Análisis Léxico y Morfológico
        tabbedPane.addTab("1. Léxico y Morfología", createLexicalPanel());

        // Tab 2: Traza del Autómata de Pila
        tabbedPane.addTab("2. Traza del Autómata de Pila", createTracePanel());

        // Tab 3: Gramática Formal y Tabla LL(1)
        tabbedPane.addTab("3. Gramática Formal y Tabla LL(1)", createGrammarPanel());

        // Tab 4: Definición Matemática (7-Tupla)
        tabbedPane.addTab("4. 7-Tupla Matemática", createFormalTuplePanel());

        // Tab 5: JFLAP
        tabbedPane.addTab("5. Archivo JFLAP (.jff)", createJFlapPanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    private JPanel createLexicalPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        String[] columns = {
                "#", "Lexema", "Normalizado", "Categoría Gramatical", "Lema", "Rasgos Morfológicos", "Terminal PDA", "Estado", "Alternativas Léxicas"
        };
        lexicalTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        lexicalTable = new JTable(lexicalTableModel);
        lexicalTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lexicalTable.setRowHeight(24);
        lexicalTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        lexicalTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        lexicalTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        lexicalTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        lexicalTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        lexicalTable.getColumnModel().getColumn(5).setPreferredWidth(210);
        lexicalTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        lexicalTable.getColumnModel().getColumn(7).setPreferredWidth(110);
        lexicalTable.getColumnModel().getColumn(8).setPreferredWidth(220);

        panel.add(new JScrollPane(lexicalTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTracePanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        String[] columns = {
                "Paso", "Estado Actual", "Entrada", "Pila (Antes)", "Cima Pila", "Acción / Regla Aplicada", "Pila (Después)", "Estado Sig.", "Resultado"
        };
        traceTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        traceTable = new JTable(traceTableModel);
        traceTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        traceTable.setRowHeight(25);
        traceTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        traceTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        traceTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        traceTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        traceTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        traceTable.getColumnModel().getColumn(5).setPreferredWidth(270);
        traceTable.getColumnModel().getColumn(6).setPreferredWidth(180);
        traceTable.getColumnModel().getColumn(7).setPreferredWidth(85);
        traceTable.getColumnModel().getColumn(8).setPreferredWidth(80);

        // Monospace renderer for stacks
        DefaultTableCellRenderer monoRenderer = new DefaultTableCellRenderer();
        monoRenderer.setFont(new Font("Consolas", Font.PLAIN, 11));
        traceTable.getColumnModel().getColumn(3).setCellRenderer(monoRenderer);
        traceTable.getColumnModel().getColumn(6).setCellRenderer(monoRenderer);

        panel.add(new JScrollPane(traceTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGrammarPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.45);

        // Top: Rules & Sets
        JPanel topSubPanel = new JPanel(new BorderLayout());
        topSubPanel.setBorder(new TitledBorder("Reglas de Producción Formales y Conjuntos FIRST / FOLLOW"));
        grammarArea = new JTextArea();
        grammarArea.setEditable(false);
        grammarArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        topSubPanel.add(new JScrollPane(grammarArea), BorderLayout.CENTER);
        splitPane.setTopComponent(topSubPanel);

        // Bottom: LL(1) Table
        JPanel bottomSubPanel = new JPanel(new BorderLayout());
        bottomSubPanel.setBorder(new TitledBorder("Tabla de Análisis Predictivo LL(1): M[No Terminal, Terminal]"));

        ll1TableModel = new DefaultTableModel();
        ll1Table = new JTable(ll1TableModel);
        ll1Table.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ll1Table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ll1Table.setRowHeight(24);

        bottomSubPanel.add(new JScrollPane(ll1Table), BorderLayout.CENTER);
        splitPane.setBottomComponent(bottomSubPanel);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormalTuplePanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        formalTupleArea = new JTextArea();
        formalTupleArea.setEditable(false);
        formalTupleArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        formalTupleArea.setText(automaton.getFormalTupleDefinition());

        panel.add(new JScrollPane(formalTupleArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createJFlapPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(new EmptyBorder(6, 6, 6, 6));

        JPanel infoPanel = new JPanel(new BorderLayout());
        JLabel infoLabel = new JLabel("<html><b>Compatibilidad con JFLAP 7.x / 8.x:</b> Este archivo XML define el autómata de pila formal generado a partir de la gramática libre de contexto. Puede guardarlo como archivo <code>.jff</code> e importarlo directamente en JFLAP para simulación visual interactiva.</html>");
        infoLabel.setBorder(new EmptyBorder(4, 4, 8, 4));
        infoPanel.add(infoLabel, BorderLayout.CENTER);

        JButton saveJFlapBtn = new JButton("Guardar archivo .jff en disco");
        saveJFlapBtn.addActionListener(e -> exportToJFlap());
        infoPanel.add(saveJFlapBtn, BorderLayout.EAST);
        panel.add(infoPanel, BorderLayout.NORTH);

        jflapPreviewArea = new JTextArea();
        jflapPreviewArea.setEditable(false);
        jflapPreviewArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        jflapPreviewArea.setText(jflapExporter.toJFlapXml());

        panel.add(new JScrollPane(jflapPreviewArea), BorderLayout.CENTER);
        return panel;
    }

    private void executeAnalysis() {
        String text = inputTextArea.getText();
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese una oración en español.", "Entrada Vacía", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ParseResult result = automaton.parse(text);
        displayResult(result);
    }

    private void displayResult(ParseResult result) {
        // Actualizar Banner
        if (result.isAccepted()) {
            statusBanner.setBackground(new Color(230, 248, 235));
            statusLabel.setForeground(new Color(25, 125, 45));
            statusLabel.setText("✓ ORACIÓN ACEPTADA");
            statusDetailsLabel.setForeground(new Color(20, 100, 35));
            statusDetailsLabel.setText("La estructura sintáctica de la oración es válida según la gramática formal y el autómata de pila.");
        } else {
            statusBanner.setBackground(new Color(253, 237, 237));
            statusLabel.setForeground(new Color(185, 30, 30));
            statusLabel.setText("✗ ORACIÓN RECHAZADA");
            statusDetailsLabel.setForeground(new Color(150, 20, 20));
            statusDetailsLabel.setText(result.getRejectionReason());
        }

        statsLabel.setText(String.format(
                "Métricas: Tiempo: %d ms | Tokens: %d | Palabras reconocidas: %d | Desconocidas: %d | Errores sintácticos: %d | Pasos de pila: %d",
                result.getExecutionTimeMs(),
                result.getTotalTokens(),
                result.getRecognizedWordCount(),
                result.getUnknownWordCount(),
                result.getSyntacticErrorsCount(),
                result.getTrace().size()
        ));

        // Actualizar Tabla Léxica
        lexicalTableModel.setRowCount(0);
        for (Token t : result.getTokens()) {
            String alts = t.getMorphology() != null ? t.getMorphology().getDescripcionAlternativas() : "-";
            lexicalTableModel.addRow(new Object[]{
                    t.getId(),
                    t.getLexeme(),
                    t.getNormalizedText(),
                    t.getType().getDescripcion(),
                    t.getLemma(),
                    t.getFeatures(),
                    t.toGrammarTerminal().getName(),
                    t.getStatus().getEtiqueta(),
                    alts
            });
        }

        // Actualizar Tabla de Traza
        traceTableModel.setRowCount(0);
        for (TransitionStep step : result.getTrace()) {
            traceTableModel.addRow(new Object[]{
                    step.getStepNumber(),
                    step.getCurrentState(),
                    step.getInputSymbolText(),
                    step.getStackBefore(),
                    step.getTopOfStack() != null ? step.getTopOfStack().getName() : "-",
                    step.getActionDescription(),
                    step.getStackAfter(),
                    step.getNextState(),
                    step.isSuccess() ? "OK" : "FALLO"
            });
        }
    }

    private void resetResults() {
        statusBanner.setBackground(new Color(238, 242, 246));
        statusLabel.setForeground(Color.BLACK);
        statusLabel.setText("Listo para analizar.");
        statusDetailsLabel.setText("Ingrese una oración y presione 'Analizar Oración'.");
        statsLabel.setText("Métricas: -");
        lexicalTableModel.setRowCount(0);
        traceTableModel.setRowCount(0);
    }

    private void loadGrammarInfo() {
        FormalGrammar g = automaton.getGrammar();
        StringBuilder sb = new StringBuilder();

        sb.append("--- REGLAS DE PRODUCCIÓN FORMALES ---\n");
        for (GrammarRule r : g.getRules()) {
            sb.append(String.format("(%2d) %-16s -> %s\n", r.getId(), r.getHead().getName(),
                    r.getBody().stream().map(Symbol::getName).collect(Collectors.joining(" "))));
        }

        sb.append("\n--- CONJUNTOS FIRST ---\n");
        for (Map.Entry<Symbol, Set<Symbol>> entry : g.getFirstSets().entrySet()) {
            sb.append(String.format("FIRST(%-16s) = { %s }\n", entry.getKey().getName(),
                    entry.getValue().stream().map(Symbol::getName).collect(Collectors.joining(", "))));
        }

        sb.append("\n--- CONJUNTOS FOLLOW ---\n");
        for (Map.Entry<NonTerminal, Set<Terminal>> entry : g.getFollowSets().entrySet()) {
            sb.append(String.format("FOLLOW(%-15s) = { %s }\n", entry.getKey().getName(),
                    entry.getValue().stream().map(Symbol::getName).collect(Collectors.joining(", "))));
        }

        grammarArea.setText(sb.toString());
        grammarArea.setCaretPosition(0);

        // Tabla LL(1)
        List<Terminal> terminals = new ArrayList<>(g.getTerminals().values());
        String[] cols = new String[terminals.size() + 1];
        cols[0] = "No Terminal";
        for (int i = 0; i < terminals.size(); i++) {
            cols[i + 1] = terminals.get(i).getName();
        }

        DefaultTableModel model = new DefaultTableModel(cols, 0);
        for (NonTerminal nt : g.getNonTerminals().values()) {
            Object[] row = new Object[cols.length];
            row[0] = nt.getName();
            for (int i = 0; i < terminals.size(); i++) {
                GrammarRule rule = g.getTableEntry(nt, terminals.get(i));
                row[i + 1] = rule != null ? "(" + rule.getId() + ") " + rule.getBody().stream().map(Symbol::getName).collect(Collectors.joining(" ")) : "-";
            }
            model.addRow(row);
        }

        ll1Table.setModel(model);
        ll1Table.getColumnModel().getColumn(0).setPreferredWidth(140);
        for (int i = 1; i < cols.length; i++) {
            ll1Table.getColumnModel().getColumn(i).setPreferredWidth(160);
        }
    }

    private void exportToJFlap() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("automata_espanol.jff"));
        chooser.setDialogTitle("Exportar Autómata a formato JFLAP");
        int userChoice = chooser.showSaveDialog(this);
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                jflapExporter.exportToFile(file);
                JOptionPane.showMessageDialog(this,
                        "El autómata fue exportado con éxito a:\n" + file.getAbsolutePath(),
                        "Exportación Exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error al exportar archivo JFLAP: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private ExampleItem[] getExampleItems() {
        return new ExampleItem[]{
                new ExampleItem("--- Seleccionar Ejemplo ---", ""),
                new ExampleItem("1. [Válida] Simple: El perro corre rápido.", "El perro corre rápido."),
                new ExampleItem("2. [Válida] Con adjetivo: La estudiante inteligente lee un libro interesante.", "La estudiante inteligente lee un libro interesante."),
                new ExampleItem("3. [Válida] Sintagma prep: El gato duerme en la casa.", "El gato duerme en la casa."),
                new ExampleItem("4. [Válida] Coordinada: María come manzanas y Pedro bebe agua.", "María come manzanas y Pedro bebe agua."),
                new ExampleItem("5. [Válida] Adverbio modo: Los niños juegan alegremente con sus amigos.", "Los niños juegan alegremente con sus amigos."),
                new ExampleItem("6. [Válida] Pronombre sujeto: Ella canta una canción dulce.", "Ella canta una canción dulce."),
                new ExampleItem("7. [Válida] Adverbio preverbal: El perro alegremente corre por el parque.", "El perro alegremente corre por el parque."),
                new ExampleItem("8. [Válida] Múltiples modif: El tren veloz llega a la estación puntualmente.", "El tren veloz llega a la estación puntualmente."),
                new ExampleItem("9. [Inválida] Orden erróneo: Perro el corre.", "Perro el corre."),
                new ExampleItem("10. [Inválida] Sin punto: El perro corre", "El perro corre"),
                new ExampleItem("11. [Inválida] Palabra desconocida: La qwertyuiop corre.", "La qwertyuiop corre.")
        };
    }

    private static class ExampleItem {
        final String label;
        final String text;

        ExampleItem(String label, String text) {
            this.label = label;
            this.text = text;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
