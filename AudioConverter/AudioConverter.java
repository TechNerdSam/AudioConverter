import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.*;

public class AudioConverter extends JFrame {

    // ==================== CONSTANTES DE DESIGN ====================
    private static final Color PRIMARY_COLOR = new Color(99, 102, 241);
    private static final Color PRIMARY_DARK = new Color(79, 70, 229);
    private static final Color BACKGROUND_DARK = new Color(15, 23, 42);
    private static final Color BACKGROUND_MEDIUM = new Color(30, 41, 59);
    private static final Color BACKGROUND_LIGHT = new Color(51, 65, 85);
    private static final Color TEXT_PRIMARY = new Color(248, 250, 252);
    private static final Color TEXT_SECONDARY = new Color(203, 213, 225);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color WARNING_COLOR = new Color(251, 146, 60);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color BORDER_COLOR = new Color(71, 85, 105);
    private static final Color FIELD_TEXT_COLOR = Color.BLACK; // Texte noir pour les champs

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    // ==================== COMPOSANTS UI ====================
    private JPanel mainPanel;
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private JPanel footerPanel;

    private DefaultListModel<AudioFile> fileListModel;
    private JList<AudioFile> fileList;
    private JComboBox<String> formatComboBox;
    private JComboBox<String> qualityComboBox;
    private JComboBox<String> sampleRateComboBox;
    private JComboBox<String> channelsComboBox;
    private JSlider volumeSlider;
    private JProgressBar globalProgressBar;
    private JLabel statusLabel;
    private JLabel fileCountLabel;
    private JTextArea logArea;
    private JButton addFilesButton;
    private JButton removeFilesButton;
    private JButton clearAllButton;
    private JButton convertButton;
    private JButton stopButton;
    private JButton settingsButton;

    // ==================== VARIABLES D'ÉTAT ====================
    private List<AudioFile> audioFiles;
    private ExecutorService executorService;
    private volatile boolean isConverting = false;
    private File outputDirectory;
    private ConversionSettings settings;
    private ConversionStatistics statistics;

    // ==================== FORMATS SUPPORTÉS ====================
    private static final String[] SUPPORTED_INPUT_FORMATS = {
            "wav", "aiff", "au"
    };

    private static final String[] OUTPUT_FORMATS = {
            "WAV", "AIFF", "AU"
    };

    private static final String[] QUALITY_PRESETS = {
            "Basse (8 bits)", "Moyenne (16 bits)", "Haute (24 bits)", "Maximum (32 bits)"
    };

    private static final String[] SAMPLE_RATES = {
            "8000 Hz", "11025 Hz", "16000 Hz", "22050 Hz",
            "44100 Hz", "48000 Hz", "96000 Hz", "192000 Hz"
    };

    private static final String[] CHANNEL_OPTIONS = {
            "Mono (1 canal)", "Stéréo (2 canaux)", "Conserver original"
    };

    // ==================== CONSTRUCTEUR ====================
    public AudioConverter() {
        initializeVariables();
        applyModernLookAndFeel();
        initializeUI();
        setupEventHandlers();
    }

    private void initializeVariables() {
        audioFiles = new ArrayList<>();
        fileListModel = new DefaultListModel<>();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        outputDirectory = new File(System.getProperty("user.home"), "AudioConverter_Output");
        settings = new ConversionSettings();
        statistics = new ConversionStatistics();

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }
    }

    private void initializeUI() {
        setTitle("AudioConverter Pro 2025 - Elite Edition");
        setSize(1400, 900);
        setMinimumSize(new Dimension(1200, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(BACKGROUND_DARK);

        createHeader();
        createSidebar();
        createContent();
        createFooter();

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(sidebarPanel, BorderLayout.WEST);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_MEDIUM);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("AudioConverter Pro");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Convertisseur Audio Professionnel - Edition Elite 2025");
        subtitleLabel.setFont(FONT_SUBTITLE);
        subtitleLabel.setForeground(TEXT_SECONDARY);

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        settingsButton = createModernButton("Paramètres", PRIMARY_COLOR, false);
        JButton aboutButton = createModernButton("À propos", BACKGROUND_LIGHT, false);
        JButton helpButton = createModernButton("Aide", BACKGROUND_LIGHT, false);

        aboutButton.addActionListener(e -> showAboutDialog());
        helpButton.addActionListener(e -> showHelpDialog());
        settingsButton.addActionListener(e -> showSettingsDialog());

        actionPanel.add(helpButton);
        actionPanel.add(aboutButton);
        actionPanel.add(settingsButton);

        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(actionPanel, BorderLayout.EAST);
    }

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(BACKGROUND_MEDIUM);
        sidebarPanel.setPreferredSize(new Dimension(320, 0));
        sidebarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        sidebarPanel.add(createSectionTitle("Gestion des fichiers"));
        sidebarPanel.add(Box.createVerticalStrut(15));

        addFilesButton = createModernButton("+ Ajouter des fichiers", PRIMARY_COLOR, true);
        addFilesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(addFilesButton);
        sidebarPanel.add(Box.createVerticalStrut(10));

        removeFilesButton = createModernButton("- Retirer la sélection", WARNING_COLOR, true);
        removeFilesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(removeFilesButton);
        sidebarPanel.add(Box.createVerticalStrut(10));

        clearAllButton = createModernButton("Vider la liste", ERROR_COLOR, true);
        clearAllButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(clearAllButton);

        sidebarPanel.add(Box.createVerticalStrut(30));
        sidebarPanel.add(createSeparator());
        sidebarPanel.add(Box.createVerticalStrut(30));

        sidebarPanel.add(createSectionTitle("Paramètres de conversion"));
        sidebarPanel.add(Box.createVerticalStrut(15));

        sidebarPanel.add(createLabel("Format de sortie :"));
        sidebarPanel.add(Box.createVerticalStrut(8));
        formatComboBox = createModernComboBox(OUTPUT_FORMATS);
        formatComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(formatComboBox);
        sidebarPanel.add(Box.createVerticalStrut(15));

        sidebarPanel.add(createLabel("Qualité audio :"));
        sidebarPanel.add(Box.createVerticalStrut(8));
        qualityComboBox = createModernComboBox(QUALITY_PRESETS);
        qualityComboBox.setSelectedIndex(1); // 16 bits par défaut
        qualityComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(qualityComboBox);
        sidebarPanel.add(Box.createVerticalStrut(15));

        sidebarPanel.add(createLabel("Taux d'échantillonnage :"));
        sidebarPanel.add(Box.createVerticalStrut(8));
        sampleRateComboBox = createModernComboBox(SAMPLE_RATES);
        sampleRateComboBox.setSelectedIndex(4); // 44100 Hz par défaut
        sampleRateComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(sampleRateComboBox);
        sidebarPanel.add(Box.createVerticalStrut(15));

        sidebarPanel.add(createLabel("Canaux audio :"));
        sidebarPanel.add(Box.createVerticalStrut(8));
        channelsComboBox = createModernComboBox(CHANNEL_OPTIONS);
        channelsComboBox.setSelectedIndex(2); // Conserver original
        channelsComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(channelsComboBox);
        sidebarPanel.add(Box.createVerticalStrut(15));

        sidebarPanel.add(createLabel("Ajustement du volume :"));
        sidebarPanel.add(Box.createVerticalStrut(8));

        volumeSlider = new JSlider(0, 200, 100);
        volumeSlider.setOpaque(false);
        volumeSlider.setForeground(PRIMARY_COLOR);
        volumeSlider.setMajorTickSpacing(50);
        volumeSlider.setMinorTickSpacing(10);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dictionary<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, createSliderLabel("0%"));
        labelTable.put(100, createSliderLabel("100%"));
        labelTable.put(200, createSliderLabel("200%"));
        volumeSlider.setLabelTable(labelTable);

        sidebarPanel.add(volumeSlider);

        sidebarPanel.add(Box.createVerticalStrut(30));
        sidebarPanel.add(createSeparator());
        sidebarPanel.add(Box.createVerticalStrut(30));

        sidebarPanel.add(createSectionTitle("Statistiques"));
        sidebarPanel.add(Box.createVerticalStrut(15));

        JPanel statsPanel = createStatsPanel();
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(statsPanel);

        sidebarPanel.add(Box.createVerticalGlue());
    }

    private void createContent() {
        contentPanel = new JPanel(new BorderLayout(0, 0));
        contentPanel.setBackground(BACKGROUND_DARK);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel fileListPanel = new JPanel(new BorderLayout(0, 15));
        fileListPanel.setOpaque(false);

        JLabel fileListTitle = createSectionTitle("Fichiers à convertir");
        fileCountLabel = createLabel("0 fichier(s)");
        fileCountLabel.setFont(FONT_SUBTITLE);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(fileListTitle, BorderLayout.WEST);
        titleRow.add(fileCountLabel, BorderLayout.EAST);

        fileListPanel.add(titleRow, BorderLayout.NORTH);

        fileList = new JList<>(fileListModel);
        fileList.setBackground(BACKGROUND_MEDIUM);
        fileList.setForeground(TEXT_PRIMARY);
        fileList.setFont(FONT_LABEL);
        fileList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        fileList.setCellRenderer(new AudioFileCellRenderer());
        fileList.setFixedCellHeight(80);

        JScrollPane fileScrollPane = new JScrollPane(fileList);
        fileScrollPane.setBorder(createModernBorder());
        fileScrollPane.getViewport().setBackground(BACKGROUND_MEDIUM);
        fileScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        fileScrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        fileListPanel.add(fileScrollPane, BorderLayout.CENTER);

        JPanel logPanel = new JPanel(new BorderLayout(0, 15));
        logPanel.setOpaque(false);
        logPanel.setPreferredSize(new Dimension(0, 200));

        JLabel logTitle = createSectionTitle("Journal de conversion");
        logPanel.add(logTitle, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(BACKGROUND_MEDIUM);
        logArea.setForeground(TEXT_SECONDARY);
        logArea.setFont(FONT_SMALL);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setBorder(createModernBorder());
        logScrollPane.getVerticalScrollBar().setUI(new ModernScrollBarUI());
        logScrollPane.getHorizontalScrollBar().setUI(new ModernScrollBarUI());

        logPanel.add(logScrollPane, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fileListPanel, logPanel);
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setUI(new ModernSplitPaneUI());

        contentPanel.add(splitPane, BorderLayout.CENTER);
    }

    private void createFooter() {
        footerPanel = new JPanel(new BorderLayout(20, 0));
        footerPanel.setBackground(BACKGROUND_MEDIUM);
        footerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JPanel progressPanel = new JPanel(new BorderLayout(0, 10));
        progressPanel.setOpaque(false);

        statusLabel = createLabel("Prêt à convertir");
        statusLabel.setFont(FONT_SUBTITLE);
        progressPanel.add(statusLabel, BorderLayout.NORTH);

        globalProgressBar = new JProgressBar(0, 100);
        globalProgressBar.setStringPainted(true);
        globalProgressBar.setFont(FONT_LABEL);
        globalProgressBar.setPreferredSize(new Dimension(0, 30));
        globalProgressBar.setBackground(BACKGROUND_LIGHT);
        globalProgressBar.setForeground(PRIMARY_COLOR);
        globalProgressBar.setBorder(createModernBorder());
        globalProgressBar.setUI(new ModernProgressBarUI());

        progressPanel.add(globalProgressBar, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        controlPanel.setOpaque(false);

        convertButton = createModernButton("CONVERTIR", SUCCESS_COLOR, false);
        convertButton.setPreferredSize(new Dimension(180, 45));
        convertButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        stopButton = createModernButton("ARRÊTER", ERROR_COLOR, false);
        stopButton.setPreferredSize(new Dimension(150, 45));
        stopButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        stopButton.setEnabled(false);

        controlPanel.add(stopButton);
        controlPanel.add(convertButton);

        footerPanel.add(progressPanel, BorderLayout.CENTER);
        footerPanel.add(controlPanel, BorderLayout.EAST);
    }

    // ==================== COMPOSANTS MODERNES ====================

    private JButton createModernButton(String text, Color color, boolean fullWidth) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2.setColor(color.brighter());
                } else {
                    g2.setColor(color);
                }

                if (!isEnabled()) {
                    g2.setColor(BACKGROUND_LIGHT);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(FONT_BUTTON);
        button.setForeground(TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(fullWidth ? 280 : 140, 40));
        if (fullWidth) {
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        }
        return button;
    }

    private JComboBox<String> createModernComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(FONT_LABEL);
        comboBox.setBackground(Color.WHITE); // Fond blanc
        comboBox.setForeground(FIELD_TEXT_COLOR); // Texte noir
        comboBox.setBorder(createModernBorder());
        comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        comboBox.setPreferredSize(new Dimension(0, 35));
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Rendre le texte de la liste déroulante également noir
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (!isSelected) {
                    setForeground(FIELD_TEXT_COLOR);
                    setBackground(Color.WHITE);
                }
                return this;
            }
        });
        
        return comboBox;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createSliderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_SECONDARY);
        return label;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 15));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(BORDER_COLOR);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        return separator;
    }

    private Border createModernBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        );
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                createModernBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        panel.add(createLabel("Total converti :"));
        statistics.totalConvertedLabel = createLabel("0");
        panel.add(statistics.totalConvertedLabel);

        panel.add(createLabel("Réussis :"));
        statistics.successLabel = createLabel("0");
        statistics.successLabel.setForeground(SUCCESS_COLOR);
        panel.add(statistics.successLabel);

        panel.add(createLabel("Échoués :"));
        statistics.failedLabel = createLabel("0");
        statistics.failedLabel.setForeground(ERROR_COLOR);
        panel.add(statistics.failedLabel);

        panel.add(createLabel("Taille totale :"));
        statistics.totalSizeLabel = createLabel("0 MB");
        panel.add(statistics.totalSizeLabel);

        return panel;
    }

    // ==================== EVENTS ====================

    private void setupEventHandlers() {
        addFilesButton.addActionListener(e -> addFiles());
        removeFilesButton.addActionListener(e -> removeSelectedFiles());
        clearAllButton.addActionListener(e -> clearAllFiles());
        convertButton.addActionListener(e -> startConversion());
        stopButton.addActionListener(e -> stopConversion());

        new FileDrop(fileList, files -> {
            for (File file : files) {
                if (isAudioFile(file)) {
                    addAudioFile(file);
                }
            }
            updateFileCount();
        });
    }

    private void addFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Fichiers audio (" + String.join(", ", SUPPORTED_INPUT_FORMATS) + ")",
                SUPPORTED_INPUT_FORMATS
        ));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                addAudioFile(file);
            }
            updateFileCount();
            log("Ajout de " + selectedFiles.length + " fichier(s).");
        }
    }

    private void addAudioFile(File file) {
        AudioFile audioFile = new AudioFile(file);
        audioFiles.add(audioFile);
        fileListModel.addElement(audioFile);
    }

    private void removeSelectedFiles() {
        List<AudioFile> selectedFiles = fileList.getSelectedValuesList();
        if (selectedFiles.isEmpty()) {
            showWarning("Aucun fichier sélectionné.");
            return;
        }
        for (AudioFile file : selectedFiles) {
            audioFiles.remove(file);
            fileListModel.removeElement(file);
        }
        updateFileCount();
        log("Suppression de " + selectedFiles.size() + " fichier(s).");
    }

    private void clearAllFiles() {
        if (audioFiles.isEmpty()) return;

        int result = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment effacer tous les fichiers ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION) {
            audioFiles.clear();
            fileListModel.clear();
            updateFileCount();
            log("Tous les fichiers ont été effacés.");
        }
    }

    private void startConversion() {
        if (audioFiles.isEmpty()) {
            showWarning("Aucun fichier à convertir.");
            return;
        }
        if (isConverting) return;

        isConverting = true;
        convertButton.setEnabled(false);
        stopButton.setEnabled(true);
        addFilesButton.setEnabled(false);
        removeFilesButton.setEnabled(false);
        clearAllButton.setEnabled(false);

        globalProgressBar.setValue(0);
        statusLabel.setText("Conversion en cours...");
        log("Démarrage de la conversion de " + audioFiles.size() + " fichier(s).");

        SwingWorker<Void, ConversionProgress> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                int total = audioFiles.size();
                int completed = 0;
                int success = 0;
                int failed = 0;

                for (AudioFile audioFile : audioFiles) {
                    if (!isConverting) break;

                    audioFile.status = AudioFileStatus.CONVERTING;
                    fileList.repaint();

                    publish(new ConversionProgress(completed, total, audioFile.file.getName(), 0));

                    boolean result = convertAudioFile(audioFile);

                    if (result) {
                        audioFile.status = AudioFileStatus.COMPLETED;
                        success++;
                    } else {
                        audioFile.status = AudioFileStatus.FAILED;
                        failed++;
                    }

                    completed++;
                    publish(new ConversionProgress(completed, total, audioFile.file.getName(), 100));
                    fileList.repaint();
                }

                statistics.totalConverted += completed;
                statistics.successCount += success;
                statistics.failedCount += failed;
                return null;
            }

            @Override
            protected void process(List<ConversionProgress> chunks) {
                ConversionProgress latest = chunks.get(chunks.size() - 1);
                int progress = (int) ((latest.completed * 100.0) / latest.total);
                globalProgressBar.setValue(progress);
                statusLabel.setText(String.format(
                        "Conversion : %d/%d - %s",
                        latest.completed, latest.total, latest.fileName));
            }

            @Override
            protected void done() {
                isConverting = false;
                convertButton.setEnabled(true);
                stopButton.setEnabled(false);
                addFilesButton.setEnabled(true);
                removeFilesButton.setEnabled(true);
                clearAllButton.setEnabled(true);

                globalProgressBar.setValue(100);
                statusLabel.setText("Conversion terminée.");
                log("Conversion terminée. Fichiers sauvegardés dans : " + outputDirectory.getAbsolutePath());

                updateStatistics();
                showSuccess("Conversion terminée avec succès.\n\nFichiers sauvegardés dans :\n" +
                        outputDirectory.getAbsolutePath());
            }
        };

        worker.execute();
    }

    private boolean convertAudioFile(AudioFile audioFile) {
        AudioInputStream sourceStream = null;
        AudioInputStream convertedStream = null;
        
        try {
            log("Conversion du fichier : " + audioFile.file.getName());

            // Lecture du fichier source
            sourceStream = AudioSystem.getAudioInputStream(audioFile.file);
            AudioFormat sourceFormat = sourceStream.getFormat();

            // Paramètres de conversion
            String outputFormat = Objects.requireNonNull(formatComboBox.getSelectedItem()).toString();
            float sampleRate = getSampleRate();
            int channels = getChannels(sourceFormat);
            int sampleSizeInBits = getSampleSize();

            // Création du format cible
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sampleRate,
                    sampleSizeInBits,
                    channels,
                    channels * (sampleSizeInBits / 8),
                    sampleRate,
                    false
            );

            log("Format source : " + sourceFormat.toString());
            log("Format cible : " + targetFormat.toString());

            // Vérifier si la conversion est supportée
            if (!AudioSystem.isConversionSupported(targetFormat, sourceFormat)) {
                log("Conversion directe non supportée, tentative de conversion en deux étapes...");
                
                // Conversion en deux étapes : source -> PCM standard -> cible
                AudioFormat intermediateFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        sourceFormat.getSampleRate(),
                        16,
                        sourceFormat.getChannels(),
                        sourceFormat.getChannels() * 2,
                        sourceFormat.getSampleRate(),
                        false
                );
                
                AudioInputStream intermediateStream = AudioSystem.getAudioInputStream(intermediateFormat, sourceStream);
                convertedStream = AudioSystem.getAudioInputStream(targetFormat, intermediateStream);
            } else {
                convertedStream = AudioSystem.getAudioInputStream(targetFormat, sourceStream);
            }

            // Nom et type du fichier de sortie
            String outputFileName = getOutputFileName(audioFile.file, outputFormat);
            File outputFile = new File(outputDirectory, outputFileName);

            AudioFileFormat.Type fileType = getAudioFileType(outputFormat);
            
            // Écriture du fichier
            AudioSystem.write(convertedStream, fileType, outputFile);

            audioFile.outputFile = outputFile;
            log("Succès : " + outputFileName + " (" + formatFileSize(outputFile.length()) + ")");
            return true;

        } catch (Exception e) {
            log("Erreur sur " + audioFile.file.getName() + " : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Fermeture propre des streams
            try {
                if (convertedStream != null) convertedStream.close();
                if (sourceStream != null) sourceStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopConversion() {
        if (!isConverting) return;

        int result = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous vraiment arrêter la conversion ?",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (result == JOptionPane.YES_OPTION) {
            isConverting = false;
            log("Conversion arrêtée par l'utilisateur.");
        }
    }

    // ==================== UTILITAIRES ====================

    private boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        for (String ext : SUPPORTED_INPUT_FORMATS) {
            if (name.endsWith("." + ext)) return true;
        }
        return false;
    }

    private float getSampleRate() {
        String selected = Objects.requireNonNull(sampleRateComboBox.getSelectedItem()).toString();
        return Float.parseFloat(selected.split(" ")[0]);
    }

    private int getChannels(AudioFormat sourceFormat) {
        int selected = channelsComboBox.getSelectedIndex();
        if (selected == 0) return 1; // Mono
        if (selected == 1) return 2; // Stéréo
        return sourceFormat.getChannels(); // Original
    }

    private int getSampleSize() {
        int selected = qualityComboBox.getSelectedIndex();
        switch (selected) {
            case 0: return 8;  // Basse
            case 1: return 16; // Moyenne
            case 2: return 24; // Haute
            case 3: return 32; // Maximum
            default: return 16;
        }
    }

    private String getOutputFileName(File inputFile, String format) {
        String baseName = inputFile.getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex > 0) baseName = baseName.substring(0, dotIndex);
        return baseName + "_converted." + format.toLowerCase();
    }

    private AudioFileFormat.Type getAudioFileType(String format) {
        switch (format.toUpperCase()) {
            case "WAV":
                return AudioFileFormat.Type.WAVE;
            case "AIFF":
                return AudioFileFormat.Type.AIFF;
            case "AU":
                return AudioFileFormat.Type.AU;
            default:
                return AudioFileFormat.Type.WAVE;
        }
    }

    private void updateFileCount() {
        fileCountLabel.setText(audioFiles.size() + " fichier(s)");
    }

    private void updateStatistics() {
        statistics.totalConvertedLabel.setText(String.valueOf(statistics.totalConverted));
        statistics.successLabel.setText(String.valueOf(statistics.successCount));
        statistics.failedLabel.setText(String.valueOf(statistics.failedCount));

        long totalSize = audioFiles.stream()
                .filter(f -> f.outputFile != null)
                .mapToLong(f -> f.outputFile.length())
                .sum();

        statistics.totalSizeLabel.setText(formatFileSize(totalSize));
    }

    private String formatFileSize(long bytes) {
        DecimalFormat df = new DecimalFormat("#.##");
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024L * 1024L) return df.format(bytes / 1024.0) + " KB";
        if (bytes < 1024L * 1024L * 1024L) return df.format(bytes / (1024.0 * 1024.0)) + " MB";
        return df.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
    }

    private void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + timestamp + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Attention", JOptionPane.WARNING_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Succès", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAboutDialog() {
        String message = "AudioConverter Pro 2025 - Elite Edition\n\n" +
                "Version : 2.0.0\n" +
                "Développé avec Java SE\n\n" +
                "Convertisseur audio professionnel multi-format\n" +
                "avec interface moderne et fonctionnalités avancées.\n\n" +
                "© 2025 - Tous droits réservés.";
        JOptionPane.showMessageDialog(this, message, "À propos", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHelpDialog() {
        String message = "Guide d'utilisation :\n\n" +
                "1. Ajoutez des fichiers audio via le bouton ou par glisser-déposer.\n" +
                "2. Sélectionnez le format de sortie et les paramètres.\n" +
                "3. Cliquez sur CONVERTIR pour lancer la conversion.\n" +
                "4. Les fichiers convertis sont sauvegardés dans :\n   " +
                outputDirectory.getAbsolutePath() + "\n\n" +
                "Formats supportés : WAV, AIFF, AU\n" +
                "Note : Java Sound API supporte nativement ces formats.";
        JOptionPane.showMessageDialog(this, message, "Aide", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSettingsDialog() {
        JDialog dialog = new JDialog(this, "Paramètres", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(BACKGROUND_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = createSectionTitle("Paramètres avancés");
        panel.add(title, BorderLayout.NORTH);

        JPanel settingsPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        settingsPanel.setOpaque(false);

        settingsPanel.add(createLabel("Dossier de sortie :"));
        JButton changeDirButton = createModernButton("Changer...", PRIMARY_COLOR, false);
        changeDirButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                outputDirectory = chooser.getSelectedFile();
                log("Dossier de sortie changé : " + outputDirectory.getAbsolutePath());
            }
        });
        settingsPanel.add(changeDirButton);

        settingsPanel.add(createLabel("Threads de conversion :"));
        JSpinner threadSpinner = new JSpinner(new SpinnerNumberModel(
                Runtime.getRuntime().availableProcessors(), 1, 16, 1));
        threadSpinner.setFont(FONT_LABEL);
        settingsPanel.add(threadSpinner);

        settingsPanel.add(createLabel("Ouvrir le dossier après conversion :"));
        JCheckBox openFolderCheck = new JCheckBox();
        openFolderCheck.setOpaque(false);
        openFolderCheck.setSelected(settings.openFolderAfterConversion);
        settingsPanel.add(openFolderCheck);

        panel.add(settingsPanel, BorderLayout.CENTER);

        JButton closeButton = createModernButton("Fermer", PRIMARY_COLOR, false);
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(closeButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void applyModernLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== CLASSES INTERNES ====================

    private static class AudioFile {
        File file;
        File outputFile;
        AudioFileStatus status;

        AudioFile(File file) {
            this.file = file;
            this.status = AudioFileStatus.PENDING;
        }

        @Override
        public String toString() {
            return file.getName();
        }
    }

    private enum AudioFileStatus {
        PENDING, CONVERTING, COMPLETED, FAILED
    }

    private static class ConversionSettings {
        boolean openFolderAfterConversion = false;
        int threadCount = Runtime.getRuntime().availableProcessors();
    }

    private static class ConversionStatistics {
        int totalConverted = 0;
        int successCount = 0;
        int failedCount = 0;
        JLabel totalConvertedLabel;
        JLabel successLabel;
        JLabel failedLabel;
        JLabel totalSizeLabel;
    }

    private static class ConversionProgress {
        int completed;
        int total;
        String fileName;
        int fileProgress;

        ConversionProgress(int completed, int total, String fileName, int fileProgress) {
            this.completed = completed;
            this.total = total;
            this.fileName = fileName;
            this.fileProgress = fileProgress;
        }
    }

    // ==================== RENDERER ====================

    private class AudioFileCellRenderer extends JPanel implements ListCellRenderer<AudioFile> {
        private JLabel nameLabel;
        private JLabel sizeLabel;
        private JLabel statusLabel;

        AudioFileCellRenderer() {
            setLayout(new BorderLayout(15, 5));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));

            JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
            infoPanel.setOpaque(false);

            nameLabel = new JLabel();
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

            sizeLabel = new JLabel();
            sizeLabel.setFont(FONT_SMALL);

            infoPanel.add(nameLabel);
            infoPanel.add(sizeLabel);

            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            statusPanel.setOpaque(false);

            statusLabel = new JLabel();
            statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            statusPanel.add(statusLabel);

            add(infoPanel, BorderLayout.CENTER);
            add(statusPanel, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends AudioFile> list,
                AudioFile value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            nameLabel.setText(value.file.getName());
            sizeLabel.setText(formatFileSize(value.file.length()));

            switch (value.status) {
                case PENDING:
                    statusLabel.setText("En attente");
                    statusLabel.setForeground(TEXT_SECONDARY);
                    break;
                case CONVERTING:
                    statusLabel.setText("En conversion...");
                    statusLabel.setForeground(PRIMARY_COLOR);
                    break;
                case COMPLETED:
                    statusLabel.setText("Terminé");
                    statusLabel.setForeground(SUCCESS_COLOR);
                    break;
                case FAILED:
                    statusLabel.setText("Échoué");
                    statusLabel.setForeground(ERROR_COLOR);
                    break;
            }

            if (isSelected) {
                setBackground(PRIMARY_DARK);
                nameLabel.setForeground(TEXT_PRIMARY);
                sizeLabel.setForeground(TEXT_SECONDARY);
            } else {
                setBackground(BACKGROUND_MEDIUM);
                nameLabel.setForeground(TEXT_PRIMARY);
                sizeLabel.setForeground(TEXT_SECONDARY);
            }
            return this;
        }
    }

    // ==================== UI PERSONNALISÉE ====================

    private class ModernScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = PRIMARY_COLOR;
            this.trackColor = BACKGROUND_LIGHT;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!c.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(thumbBounds.x, thumbBounds.y,
                    thumbBounds.width, thumbBounds.height, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fillRect(trackBounds.x, trackBounds.y,
                    trackBounds.width, trackBounds.height);
            g2.dispose();
        }
    }

    private class ModernProgressBarUI extends BasicProgressBarUI {
        @Override
        protected void paintDeterminate(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width = progressBar.getWidth();
            int height = progressBar.getHeight();
            int amountFull = getAmountFull(null, width, height);

            g2.setColor(BACKGROUND_LIGHT);
            g2.fillRoundRect(0, 0, width, height, 10, 10);

            if (amountFull > 0) {
                g2.setColor(PRIMARY_COLOR);
                g2.fillRoundRect(0, 0, amountFull, height, 10, 10);
            }

            if (progressBar.isStringPainted()) {
                paintString(g2, 0, 0, width, height, amountFull, null);
            }

            g2.dispose();
        }
    }

    private class ModernSplitPaneUI extends BasicSplitPaneUI {
        @Override
        public BasicSplitPaneDivider createDefaultDivider() {
            return new BasicSplitPaneDivider(this) {
                @Override
                public void paint(Graphics g) {
                    g.setColor(BORDER_COLOR);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            };
        }
    }

    // ==================== DRAG & DROP ====================

    private static class FileDrop {
        FileDrop(Component c, Listener listener) {
            c.setDropTarget(new java.awt.dnd.DropTarget() {
                @Override
                public synchronized void drop(java.awt.dnd.DropTargetDropEvent evt) {
                    try {
                        evt.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
                        @SuppressWarnings("unchecked")
                        java.util.List<File> droppedFiles =
                                (java.util.List<File>) evt.getTransferable().getTransferData(
                                        java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                        listener.filesDropped(droppedFiles.toArray(new File[0]));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        interface Listener {
            void filesDropped(File[] files);
        }
    }

    // ==================== MAIN ====================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                AudioConverter app = new AudioConverter();
                app.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erreur lors du démarrage de l'application :\n" + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}