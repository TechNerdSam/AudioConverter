import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO; // Import for ImageIO

/**
 * <h1>Elegant Audio Converter</h1>
 * An advanced, professional-grade audio conversion tool designed for high-end clients.
 * This version introduces granular control over output audio parameters (bitrate, sample rate, channels),
 * flexible output directory selection for batch conversions, and enhanced error diagnostics
 * for a superior user experience.
 *
 * Cette version introduit un contrôle granulaire sur les paramètres audio de sortie (débit binaire,
 * fréquence d'échantillonnage, canaux), une sélection flexible du répertoire de sortie pour les
 * conversions par lots, et des diagnostics d'erreurs améliorés pour une expérience utilisateur supérieure.
 *
 * @author Gemini (Refactored & Enhanced by AI)
 * @version 5.1.0 (Improved FFmpeg Path Handling, Code Quality, Documentation)
 */
public class AudioConverter extends JFrame {

    // --- UI Constants / Constantes de l'interface utilisateur ---
    // UI Colors / Couleurs de l'interface utilisateur
    private static final Color COLOR_PRIMARY_BG = new Color(250, 248, 245);
    private static final Color COLOR_SECONDARY_PANEL_BG = new Color(240, 235, 230);
    private static final Color COLOR_ACCENT_PRIMARY = new Color(110, 150, 155);
    private static final Color COLOR_ACCENT_SUCCESS = new Color(130, 180, 120);
    private static final Color COLOR_ACCENT_DANGER = new Color(210, 110, 100);
    private static final Color COLOR_TEXT_MAIN = new Color(60, 60, 60);
    private static final Color COLOR_TEXT_FIELD_CONTENT = new Color(80, 80, 80);
    private static final Color COLOR_FIELD_BG_LIGHT = new Color(255, 255, 255);
    private static final Color COLOR_FIELD_BORDER_SUBTLE = new Color(200, 195, 190);
    private static final Color COLOR_STATUS_READY = new Color(160, 210, 150);
    private static final Color COLOR_STATUS_PROCESSING = new Color(255, 200, 120);
    private static final Color COLOR_STATUS_ERROR = new Color(230, 120, 110);
    private static final Color COLOR_SHADOW_SUBTLE = new Color(0, 0, 0, 50);

    // UI Dimensions
    private static final int CORNER_RADIUS_PANEL = 15;
    private static final int CORNER_RADIUS_FIELD = 10;
    private static final int CORNER_RADIUS_BUTTON = 12;

    // Fonts / Polices
    private static final Font FONT_PRIMARY = new Font("Arial", Font.PLAIN, 14);
    private static final Font FONT_BUTTON = FONT_PRIMARY.deriveFont(Font.BOLD, 16);
    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 28);
    private static final Font FONT_STATUS = FONT_PRIMARY.deriveFont(Font.BOLD, 14);
    private static final Font FONT_FIELD = FONT_PRIMARY.deriveFont(Font.PLAIN, 14);
    private static final Font FONT_SMALL = FONT_PRIMARY.deriveFont(Font.PLAIN, 11);

    // Supported Audio Formats / Formats audio pris en charge
    private static final String[] SUPPORTED_FORMATS = {"mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "aiff"};
    // Channel Options / Options de canal
    private static final String[] CHANNELS_OPTIONS = {"Original", "Mono", "Stéréo"};

    // Default Background Image URL (Local or Remote) / URL de l'image de fond par défaut (Locale ou distante)
    private static final String DEFAULT_BACKGROUND_IMAGE_URL = "https://image.noelshack.com/fichiers/2025/22/7/1748758758-codioful-formerly-gradienta-leg68prxa6y-unsplash-1.jpg";

    // --- FFmpeg Path Configuration Constants ---
    // Preference key for custom FFmpeg path / Clé de préférence pour le chemin FFmpeg personnalisé
    private static final String PREF_KEY_CUSTOM_FFMPEG_PATH = "customFfmpegPath";
    // Preference keys for advanced settings / Clés de préférence pour les paramètres avancés
    private static final String PREF_KEY_CUSTOM_SETTINGS_ENABLED = "customSettingsEnabled";
    private static final String PREF_KEY_CUSTOM_BITRATE = "customBitrate";
    private static final String PREF_KEY_CUSTOM_SAMPLERATE = "customSampleRate";
    private static final String PREF_KEY_CUSTOM_CHANNELS = "customChannels";

    /*
     * The hardcoded FFmpeg directory path has been removed for security and portability reasons.
     * Le chemin du répertoire FFmpeg codé en dur a été supprimé pour des raisons de sécurité et de portabilité.
     * The application will now primarily rely on the user-configured path or the system's PATH environment variable.
     * L'application s'appuiera désormais principalement sur le chemin configuré par l'utilisateur ou sur la variable d'environnement PATH du système.
     */
    // private static final String HARDCODED_FFMPEG_DIRECTORY_PATH = "C:\\Users\\Samyn\\OneDrive\\Applications\\Mes Projets informatiques\\Mes_Projets_Java\\Mes_projets_Informatiques_Java\\Mes_Projets_Logiciels\\AudioConverter";

    // Effective FFmpeg executable path used by the application / Chemin effectif de l'exécutable FFmpeg utilisé par l'application
    private static String FFMPEG_EXECUTABLE;
    // User-defined FFmpeg path via the interface / Chemin FFmpeg défini par l'utilisateur via l'interface
    private static String USER_CUSTOM_FFMPEG_PATH = "";
    // Details of FFmpeg search for diagnostics / Détails de la recherche de FFmpeg pour les diagnostics
    private static String LOCAL_FFMPEG_SEARCH_DETAILS = "";


    // --- UI Components / Composants de l'interface utilisateur ---
    private JLabel titleLabel;
    private JTextArea fileListArea;
    private JScrollPane fileListScrollPane;
    private JButton addFilesButton;
    private JButton clearFilesButton;
    private JComboBox<String> outputFormatComboBox;
    private JTextField outputDirectoryField;
    private JButton browseOutputButton;
    private JButton convertButton;
    private JLabel statusLabel;
    private JPanel contentPanel;
    private JLabel backgroundLabel; // For background image / Pour l'image de fond
    private JButton advancedSettingsButton;
    private JPanel advancedSettingsPanel;
    private JCheckBox customSettingsCheckBox;
    private JTextField bitrateField;
    private JTextField sampleRateField;
    private JComboBox<String> channelsComboBox;
    private JButton ffmpegPathButton;
    private JLabel ffmpegStatusLabel;


    // --- Application State / État de l'application ---
    // List of files selected for conversion / Liste des fichiers sélectionnés pour la conversion
    private List<File> filesToConvert;

    /**
     * Constructor for the AudioConverter application.
     * Initializes the UI, loads settings, and sets up event listeners.
     *
     * Constructeur de l'application AudioConverter.
     * Initialise l'interface utilisateur, charge les paramètres et configure les écouteurs d'événements.
     */
    public AudioConverter() {
        this.filesToConvert = new ArrayList<>();
        configureFrame();
        loadBackgroundImage();
        initComponents();
        loadAdvancedSettingsFromPreferences(); // Load preferences after initComponents
        layoutComponents();
        addEventListeners();
        pack();
        setLocationRelativeTo(null); // Center the window / Centrer la fenêtre
        checkFfmpegOnStartup(); // Check FFmpeg availability on startup / Vérifier la disponibilité de FFmpeg au démarrage
    }

    /**
     * Configures the main JFrame properties.
     * Sets title, default close operation, and initial background color.
     *
     * Configure les propriétés principales de la JFrame.
     * Définit le titre, l'opération de fermeture par défaut et la couleur de fond initiale.
     */
    private void configureFrame() {
        setTitle("Elegant Audio Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setBackground(COLOR_PRIMARY_BG);
        setLayout(new BorderLayout()); // Use BorderLayout for main frame / Utiliser BorderLayout pour le cadre principal
    }

    /**
     * Loads and sets a background image for the application.
     * The image is fetched from a URL, and if successful, is scaled and set as the background.
     *
     * Charge et définit une image de fond pour l'application.
     * L'image est récupérée depuis une URL, et si cela réussit, elle est redimensionnée et définie comme arrière-plan.
     */
    private void loadBackgroundImage() {
        backgroundLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getIcon() != null) {
                    Image img = ((ImageIcon) getIcon()).getImage();
                    // Scale the image to fill the label, maintaining aspect ratio
                    // Redimensionner l'image pour remplir le JLabel, en maintenant les proportions
                    int iw = img.getWidth(this);
                    int ih = img.getHeight(this);
                    int cw = getWidth();
                    int ch = getHeight();

                    if (iw <= 0 || ih <= 0 || cw <= 0 || ch <= 0) {
                        return; // Avoid division by zero or invalid dimensions
                    }

                    Image scaledImage;
                    // Calculate scale to cover the component
                    double scale = Math.max((double) cw / iw, (double) ch / ih);
                    int scaledWidth = (int) (iw * scale);
                    int scaledHeight = (int) (ih * scale);

                    // Determine position to center the scaled image
                    int x = (cw - scaledWidth) / 2;
                    int y = (ch - scaledHeight) / 2;

                    g.drawImage(img, x, y, scaledWidth, scaledHeight, this);
                }
            }
        };
        backgroundLabel.setLayout(new BorderLayout()); // Use BorderLayout for components on top of background
        add(backgroundLabel, BorderLayout.CENTER); // Add to the center of the frame

        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() {
                try {
                    URL imageUrl = new URL(DEFAULT_BACKGROUND_IMAGE_URL);
                    return new ImageIcon(ImageIO.read(imageUrl));
                } catch (IOException e) {
                    System.err.println("ERROR: Could not load background image from URL: " + DEFAULT_BACKGROUND_IMAGE_URL + " - " + e.getMessage());
                    // Fallback: Attempt to load a local image if URL fails or is not accessible
                    // Retour : Tenter de charger une image locale si l'URL échoue ou n'est pas accessible
                    try {
                        // Adjust this path for local testing if needed. This assumes a 'resources' directory or similar packaging.
                        // Ajustez ce chemin pour les tests locaux si nécessaire. Cela suppose un répertoire 'resources' ou un empaquetage similaire.
                        return new ImageIcon(getClass().getResource("/codioful-formerly-gradienta-leg68prxa6y-unsplash-1.jpg"));
                    } catch (Exception ex) {
                        System.err.println("ERROR: Could not load local background image: " + ex.getMessage());
                        return null; // No image available / Aucune image disponible
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        backgroundLabel.setIcon(icon);
                        backgroundLabel.repaint(); // Ensure the background is repainted / S'assurer que le fond est repeint
                    } else {
                        System.err.println("WARNING: No background image could be loaded. Using default background color.");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    System.err.println("ERROR: Background image loading interrupted or failed: " + e.getMessage());
                }
            }
        }.execute();
    }


    /**
     * Initializes all UI components and sets their default properties.
     *
     * Initialise tous les composants de l'interface utilisateur et définit leurs propriétés par défaut.
     */
    private void initComponents() {
        // Main content panel setup / Configuration du panneau de contenu principal
        contentPanel = new JPanel();
        contentPanel.setOpaque(false); // Make it transparent to show background image
        contentPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for flexible arrangement
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Add some padding
        backgroundLabel.add(contentPanel, BorderLayout.CENTER); // Add to the background label

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Padding between components / Espacement entre les composants
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally by default / Remplir horizontalement par défaut

        // Title Label / Titre
        titleLabel = new JLabel("Elegant Audio Converter", SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_TEXT_MAIN);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4; // Span across all columns / S'étendre sur toutes les colonnes
        contentPanel.add(titleLabel, gbc);

        // File List Area / Zone de liste de fichiers
        fileListArea = new JTextArea(10, 50); // Rows, Columns
        fileListArea.setEditable(false);
        fileListArea.setFont(FONT_FIELD);
        fileListArea.setBackground(COLOR_FIELD_BG_LIGHT);
        fileListArea.setForeground(COLOR_TEXT_FIELD_CONTENT);
        fileListArea.setBorder(BorderFactory.createLineBorder(COLOR_FIELD_BORDER_SUBTLE, 1)); // Subtle border
        fileListArea.setLineWrap(true);
        fileListArea.setWrapStyleWord(true);
        fileListArea.setDropTarget(new FileDropTarget(this::addFiles)); // Enable drag and drop
        fileListScrollPane = new JScrollPane(fileListArea);
        fileListScrollPane.setPreferredSize(new Dimension(600, 150));
        fileListScrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_FIELD_BORDER_SUBTLE, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0; // Allow horizontal expansion
        contentPanel.add(fileListScrollPane, gbc);

        // File Management Buttons / Boutons de gestion des fichiers
        JPanel fileButtonsPanel = new JPanel(new GridLayout(2, 1, 0, 10)); // Vertical layout with gap
        fileButtonsPanel.setOpaque(false);

        addFilesButton = createStyledButton("Ajouter Fichiers / Add Files", COLOR_ACCENT_PRIMARY);
        clearFilesButton = createStyledButton("Effacer Liste / Clear List", COLOR_ACCENT_DANGER);

        fileButtonsPanel.add(addFilesButton);
        fileButtonsPanel.add(clearFilesButton);
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.BOTH; // Fill both vertically and horizontally
        contentPanel.add(fileButtonsPanel, gbc);


        // Output Format Selection / Sélection du format de sortie
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE; // Do not fill horizontally

        contentPanel.add(new JLabel("Format de Sortie / Output Format:"), gbc);
        outputFormatComboBox = new JComboBox<>(SUPPORTED_FORMATS);
        outputFormatComboBox.setFont(FONT_FIELD);
        outputFormatComboBox.setBackground(COLOR_FIELD_BG_LIGHT);
        outputFormatComboBox.setForeground(COLOR_TEXT_FIELD_CONTENT);
        outputFormatComboBox.setRenderer(new CustomComboBoxRenderer(COLOR_FIELD_BG_LIGHT, COLOR_TEXT_FIELD_CONTENT));
        outputFormatComboBox.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(outputFormatComboBox, gbc);


        // Output Directory / Répertoire de sortie
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Répertoire de Sortie / Output Directory:"), gbc);

        outputDirectoryField = new JTextField();
        outputDirectoryField.setEditable(false);
        outputDirectoryField.setFont(FONT_FIELD);
        outputDirectoryField.setBackground(COLOR_FIELD_BG_LIGHT);
        outputDirectoryField.setForeground(COLOR_TEXT_FIELD_CONTENT);
        outputDirectoryField.setBorder(BorderFactory.createLineBorder(COLOR_FIELD_BORDER_SUBTLE, 1));
        gbc.gridx = 1;
        gbc.gridwidth = 2; // Span two columns
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(outputDirectoryField, gbc);

        browseOutputButton = createStyledButton("Parcourir / Browse", COLOR_ACCENT_PRIMARY);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(browseOutputButton, gbc);

        // Advanced Settings Button / Bouton Paramètres Avancés
        advancedSettingsButton = createStyledButton("Paramètres Avancés / Advanced Settings", COLOR_ACCENT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(advancedSettingsButton, gbc);

        // Advanced Settings Panel (initially hidden) / Panneau des paramètres avancés (initialement caché)
        advancedSettingsPanel = new JPanel(new GridBagLayout());
        advancedSettingsPanel.setOpaque(false);
        advancedSettingsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_FIELD_BORDER_SUBTLE),
                "Paramètres Avancés / Advanced Settings",
                0, 0, FONT_PRIMARY.deriveFont(Font.BOLD, 12), COLOR_TEXT_MAIN));
        advancedSettingsPanel.setVisible(false); // Hidden by default / Caché par défaut

        GridBagConstraints gbcAdvanced = new GridBagConstraints();
        gbcAdvanced.insets = new Insets(5, 5, 5, 5);
        gbcAdvanced.fill = GridBagConstraints.HORIZONTAL;

        // Custom Settings CheckBox / Case à cocher Paramètres personnalisés
        customSettingsCheckBox = new JCheckBox("Activer les paramètres personnalisés / Enable Custom Settings");
        customSettingsCheckBox.setFont(FONT_FIELD);
        customSettingsCheckBox.setOpaque(false);
        customSettingsCheckBox.setForeground(COLOR_TEXT_MAIN);
        gbcAdvanced.gridx = 0;
        gbcAdvanced.gridy = 0;
        gbcAdvanced.gridwidth = 2;
        advancedSettingsPanel.add(customSettingsCheckBox, gbcAdvanced);

        // Bitrate Field / Champ Débit binaire
        gbcAdvanced.gridy = 1;
        gbcAdvanced.gridwidth = 1;
        advancedSettingsPanel.add(new JLabel("Débit Binaire (kbps) / Bitrate (kbps):"), gbcAdvanced);
        bitrateField = new JTextField("192"); // Default value / Valeur par défaut
        bitrateField.setFont(FONT_FIELD);
        bitrateField.setBackground(COLOR_FIELD_BG_LIGHT);
        bitrateField.setForeground(COLOR_TEXT_FIELD_CONTENT);
        bitrateField.setBorder(BorderFactory.createLineBorder(COLOR_FIELD_BORDER_SUBTLE, 1));
        gbcAdvanced.gridx = 1;
        advancedSettingsPanel.add(bitrateField, gbcAdvanced);

        // Sample Rate Field / Champ Fréquence d'échantillonnage
        gbcAdvanced.gridx = 0;
        gbcAdvanced.gridy = 2;
        advancedSettingsPanel.add(new JLabel("Fréquence d'Échantillonnage (Hz) / Sample Rate (Hz):"), gbcAdvanced);
        sampleRateField = new JTextField("44100"); // Default value / Valeur par défaut
        sampleRateField.setFont(FONT_FIELD);
        sampleRateField.setBackground(COLOR_FIELD_BG_LIGHT);
        sampleRateField.setForeground(COLOR_TEXT_FIELD_CONTENT);
        sampleRateField.setBorder(BorderFactory.createLineBorder(COLOR_FIELD_BORDER_SUBTLE, 1));
        gbcAdvanced.gridx = 1;
        advancedSettingsPanel.add(sampleRateField, gbcAdvanced);

        // Channels ComboBox / Liste déroulante des canaux
        gbcAdvanced.gridx = 0;
        gbcAdvanced.gridy = 3;
        advancedSettingsPanel.add(new JLabel("Canaux / Channels:"), gbcAdvanced);
        channelsComboBox = new JComboBox<>(CHANNELS_OPTIONS);
        channelsComboBox.setFont(FONT_FIELD);
        channelsComboBox.setBackground(COLOR_FIELD_BG_LIGHT);
        channelsComboBox.setForeground(COLOR_TEXT_FIELD_CONTENT);
        channelsComboBox.setRenderer(new CustomComboBoxRenderer(COLOR_FIELD_BG_LIGHT, COLOR_TEXT_FIELD_CONTENT));
        gbcAdvanced.gridx = 1;
        advancedSettingsPanel.add(channelsComboBox, gbcAdvanced);

        gbc.gridy = 5; // Position below advanced settings toggle button
        gbc.gridwidth = 4;
        contentPanel.add(advancedSettingsPanel, gbc);

        // FFmpeg Path Configuration / Configuration du chemin FFmpeg
        ffmpegPathButton = createStyledButton("Configurer Chemin FFmpeg / Configure FFmpeg Path", COLOR_ACCENT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        contentPanel.add(ffmpegPathButton, gbc);

        ffmpegStatusLabel = new JLabel("FFmpeg Status: Checking...", SwingConstants.CENTER);
        ffmpegStatusLabel.setFont(FONT_STATUS);
        gbc.gridy = 7;
        contentPanel.add(ffmpegStatusLabel, gbc);

        // Convert Button / Bouton Convertir
        convertButton = createStyledButton("Convertir Audio / Convert Audio", COLOR_ACCENT_SUCCESS);
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 4;
        contentPanel.add(convertButton, gbc);

        // Status Label / État de la conversion
        statusLabel = new JLabel("Prêt / Ready", SwingConstants.CENTER);
        statusLabel.setFont(FONT_STATUS);
        statusLabel.setForeground(COLOR_STATUS_READY);
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.gridwidth = 4;
        contentPanel.add(statusLabel, gbc);
    }

    /**
     * Lays out the UI components using GridBagLayout within the content panel.
     * This method is called after all components are initialized.
     *
     * Dispose les composants de l'interface utilisateur en utilisant GridBagLayout dans le panneau de contenu.
     * Cette méthode est appelée après l'initialisation de tous les composants.
     */
    private void layoutComponents() {
        // This method primarily defines the layout. Since initComponents already adds components
        // with GridBagConstraints, this method can be used for any final adjustments or
        // if layout changes are required dynamically.
        // For now, it serves as a placeholder to emphasize layout management.
    }


    /**
     * Adds event listeners to interactive UI components.
     * Handles file addition, clearing, output directory Browse, advanced settings toggling,
     * FFmpeg path configuration, and audio conversion.
     *
     * Ajoute des écouteurs d'événements aux composants interactifs de l'interface utilisateur.
     * Gère l'ajout et l'effacement des fichiers, la navigation dans le répertoire de sortie,
     * l'activation/désactivation des paramètres avancés, la configuration du chemin FFmpeg et la conversion audio.
     */
    private void addEventListeners() {
        addFilesButton.addActionListener(e -> chooseFiles());
        clearFilesButton.addActionListener(e -> clearFileList());
        browseOutputButton.addActionListener(e -> chooseOutputDirectory());
        convertButton.addActionListener(e -> startConversionProcess());
        advancedSettingsButton.addActionListener(e -> toggleAdvancedSettingsPanel());
        customSettingsCheckBox.addActionListener(e -> toggleAdvancedSettings(customSettingsCheckBox.isSelected()));
        ffmpegPathButton.addActionListener(e -> configureFfmpegPath());

        // Allow FFmpeg status label to show details on click
        ffmpegStatusLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (LOCAL_FFMPEG_SEARCH_DETAILS != null && !LOCAL_FFMPEG_SEARCH_DETAILS.isEmpty()) {
                    JOptionPane.showMessageDialog(AudioConverter.this,
                            LOCAL_FFMPEG_SEARCH_DETAILS,
                            "FFmpeg Path Details / Détails du chemin FFmpeg",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (LOCAL_FFMPEG_SEARCH_DETAILS != null && !LOCAL_FFMPEG_SEARCH_DETAILS.isEmpty()) {
                    ffmpegStatusLabel.setToolTipText("Click to see FFmpeg path search details / Cliquez pour voir les détails de la recherche du chemin FFmpeg");
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ffmpegStatusLabel.setToolTipText(null);
            }
        });

        // Add drag and drop functionality to the entire frame
        // Ajouter la fonctionnalité de glisser-déposer à l'ensemble du cadre
        setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                Transferable t = support.getTransferable();
                try {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    addFiles(files);
                    return true;
                } catch (UnsupportedFlavorException | IOException e) {
                    System.err.println("ERROR: Drag and drop failed: " + e.getMessage());
                    return false;
                }
            }
        });
    }

    /**
     * Creates a styled JButton with common properties.
     *
     * Crée un JButton stylisé avec des propriétés communes.
     *
     * @param text The button text. / Le texte du bouton.
     * @param bgColor The background color. / La couleur de fond.
     * @return A styled JButton instance. / Une instance de JButton stylisé.
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(FONT_BUTTON);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding
        button.putClientProperty("JButton.buttonType", "roundRect"); // For some LaF to round corners
        return button;
    }

    /**
     * Toggles the visibility of the advanced settings panel.
     *
     * Bascule la visibilité du panneau des paramètres avancés.
     */
    private void toggleAdvancedSettingsPanel() {
        boolean isVisible = advancedSettingsPanel.isVisible();
        advancedSettingsPanel.setVisible(!isVisible);
        // Re-pack and re-validate to adjust layout / Re-empaqueter et revalider pour ajuster la mise en page
        this.revalidate();
        this.repaint();
        saveAdvancedSettingsToPreferences(); // Save state when toggled / Sauvegarder l'état lors du basculement
    }

    /**
     * Enables or disables the advanced settings input fields based on the provided state.
     *
     * Active ou désactive les champs de saisie des paramètres avancés en fonction de l'état fourni.
     *
     * @param enable True to enable, false to disable. / Vrai pour activer, faux pour désactiver.
     */
    private void toggleAdvancedSettings(boolean enable) {
        bitrateField.setEnabled(enable);
        sampleRateField.setEnabled(enable);
        channelsComboBox.setEnabled(enable);
        // Update background/foreground based on enabled state for visual feedback
        // Mettre à jour le fond/premier plan en fonction de l'état activé pour un retour visuel
        bitrateField.setBackground(enable ? COLOR_FIELD_BG_LIGHT : Color.LIGHT_GRAY);
        sampleRateField.setBackground(enable ? COLOR_FIELD_BG_LIGHT : Color.LIGHT_GRAY);
        channelsComboBox.setBackground(enable ? COLOR_FIELD_BG_LIGHT : Color.LIGHT_GRAY);

        bitrateField.setForeground(enable ? COLOR_TEXT_FIELD_CONTENT : Color.DARK_GRAY);
        sampleRateField.setForeground(enable ? COLOR_TEXT_FIELD_CONTENT : Color.DARK_GRAY);
        channelsComboBox.setForeground(enable ? COLOR_TEXT_FIELD_CONTENT : Color.DARK_GRAY);

        if (!enable) {
            // Reset to default values when disabled
            // Réinitialiser aux valeurs par défaut lorsque désactivé
            bitrateField.setText("192");
            sampleRateField.setText("44100");
            channelsComboBox.setSelectedItem("Original");
        }
    }

    /**
     * Displays a file chooser dialog to select audio files for conversion.
     * Adds selected files to the list if they are valid audio files.
     *
     * Affiche une boîte de dialogue de sélection de fichiers pour choisir les fichiers audio à convertir.
     * Ajoute les fichiers sélectionnés à la liste s'ils sont des fichiers audio valides.
     */
    private void chooseFiles() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true); // Allow multiple file selection
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // Only allow files to be selected
        // Restrict file types (optional, can be done by checking extensions later)
        // Restreindre les types de fichiers (facultatif, peut être fait en vérifiant les extensions plus tard)
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Audio Files", "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "aiff"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            List<File> selectedFiles = Arrays.asList(fileChooser.getSelectedFiles());
            addFiles(selectedFiles);
        }
    }

    /**
     * Adds files to the conversion list and updates the display.
     * Filters out non-audio files based on their extensions.
     *
     * Ajoute des fichiers à la liste de conversion et met à jour l'affichage.
     * Filtre les fichiers non-audio en fonction de leurs extensions.
     *
     * @param files A list of files to add. / Une liste de fichiers à ajouter.
     */
    private void addFiles(List<File> files) {
        for (File file : files) {
            String fileName = file.getName();
            String fileExtension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                fileExtension = fileName.substring(dotIndex + 1).toLowerCase();
            }

            // Check if the file extension is in our supported formats list
            // Vérifier si l'extension du fichier est dans notre liste de formats pris en charge
            if (Arrays.asList(SUPPORTED_FORMATS).contains(fileExtension)) {
                if (!filesToConvert.contains(file)) { // Avoid duplicates / Éviter les doublons
                    filesToConvert.add(file);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Skipping unsupported file: " + fileName + "\nFormats supported: " + String.join(", ", SUPPORTED_FORMATS),
                        "Unsupported File / Fichier non pris en charge",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        updateFileListDisplay();
    }

    /**
     * Clears the list of files selected for conversion and updates the display.
     *
     * Efface la liste des fichiers sélectionnés pour la conversion et met à jour l'affichage.
     */
    private void clearFileList() {
        filesToConvert.clear();
        updateFileListDisplay();
    }

    /**
     * Updates the JTextArea to show the current list of files to be converted.
     *
     * Met à jour la JTextArea pour afficher la liste actuelle des fichiers à convertir.
     */
    private void updateFileListDisplay() {
        if (filesToConvert.isEmpty()) {
            fileListArea.setText("Aucun fichier sélectionné. Glissez-déposez ou cliquez sur 'Ajouter Fichiers'.\n" +
                                 "No files selected. Drag & drop or click 'Add Files'.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < filesToConvert.size(); i++) {
                sb.append(i + 1).append(". ").append(filesToConvert.get(i).getName()).append("\n");
            }
            fileListArea.setText(sb.toString());
        }
    }

    /**
     * Displays a directory chooser dialog to select the output directory.
     *
     * Affiche une boîte de dialogue de sélection de répertoire pour choisir le répertoire de sortie.
     */
    private void chooseOutputDirectory() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Only allow directories
        dirChooser.setAcceptAllFileFilterUsed(false); // Disable "All Files" filter

        int result = dirChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDirectory = dirChooser.getSelectedFile();
            outputDirectoryField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    /**
     * Initiates the audio conversion process.
     * Validates inputs and executes FFmpeg commands for each selected file in a SwingWorker.
     *
     * Lance le processus de conversion audio.
     * Valide les entrées et exécute les commandes FFmpeg pour chaque fichier sélectionné dans un SwingWorker.
     */
    private void startConversionProcess() {
        if (filesToConvert.isEmpty()) {
            setStatus("ERREUR: Aucun fichier à convertir / ERROR: No files to convert.", COLOR_STATUS_ERROR);
            return;
        }

        String outputDir = outputDirectoryField.getText();
        if (outputDir.isEmpty()) {
            setStatus("ERREUR: Veuillez sélectionner un répertoire de sortie / ERROR: Please select an output directory.", COLOR_STATUS_ERROR);
            return;
        }

        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            setStatus("ERREUR: Impossible de créer le répertoire de sortie / ERROR: Could not create output directory.", COLOR_STATUS_ERROR);
            return;
        }
        if (!outputDirectory.canWrite()) {
            setStatus("ERREUR: Le répertoire de sortie n'est pas inscriptible / ERROR: Output directory is not writable.", COLOR_STATUS_ERROR);
            return;
        }

        // Re-check FFmpeg before starting conversion, in case it was moved/deleted
        // Revérifier FFmpeg avant de lancer la conversion, au cas où il aurait été déplacé/supprimé
        if (!isFfmpegAvailable()) {
            setStatus("ERREUR: FFmpeg non trouvé ou non exécutable. Veuillez configurer le chemin. / ERROR: FFmpeg not found or not executable. Please configure path.", COLOR_STATUS_ERROR);
            return;
        }

        String outputFormat = (String) outputFormatComboBox.getSelectedItem();
        boolean useCustomSettings = customSettingsCheckBox.isSelected();

        // Validate custom settings if enabled
        // Valider les paramètres personnalisés si activés
        if (useCustomSettings) {
            if (!bitrateField.getText().matches("\\d+") || Integer.parseInt(bitrateField.getText()) <= 0) {
                setStatus("ERREUR: Débit binaire invalide. Doit être un nombre positif. / ERROR: Invalid bitrate. Must be a positive number.", COLOR_STATUS_ERROR);
                return;
            }
            if (!sampleRateField.getText().matches("\\d+") || Integer.parseInt(sampleRateField.getText()) <= 0) {
                setStatus("ERREUR: Fréquence d'échantillonnage invalide. Doit être un nombre positif. / ERROR: Invalid sample rate. Must be a positive number.", COLOR_STATUS_ERROR);
                return;
            }
        }

        setStatus("Conversion en cours... / Conversion in progress...", COLOR_STATUS_PROCESSING);
        setUIEnabled(false); // Disable UI during conversion / Désactiver l'interface pendant la conversion

        new SwingWorker<List<String>, String>() {
            private int successfulConversions = 0;
            private int failedConversions = 0;
            private final List<String> conversionErrors = new ArrayList<>();

            @Override
            protected List<String> doInBackground() {
                for (int i = 0; i < filesToConvert.size(); i++) {
                    File inputFile = filesToConvert.get(i);
                    // Construct output filename based on original name and new format
                    // Construire le nom du fichier de sortie basé sur le nom original et le nouveau format
                    String outputFileName = getOutputFileName(inputFile.getName(), outputFormat);
                    File outputFile = new File(outputDirectory, outputFileName);

                    publish("Converting: " + inputFile.getName() + " (" + (i + 1) + "/" + filesToConvert.size() + ")");

                    try {
                        List<String> command = buildFfmpegCommand(inputFile, outputFile, outputFormat, useCustomSettings);
                        System.out.println("INFO: Executing command: " + String.join(" ", command)); // Log the full command for debugging
                        int exitCode = executeFfmpegCommand(command);

                        if (exitCode == 0) {
                            successfulConversions++;
                            System.out.println("INFO: Successfully converted: " + inputFile.getName());
                        } else {
                            failedConversions++;
                            String errorMsg = "Failed to convert " + inputFile.getName() + ". FFmpeg exited with code " + exitCode + ".";
                            System.err.println("ERROR: " + errorMsg);
                            conversionErrors.add(errorMsg);
                        }
                    } catch (IOException | InterruptedException e) {
                        failedConversions++;
                        String errorMsg = "Error converting " + inputFile.getName() + ": " + e.getMessage();
                        System.err.println("ERROR: " + errorMsg);
                        conversionErrors.add(errorMsg);
                        // If FFmpeg executable is not found or cannot be executed, stop further conversions
                        // Si l'exécutable FFmpeg est introuvable ou ne peut pas être exécuté, arrêter les conversions
                        if (e instanceof IOException && (e.getMessage().contains("No such file or directory") || e.getMessage().contains("error=2, No such file or directory"))) {
                             SwingUtilities.invokeLater(() -> setStatus("FATAL ERROR: FFmpeg executable not found. Please configure path. / ERREUR FATALE: Exécutable FFmpeg introuvable. Veuillez configurer le chemin.", COLOR_STATUS_ERROR));
                             return conversionErrors; // Stop processing further files
                        }
                    }
                }
                return conversionErrors;
            }

            @Override
            protected void process(List<String> chunks) {
                for (String status : chunks) {
                    setStatus(status, COLOR_STATUS_PROCESSING);
                }
            }

            @Override
            protected void done() {
                setUIEnabled(true);
                try {
                    List<String> errors = get();
                    if (errors.isEmpty()) {
                        setStatus("Conversion terminée avec succès ! " + successfulConversions + " fichier(s) converti(s). / Conversion completed successfully! " + successfulConversions + " file(s) converted.", COLOR_ACCENT_SUCCESS);
                    } else {
                        StringBuilder fullErrorMsg = new StringBuilder();
                        fullErrorMsg.append("Conversion terminée avec des erreurs. Succès: ").append(successfulConversions)
                                .append(", Échecs: ").append(failedConversions).append(".\n")
                                .append("Conversion finished with errors. Success: ").append(successfulConversions)
                                .append(", Failures: ").append(failedConversions).append(".\n\n")
                                .append("Détails des erreurs / Error details:\n");
                        for (String error : errors) {
                            fullErrorMsg.append("- ").append(error).append("\n");
                        }
                        setStatus("Conversion terminée avec des erreurs. Voir les logs pour plus de détails. / Conversion finished with errors. See logs for details.", COLOR_STATUS_ERROR);
                        JOptionPane.showMessageDialog(AudioConverter.this,
                                fullErrorMsg.toString(),
                                "Conversion Errored / Conversion avec Erreurs",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    setStatus("Conversion interrompue ou échouée: " + e.getMessage(), COLOR_STATUS_ERROR);
                    System.err.println("ERROR: Conversion SwingWorker failed: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Constructs the FFmpeg command based on user selections.
     * Uses a List of Strings for ProcessBuilder for security against command injection.
     *
     * Construit la commande FFmpeg basée sur les sélections de l'utilisateur.
     * Utilise une liste de chaînes de caractères pour ProcessBuilder afin de sécuriser contre l'injection de commandes.
     *
     * @param inputFile The input audio file. / Le fichier audio d'entrée.
     * @param outputFile The desired output file. / Le fichier de sortie désiré.
     * @param outputFormat The selected output format (e.g., "mp3", "wav"). / Le format de sortie sélectionné.
     * @param useCustomSettings Whether to apply custom bitrate, sample rate, and channels. / Si les paramètres personnalisés doivent être appliqués.
     * @return A List of strings representing the FFmpeg command and its arguments. / Une liste de chaînes de caractères représentant la commande FFmpeg et ses arguments.
     */
    private List<String> buildFfmpegCommand(File inputFile, File outputFile, String outputFormat, boolean useCustomSettings) {
        List<String> command = new ArrayList<>();
        command.add(FFMPEG_EXECUTABLE);
        command.add("-i");
        command.add(inputFile.getAbsolutePath());

        // Apply custom settings if enabled / Appliquer les paramètres personnalisés si activés
        if (useCustomSettings) {
            String bitrate = bitrateField.getText();
            String sampleRate = sampleRateField.getText();
            String channels = (String) channelsComboBox.getSelectedItem();

            if (bitrate != null && !bitrate.trim().isEmpty()) {
                command.add("-b:a");
                command.add(bitrate + "k"); // Bitrate in kilobits per second / Débit binaire en kilobits par seconde
            }
            if (sampleRate != null && !sampleRate.trim().isEmpty()) {
                command.add("-ar");
                command.add(sampleRate); // Audio sample rate in Hz / Fréquence d'échantillonnage audio en Hz
            }

            if (channels != null && !channels.equals("Original")) {
                switch (channels) {
                    case "Mono":
                        command.add("-ac");
                        command.add("1");
                        break;
                    case "Stéréo":
                        command.add("-ac");
                        command.add("2");
                        break;
                    // Add more channel mappings if needed
                    // Ajouter plus de mappings de canaux si nécessaire
                }
            }
        }

        command.add("-vn"); // Disable video output, ensuring audio-only conversion / Désactiver la sortie vidéo, assurant une conversion audio seulement
        command.add("-y");  // Overwrite output file without asking / Écraser le fichier de sortie sans demander
        command.add(outputFile.getAbsolutePath());

        return command;
    }

    /**
     * Executes an FFmpeg command and captures its output and errors.
     * Returns the exit code of the FFmpeg process.
     * FFmpeg output is redirected to System.out and System.err for debugging.
     *
     * Exécute une commande FFmpeg et capture sa sortie et ses erreurs.
     * Renvoie le code de sortie du processus FFmpeg.
     * La sortie de FFmpeg est redirigée vers System.out et System.err pour le débogage.
     *
     * @param command A list of strings representing the command and its arguments. / Une liste de chaînes de caractères représentant la commande et ses arguments.
     * @return The exit code of the FFmpeg process. / Le code de sortie du processus FFmpeg.
     * @throws IOException If an I/O error occurs when starting the process. / Si une erreur d'E/S se produit lors du démarrage du processus.
     * @throws InterruptedException If the current thread is interrupted while waiting for the process to complete. / Si le thread actuel est interrompu en attendant la fin du processus.
     */
    private int executeFfmpegCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        // Redirect FFmpeg's stderr to capture error messages
        // Rediriger la sortie d'erreur de FFmpeg pour capturer les messages d'erreur
        pb.redirectErrorStream(true); // Merges stderr into stdout, helpful for simpler single stream capture

        Process process = pb.start();

        // Capture output (and errors, due to redirectErrorStream(true))
        // Capturer la sortie (et les erreurs, grâce à redirectErrorStream(true))
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("FFmpeg: " + line); // Log FFmpeg's output / Journaliser la sortie de FFmpeg
            }
        }

        return process.waitFor(); // Wait for the process to complete and return its exit code / Attendre que le processus se termine et renvoyer son code de sortie
    }

    /**
     * Derives the output filename from the input filename and desired output format.
     *
     * Déduit le nom du fichier de sortie à partir du nom du fichier d'entrée et du format de sortie désiré.
     *
     * @param inputFileName The name of the input file. / Le nom du fichier d'entrée.
     * @param outputFormat The desired output format (e.g., "mp3", "wav"). / Le format de sortie désiré.
     * @return The constructed output filename. / Le nom du fichier de sortie construit.
     */
    private String getOutputFileName(String inputFileName, String outputFormat) {
        int dotIndex = inputFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            return inputFileName.substring(0, dotIndex) + "." + outputFormat;
        }
        return inputFileName + "." + outputFormat; // Fallback if no extension / Cas de secours si pas d'extension
    }

    /**
     * Updates the main status label with a message and a specified color.
     * This method is safe to call from any thread as it uses SwingUtilities.invokeLater.
     *
     * Met à jour le label de statut principal avec un message et une couleur spécifiée.
     * Cette méthode peut être appelée depuis n'importe quel thread car elle utilise SwingUtilities.invokeLater.
     *
     * @param message The status message to display. / Le message de statut à afficher.
     * @param color The color of the status message. / La couleur du message de statut.
     */
    private void setStatus(String message, Color color) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            statusLabel.setForeground(color);
        });
    }

    /**
     * Enables or disables the main UI elements to prevent user interaction during conversion.
     *
     * Active ou désactive les éléments principaux de l'interface utilisateur pour empêcher
     * l'interaction de l'utilisateur pendant la conversion.
     *
     * @param enabled True to enable, false to disable. / Vrai pour activer, faux pour désactiver.
     */
    private void setUIEnabled(boolean enabled) {
        addFilesButton.setEnabled(enabled);
        clearFilesButton.setEnabled(enabled);
        outputFormatComboBox.setEnabled(enabled);
        browseOutputButton.setEnabled(enabled);
        convertButton.setEnabled(enabled);
        advancedSettingsButton.setEnabled(enabled);
        customSettingsCheckBox.setEnabled(enabled);
        // Only enable bitrate/sampleRate/channels if custom settings checkbox is also enabled
        // Activer les champs de débit binaire/fréquence d'échantillonnage/canaux seulement si la case à cocher des paramètres personnalisés est également activée
        toggleAdvancedSettings(enabled && customSettingsCheckBox.isSelected());
        ffmpegPathButton.setEnabled(enabled);
    }

    /**
     * Custom renderer for JComboBox to style items.
     * Rendu personnalisé pour JComboBox afin de styliser les éléments.
     */
    static class CustomComboBoxRenderer extends BasicComboBoxRenderer {
        private final Color background;
        private final Color foreground;

        public CustomComboBoxRenderer(Color background, Color foreground) {
            this.background = background;
            this.foreground = foreground;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (isSelected) {
                setBackground(COLOR_ACCENT_PRIMARY); // Highlight selected item
                setForeground(Color.WHITE);
            } else {
                setBackground(background);
                setForeground(foreground);
            }
            // Apply border for consistency if needed
            // Appliquer une bordure pour la cohérence si nécessaire
            setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            return this;
        }
    }

    /**
     * FileDropTarget handles drag-and-drop of files onto a JTextArea.
     * Cible de dépôt de fichiers gérant le glisser-déposer de fichiers sur une JTextArea.
     */
    static class FileDropTarget extends java.awt.dnd.DropTarget {
        private final java.util.function.Consumer<List<File>> fileConsumer;

        public FileDropTarget(java.util.function.Consumer<List<File>> fileConsumer) {
            this.fileConsumer = fileConsumer;
        }

        @Override
        public void drop(java.awt.dnd.DropTargetDropEvent dtde) {
            dtde.acceptDrop(java.awt.dnd.DnDConstants.ACTION_COPY);
            Transferable transferable = dtde.getTransferable();
            try {
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    fileConsumer.accept(files); // Pass files to the provided consumer
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                System.err.println("ERROR: Drop operation failed: " + ex.getMessage());
            } finally {
                dtde.dropComplete(true);
            }
        }
    }

    /**
     * Loads advanced audio conversion settings from user preferences.
     * These settings include whether custom settings are enabled, custom bitrate, sample rate, and channels.
     *
     * Charge les paramètres avancés de conversion audio depuis les préférences utilisateur.
     * Ces paramètres incluent l'activation des paramètres personnalisés, le débit binaire, la fréquence d'échantillonnage et les canaux.
     */
    private void loadAdvancedSettingsFromPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(AudioConverter.class);
        customSettingsCheckBox.setSelected(prefs.getBoolean(PREF_KEY_CUSTOM_SETTINGS_ENABLED, false));
        bitrateField.setText(prefs.get(PREF_KEY_CUSTOM_BITRATE, "192")); // Default to 192kbps
        sampleRateField.setText(prefs.get(PREF_KEY_CUSTOM_SAMPLERATE, "44100")); // Default to 44.1kHz
        channelsComboBox.setSelectedItem(prefs.get(PREF_KEY_CUSTOM_CHANNELS, "Original"));
        toggleAdvancedSettings(customSettingsCheckBox.isSelected()); // Apply initial state
        System.out.println("INFO: Advanced settings loaded from preferences."); // INFO: Paramètres avancés chargés depuis les préférences.
    }

    /**
     * Saves the current advanced audio conversion settings to user preferences.
     * This ensures that user-defined settings persist across application sessions.
     *
     * Sauvegarde les paramètres avancés de conversion audio actuels dans les préférences utilisateur.
     * Cela garantit que les paramètres définis par l'utilisateur persistent d'une session à l'autre.
     */
    private void saveAdvancedSettingsToPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(AudioConverter.class);
        prefs.putBoolean(PREF_KEY_CUSTOM_SETTINGS_ENABLED, customSettingsCheckBox.isSelected());
        prefs.put(PREF_KEY_CUSTOM_BITRATE, bitrateField.getText());
        prefs.put(PREF_KEY_CUSTOM_SAMPLERATE, sampleRateField.getText());
        prefs.put(PREF_KEY_CUSTOM_CHANNELS, (String) channelsComboBox.getSelectedItem());
        System.out.println("INFO: Advanced settings saved to preferences."); // INFO: Paramètres avancés sauvegardés dans les préférences.
    }

    /**
     * Loads the custom FFmpeg executable path from user preferences.
     * This path is used as the highest priority when determining which FFmpeg executable to use.
     *
     * Charge le chemin de l'exécutable FFmpeg personnalisé depuis les préférences utilisateur.
     * Ce chemin est utilisé en priorité la plus élevée lors de la détermination de l'exécutable FFmpeg à utiliser.
     */
    private static void loadUserFfmpegPathFromPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(AudioConverter.class);
        USER_CUSTOM_FFMPEG_PATH = prefs.get(PREF_KEY_CUSTOM_FFMPEG_PATH, "");
        System.out.println("INFO: Custom FFmpeg path loaded from preferences: '" + USER_CUSTOM_FFMPEG_PATH + "'"); // INFO: Chemin FFmpeg personnalisé chargé depuis les préférences :
    }

    /**
     * Saves the provided FFmpeg executable path to user preferences.
     * If the path is null, it saves an empty string. This updates
     * the static variable `USER_CUSTOM_FFMPEG_PATH` as well.
     *
     * Sauvegarde le chemin de l'exécutable FFmpeg fourni dans les préférences utilisateur.
     * Si le chemin est nul, une chaîne vide est sauvegardée. Cela met également à jour
     * la variable statique `USER_CUSTOM_FFMPEG_PATH`.
     *
     * @param path The FFmpeg executable path to save. / Le chemin de l'exécutable FFmpeg à sauvegarder.
     */
    private static void saveUserFfmpegPathToPreferences(String path) {
        Preferences prefs = Preferences.userNodeForPackage(AudioConverter.class);
        if (path == null) path = "";
        prefs.put(PREF_KEY_CUSTOM_FFMPEG_PATH, path);
        USER_CUSTOM_FFMPEG_PATH = path; // Update the static variable as well / Mettre à jour la variable statique également
        System.out.println("INFO: Custom FFmpeg path saved to preferences: '" + path + "'"); // INFO: Chemin FFmpeg personnalisé sauvegardé dans les préférences :
    }

    /**
     * Initializes the FFmpeg executable path based on a priority order:
     * 1. User-defined custom path (from preferences).
     * 2. FFmpeg executable in the same directory as the application JAR/class files.
     * 3. FFmpeg from the system's PATH environment variable.
     * Sets FFMPEG_EXECUTABLE and LOCAL_FFMPEG_SEARCH_DETAILS for diagnostics.
     *
     * Initialise le chemin de l'exécutable FFmpeg selon un ordre de priorité :
     * 1. Chemin personnalisé défini par l'utilisateur (depuis les préférences).
     * 2. Exécutable FFmpeg dans le même répertoire que le JAR de l'application ou les fichiers de classe.
     * 3. FFmpeg depuis la variable d'environnement PATH du système.
     * Définit FFMPEG_EXECUTABLE et LOCAL_FFMPEG_SEARCH_DETAILS pour les diagnostics.
     */
    private static void initializeFfmpegPath() {
        StringBuilder searchLog = new StringBuilder("FFmpeg path determination:\n"); // Détermination du chemin FFmpeg :
        String ffmpegExecutableName = "ffmpeg";
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            ffmpegExecutableName = "ffmpeg.exe";
        }

        // 1. Check user-defined custom path / 1. Vérifier le chemin personnalisé par l'utilisateur
        if (USER_CUSTOM_FFMPEG_PATH != null && !USER_CUSTOM_FFMPEG_PATH.trim().isEmpty()) {
            searchLog.append(" 1. Attempting custom path: '").append(USER_CUSTOM_FFMPEG_PATH).append("'\n"); // Essai du chemin personnalisé :
            File customFfmpeg = new File(USER_CUSTOM_FFMPEG_PATH);
            if (customFfmpeg.exists() && customFfmpeg.isFile() && customFfmpeg.canExecute()) {
                FFMPEG_EXECUTABLE = customFfmpeg.getAbsolutePath();
                searchLog.append(" -> SUCCESS: FFmpeg found and executable.\n"); // SUCCÈS : FFmpeg trouvé et exécutable.
                LOCAL_FFMPEG_SEARCH_DETAILS = searchLog.toString();
                System.out.println(LOCAL_FFMPEG_SEARCH_DETAILS);
                return;
            } else {
                searchLog.append(" -> FAILED: Custom path is not a valid or executable file.\n"); // ÉCHEC : Le chemin personnalisé n'est pas un fichier valide ou exécutable.
            }
        }

        // 2. Check local application directory / 2. Vérifier le répertoire local de l'application
        try {
            ProtectionDomain protectionDomain = AudioConverter.class.getProtectionDomain();
            if (protectionDomain == null) {
                searchLog.append(" -> CRITICAL: ProtectionDomain is null. Cannot determine application directory.\n"); // CRITIQUE : ProtectionDomain est nul. Impossible de déterminer le répertoire de l'application.
                throw new SecurityException("ProtectionDomain is null");
            }
            CodeSource codeSource = protectionDomain.getCodeSource();
            if (codeSource == null) {
                searchLog.append(" -> CRITICAL: CodeSource is null. Cannot determine application directory.\n"); // CRITIQUE : CodeSource est nul. Impossible de déterminer le répertoire de l'application.
                throw new SecurityException("CodeSource is null");
            }
            URL codeSourceUrl = codeSource.getLocation();
            if (codeSourceUrl == null) {
                searchLog.append(" -> CRITICAL: CodeSource URL is null. Cannot determine application directory.\n"); // CRITIQUE : URL du CodeSource est nul. Impossible de déterminer le répertoire de l'application.
                throw new SecurityException("CodeSource URL is null");
            }
            URI codeSourceUri = codeSourceUrl.toURI();
            File codeSourceFile = new File(codeSourceUri);
            File appBaseDirectory;
            String appBaseInfo;

            if (codeSourceFile.isFile() && codeSourceFile.getName().toLowerCase().endsWith(".jar")) {
                appBaseDirectory = codeSourceFile.getParentFile();
                appBaseInfo = (appBaseDirectory != null) ? appBaseDirectory.getAbsolutePath() : "JAR parent directory (not found)"; // Répertoire parent du JAR (introuvable)
            } else if (codeSourceFile.isDirectory()) {
                appBaseDirectory = codeSourceFile;
                appBaseInfo = appBaseDirectory.getAbsolutePath();
            } else {
                appBaseInfo = codeSourceFile.getAbsolutePath() + " (is neither a JAR nor a directory)"; // n'est ni un JAR ni un répertoire
                appBaseDirectory = new File("."); // Fallback
            }

            searchLog.append(" 2. Application base directory determined: '").append(appBaseInfo).append("'\n"); // Répertoire de base de l'application déterminé :
            if (appBaseDirectory != null) {
                File localAppFfmpeg = new File(appBaseDirectory, ffmpegExecutableName);
                searchLog.append("    Checking: '").append(localAppFfmpeg.getAbsolutePath()).append("'\n"); // Vérification de :
                if (localAppFfmpeg.exists() && localAppFfmpeg.isFile() && localAppFfmpeg.canExecute()) {
                    FFMPEG_EXECUTABLE = localAppFfmpeg.getAbsolutePath();
                    searchLog.append("    -> SUCCESS: FFmpeg found and executable.\n"); // SUCCÈS : FFmpeg trouvé et exécutable.
                    LOCAL_FFMPEG_SEARCH_DETAILS = searchLog.toString();
                    System.out.println(LOCAL_FFMPEG_SEARCH_DETAILS);
                    return;
                } else {
                    searchLog.append("    -> FAILED: Not found or not executable in app directory.\n"); // ÉCHEC : Introuvable ou non exécutable dans le répertoire de l'application.
                }
            }
        } catch (URISyntaxException e) {
            searchLog.append(" 2. FAILED: Error determining application path: ").append(e.getMessage()).append("\n"); // ÉCHEC : Erreur lors de la détermination du chemin de l'application :
            System.err.println("ERROR: URISyntaxException when determining app path: " + e.getMessage());
        } catch (SecurityException e) {
            searchLog.append(" 2. FAILED: Security restriction preventing application path determination: ").append(e.getMessage()).append("\n"); // ÉCHEC : Restriction de sécurité empêchant la détermination du chemin de l'application :
            System.err.println("ERROR: SecurityException when determining app path: " + e.getMessage());
        }

        // 3. Check system PATH environment variable / 3. Vérifier la variable d'environnement PATH du système
        searchLog.append(" 3. Attempting system PATH search for '").append(ffmpegExecutableName).append("'\n"); // Tentative de recherche dans le PATH système pour
        String pathEnv = System.getenv("PATH");
        if (pathEnv != null) {
            String[] paths = pathEnv.split(File.pathSeparator);
            for (String path : paths) {
                File ffmpegFile = new File(path, ffmpegExecutableName);
                searchLog.append("    Checking PATH entry: '").append(ffmpegFile.getAbsolutePath()).append("'\n"); // Vérification de l'entrée PATH :
                if (ffmpegFile.exists() && ffmpegFile.isFile() && ffmpegFile.canExecute()) {
                    FFMPEG_EXECUTABLE = ffmpegFile.getAbsolutePath();
                    searchLog.append("    -> SUCCESS: FFmpeg found and executable in PATH.\n"); // SUCCÈS : FFmpeg trouvé et exécutable dans le PATH.
                    LOCAL_FFMPEG_SEARCH_DETAILS = searchLog.toString();
                    System.out.println(LOCAL_FFMPEG_SEARCH_DETAILS);
                    return;
                }
            }
        }
        searchLog.append(" -> FAILED: Not found or not executable in system PATH.\n"); // ÉCHEC : Introuvable ou non exécutable dans le PATH système.

        FFMPEG_EXECUTABLE = null; // FFmpeg not found / FFmpeg introuvable
        LOCAL_FFMPEG_SEARCH_DETAILS = searchLog.toString();
        System.err.println("WARNING: FFmpeg executable could not be found via any method."); // AVERTISSEMENT : L'exécutable FFmpeg n'a pu être trouvé par aucune méthode.
        System.out.println(LOCAL_FFMPEG_SEARCH_DETAILS);
    }

    /**
     * Checks if the FFmpeg executable is available and updates the FFmpeg status label.
     * This method is called on application startup and after path configuration.
     *
     * Vérifie si l'exécutable FFmpeg est disponible et met à jour le label de statut FFmpeg.
     * Cette méthode est appelée au démarrage de l'application et après la configuration du chemin.
     *
     * @return True if FFmpeg is found and executable, false otherwise. / Vrai si FFmpeg est trouvé et exécutable, faux sinon.
     */
    private boolean isFfmpegAvailable() {
        loadUserFfmpegPathFromPreferences(); // Always load the latest user path preference first
        initializeFfmpegPath(); // Then try to resolve the path based on priority

        if (FFMPEG_EXECUTABLE != null) {
            ffmpegStatusLabel.setText("FFmpeg Statut: Trouvé / Status: Found");
            ffmpegStatusLabel.setForeground(COLOR_ACCENT_SUCCESS);
            return true;
        } else {
            ffmpegStatusLabel.setText("FFmpeg Statut: Non Trouvé / Status: Not Found");
            ffmpegStatusLabel.setForeground(COLOR_ACCENT_DANGER);
            return false;
        }
    }

    /**
     * Performs a check for FFmpeg availability on application startup.
     * This method is called from the constructor to initialize the FFmpeg status label.
     *
     * Effectue une vérification de la disponibilité de FFmpeg au démarrage de l'application.
     * Cette méthode est appelée depuis le constructeur pour initialiser le label de statut FFmpeg.
     */
    private void checkFfmpegOnStartup() {
        isFfmpegAvailable();
    }


    /**
     * Prompts the user to configure the FFmpeg executable path.
     * This path is then saved to user preferences for persistence.
     *
     * Demande à l'utilisateur de configurer le chemin de l'exécutable FFmpeg.
     * Ce chemin est ensuite sauvegardé dans les préférences utilisateur pour persistance.
     */
    private void configureFfmpegPath() {
        JFileChooser pathChooser = new JFileChooser();
        pathChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        pathChooser.setDialogTitle("Sélectionner l'exécutable FFmpeg / Select FFmpeg Executable");

        // Set current custom path if it exists, for user convenience
        // Définir le chemin personnalisé actuel s'il existe, pour la commodité de l'utilisateur
        if (USER_CUSTOM_FFMPEG_PATH != null && !USER_CUSTOM_FFMPEG_PATH.isEmpty()) {
            File currentPath = new File(USER_CUSTOM_FFMPEG_PATH);
            if (currentPath.exists()) {
                pathChooser.setSelectedFile(currentPath);
            }
        }

        int result = pathChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = pathChooser.getSelectedFile();
            if (selectedFile.exists() && selectedFile.isFile() && selectedFile.canExecute()) {
                saveUserFfmpegPathToPreferences(selectedFile.getAbsolutePath());
                isFfmpegAvailable(); // Re-check and update status label / Revérifier et mettre à jour le label de statut
            } else {
                JOptionPane.showMessageDialog(this,
                        "Le fichier sélectionné n'est pas un exécutable FFmpeg valide ou n'est pas exécutable.\n" +
                                "The selected file is not a valid FFmpeg executable or is not executable.",
                        "FFmpeg Invalide / Invalid FFmpeg",
                        JOptionPane.ERROR_MESSAGE);
                saveUserFfmpegPathToPreferences(""); // Clear invalid path / Effacer le chemin invalide
                isFfmpegAvailable(); // Update status to Not Found / Mettre à jour le statut à Non Trouvé
            }
        }
    }


    /**
     * Main method to run the application.
     * Sets up the Swing UI on the Event Dispatch Thread.
     *
     * Méthode principale pour exécuter l'application.
     * Configure l'interface utilisateur Swing sur le thread de répartition des événements.
     *
     * @param args Command line arguments (not used). / Arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        // Load preferences at startup to initialize USER_CUSTOM_FFMPEG_PATH
        // Charger les préférences au démarrage pour initialiser USER_CUSTOM_FFMPEG_PATH
        loadUserFfmpegPathFromPreferences();

        SwingUtilities.invokeLater(() -> {
            AudioConverter converter = new AudioConverter();
            converter.setVisible(true);
            converter.updateFileListDisplay(); // Set initial text for file list area / Définir le texte initial pour la zone de liste de fichiers
        });
    }
}