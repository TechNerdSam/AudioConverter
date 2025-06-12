import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences; // --- NOUVEAU: Pour sauvegarder les préférences ---
import javax.imageio.ImageIO;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * <h1>Elegant Audio Converter</h1>
 * ... (description existante)
 * Il permet maintenant de spécifier un chemin personnalisé pour FFmpeg.
 *
 * @author Gemini (Refactored by AI)
 * @version 4.5.0 (Chemin FFmpeg personnalisable et priorité de recherche étendue) // --- MODIFIED VERSION ---
 */
public class AudioConverter extends JFrame {

    // --- Constantes UI (inchangées) ---
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

    private static final int CORNER_RADIUS_PANEL = 20;
    private static final int CORNER_RADIUS_FIELD = 12;
    private static final int CORNER_RADIUS_BUTTON = 20;

    private static final Font FONT_PRIMARY = new Font("Inter", Font.PLAIN, 17);
    private static final Font FONT_BUTTON = new Font("Inter", Font.BOLD, 18);
    private static final Font FONT_TITLE = new Font("Inter", Font.BOLD, 48);
    private static final Font FONT_STATUS = new Font("Inter", Font.BOLD | Font.ITALIC, 20);
    private static final Font FONT_FIELD = new Font("Inter", Font.PLAIN, 16);

    private static final String[] SUPPORTED_FORMATS = {"mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "aiff"};
    private static final String DEFAULT_BACKGROUND_IMAGE_URL = "https://image.noelshack.com/fichiers/2025/22/7/1748758758-codioful-formerly-gradienta-leg68prxa6y-unsplash-1.jpg";

    // --- NOUVEAU: Constantes pour la configuration du chemin FFmpeg ---
    private static final String PREF_KEY_CUSTOM_FFMPEG_PATH = "customFfmpegPath";
    private static final String HARDCODED_FFMPEG_DIRECTORY_PATH = "C:\\Users\\Samyn\\OneDrive\\Applications\\Mes Projets informatiques\\Mes_Projets_Java\\Mes_projets_Informatiques_Java\\Mes_Projets_Logiciels\\AudioConverter";

    private static String FFMPEG_EXECUTABLE; // Chemin effectif utilisé
    private static String USER_CUSTOM_FFMPEG_PATH = ""; // Chemin défini par l'utilisateur
    private static String LOCAL_FFMPEG_SEARCH_DETAILS;

    // --- NOUVEAU: UI Components pour le chemin FFmpeg ---
    private RoundedTextField ffmpegPathField;
    private RoundedButton browseFfmpegButton;
    private RoundedButton applyFfmpegPathButton;


    static {
        loadUserFfmpegPathFromPreferences(); // Charger avant initialisation
        initializeFfmpegPath();
    }

    // --- UI Components ---
    private RoundedTextField inputFilePathField;
    private RoundedComboBox<String> outputFormatComboBox;
    private RoundedTextField outputFilePathField;
    private RoundedButton selectFileButton;
    private RoundedButton convertButton;
    private RoundedButton clearButton;
    private ShadowLabel statusLabel;
    private JProgressBar progressBar;

    private Image backgroundImage;
    private List<File> filesToConvert;

    public AudioConverter() {
        this.filesToConvert = new ArrayList<>();
        configureFrame();
        loadBackgroundImage();
        initComponents(); // Initialise tous les composants, y compris les nouveaux
        layoutComponents();
        addEventListeners();

        pack();
        setLocationRelativeTo(null);
        checkFfmpegOnStartup(); // Vérifie FFmpeg après que tout soit configuré
    }

    // --- NOUVEAU: Charger le chemin FFmpeg personnalisé depuis les préférences ---
    private static void loadUserFfmpegPathFromPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(AudioConverter.class);
        USER_CUSTOM_FFMPEG_PATH = prefs.get(PREF_KEY_CUSTOM_FFMPEG_PATH, "");
        System.out.println("INFO: Chemin FFmpeg personnalisé chargé depuis les préférences : '" + USER_CUSTOM_FFMPEG_PATH + "'");
    }

    // --- NOUVEAU: Sauvegarder le chemin FFmpeg personnalisé dans les préférences ---
    private static void saveUserFfmpegPathToPreferences(String path) {
        Preferences prefs = Preferences.userNodeForPackage(AudioConverter.class);
        if (path == null) path = "";
        prefs.put(PREF_KEY_CUSTOM_FFMPEG_PATH, path);
        USER_CUSTOM_FFMPEG_PATH = path; // Mettre à jour la variable statique aussi
        System.out.println("INFO: Chemin FFmpeg personnalisé sauvegardé dans les préférences : '" + path + "'");
    }


    // --- MODIFIÉ: Logique d'initialisation du chemin FFmpeg avec priorités ---
    private static void initializeFfmpegPath() {
        StringBuilder searchLog = new StringBuilder("Détermination du chemin FFmpeg :\n");
        String ffmpegExecutableName = "ffmpeg";
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            ffmpegExecutableName = "ffmpeg.exe";
        }

        // 1. Vérifier le chemin personnalisé par l'utilisateur
        if (USER_CUSTOM_FFMPEG_PATH != null && !USER_CUSTOM_FFMPEG_PATH.trim().isEmpty()) {
            searchLog.append("  1. Essai du chemin personnalisé : '").append(USER_CUSTOM_FFMPEG_PATH).append("'\n");
            File customFfmpeg = new File(USER_CUSTOM_FFMPEG_PATH);
            if (customFfmpeg.exists() && customFfmpeg.isFile() && customFfmpeg.canExecute()) {
                FFMPEG_EXECUTABLE = customFfmpeg.getAbsolutePath();
                searchLog.append("     -> SUCCÈS : FFmpeg trouvé et exécutable.\n");
                LOCAL_FFMPEG_SEARCH_DETAILS = searchLog.toString();
                System.out.println(LOCAL_FFMPEG_SEARCH_DETAILS);
                return;
            } else {
                searchLog.append("     -> ÉCHEC : Fichier non trouvé, n'est pas un fichier, ou non exécutable.\n");
            }
        } else {
            searchLog.append("  1. Chemin personnalisé non défini ou vide.\n");
        }

        // 2. Vérifier le chemin codé en dur
        searchLog.append("  2. Essai du chemin codé en dur : '").append(HARDCODED_FFMPEG_DIRECTORY_PATH).append(File.separator).append(ffmpegExecutableName).append("'\n");
        File hardcodedFfmpeg = new File(HARDCODED_FFMPEG_DIRECTORY_PATH, ffmpegExecutableName);
        if (hardcodedFfmpeg.exists() && hardcodedFfmpeg.isFile() && hardcodedFfmpeg.canExecute()) {
            FFMPEG_EXECUTABLE = hardcodedFfmpeg.getAbsolutePath();
            searchLog.append("     -> SUCCÈS : FFmpeg trouvé et exécutable.\n");
            LOCAL_FFMPEG_SEARCH_DETAILS = searchLog.toString();
            System.out.println(LOCAL_FFMPEG_SEARCH_DETAILS);
            return;
        } else {
            searchLog.append("     -> ÉCHEC : Fichier non trouvé, n'est pas un fichier, ou non exécutable.\n");
        }

        // 3. Vérifier le chemin local à l'application (logique existante adaptée)
        searchLog.append("  3. Essai du chemin local à l'application :\n");
        try {
            ProtectionDomain protectionDomain = AudioConverter.class.getProtectionDomain();
             if (protectionDomain == null) {
                searchLog.append("     -> CRITIQUE : ProtectionDomain est nul. Impossible de déterminer le répertoire de l'application.\n");
                throw new SecurityException("ProtectionDomain est nul");
            }
            CodeSource codeSource = protectionDomain.getCodeSource();
             if (codeSource == null) {
                searchLog.append("     -> CRITIQUE : CodeSource est nul. Impossible de déterminer le répertoire de l'application.\n");
                throw new SecurityException("CodeSource est nul");
            }
            URL codeSourceUrl = codeSource.getLocation();
             if (codeSourceUrl == null) {
                searchLog.append("     -> CRITIQUE : URL du CodeSource est nul. Impossible de déterminer le répertoire de l'application.\n");
                throw new SecurityException("URL du CodeSource est nul");
            }

            URI codeSourceUri = codeSourceUrl.toURI();
            File codeSourceFile = new File(codeSourceUri);
            File appBaseDirectory;
            String appBaseInfo;

            if (codeSourceFile.isFile() && codeSourceFile.getName().toLowerCase().endsWith(".jar")) {
                appBaseDirectory = codeSourceFile.getParentFile();
                appBaseInfo = (appBaseDirectory != null) ? appBaseDirectory.getAbsolutePath() : "Répertoire parent du JAR (introuvable)";
            } else if (codeSourceFile.isDirectory()) {
                appBaseDirectory = codeSourceFile;
                appBaseInfo = appBaseDirectory.getAbsolutePath();
            } else {
                appBaseInfo = codeSourceFile.getAbsolutePath() + " (n'est ni un JAR ni un répertoire)";
                appBaseDirectory = new File("."); // Fallback
            }
            searchLog.append("     Répertoire de base de l'application déterminé : '").append(appBaseInfo).append("'\n");
            
            if (appBaseDirectory != null) {
                File localAppFfmpeg = new File(appBaseDirectory, ffmpegExecutableName);
                searchLog.append("     Vérification de : '").append(localAppFfmpeg.getAbsolutePath()).append("'\n");
                if (localAppFfmpeg.exists() && localAppFfmpeg.isFile() && localAppFfmpeg.canExecute()) {
                    FFMPEG_EXECUTABLE = localAppFfmpeg.getAbsolutePath();
                    searchLog.append("     -> SUCCÈS : FFmpeg trouvé et exécutable.\n");
                    LOCAL_FFMPEG_SEARCH_DETAILS = searchLog.toString();
                    System.out.println(LOCAL_FFMPEG_SEARCH_DETAILS);
                    return;
                } else {
                     searchLog.append("     -> ÉCHEC : Fichier non trouvé, n'est pas un fichier, ou non exécutable.\n");
                }
            } else {
                 searchLog.append("     -> ÉCHEC : Répertoire de base de l'application est nul.\n");
            }
        } catch (URISyntaxException | SecurityException e) {
            searchLog.append("     -> ERREUR lors de la détermination du chemin local : ").append(e.getMessage()).append("\n");
        }

        // 4. Chute vers le PATH système
        FFMPEG_EXECUTABLE = ffmpegExecutableName;
        searchLog.append("  4. Chute vers FFmpeg du system PATH : '").append(FFMPEG_EXECUTABLE).append("'\n");
        LOCAL_FFMPEG_SEARCH_DETAILS = searchLog.toString();
        System.out.println(LOCAL_FFMPEG_SEARCH_DETAILS);
    }


    private void configureFrame() {
        setTitle("Elegant Audio Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 750)); // Augmenter la hauteur pour les nouveaux champs

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system Look and Feel. Using default. Error: " + e.getMessage());
        }
    }

    private void loadBackgroundImage() {
        try {
            backgroundImage = ImageIO.read(new URL(DEFAULT_BACKGROUND_IMAGE_URL));
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL for background image: " + DEFAULT_BACKGROUND_IMAGE_URL + ". Error: " + e.getMessage());
            backgroundImage = null;
        } catch (IOException e) {
            System.err.println("Could not load background image from URL: " + DEFAULT_BACKGROUND_IMAGE_URL + ". Error: " + e.getMessage());
            backgroundImage = null;
        }
    }

    private void initComponents() {
        inputFilePathField = new RoundedTextField(40, CORNER_RADIUS_FIELD, COLOR_FIELD_BG_LIGHT, COLOR_TEXT_FIELD_CONTENT, COLOR_FIELD_BORDER_SUBTLE);
        inputFilePathField.setEditable(false);
        inputFilePathField.setToolTipText("Chemin du fichier audio à convertir. Glissez-déposez un ou plusieurs fichiers ici.");

        selectFileButton = new RoundedButton("Choisir Fichier...", COLOR_ACCENT_PRIMARY, CORNER_RADIUS_BUTTON);
        selectFileButton.setToolTipText("Cliquez pour choisir le fichier audio d'entrée.");

        outputFormatComboBox = new RoundedComboBox<>(SUPPORTED_FORMATS, CORNER_RADIUS_FIELD, COLOR_FIELD_BG_LIGHT, COLOR_TEXT_FIELD_CONTENT, COLOR_FIELD_BORDER_SUBTLE);
        outputFormatComboBox.setToolTipText("Sélectionnez le format de fichier de sortie.");

        outputFilePathField = new RoundedTextField(40, CORNER_RADIUS_FIELD, COLOR_FIELD_BG_LIGHT, COLOR_TEXT_FIELD_CONTENT, COLOR_FIELD_BORDER_SUBTLE);
        outputFilePathField.setEditable(false);
        outputFilePathField.setToolTipText("Chemin où le fichier converti sera sauvegardé (ou répertoire pour lots).");

        // --- NOUVEAU: Initialisation des composants pour le chemin FFmpeg ---
        ffmpegPathField = new RoundedTextField(35, CORNER_RADIUS_FIELD, COLOR_FIELD_BG_LIGHT, COLOR_TEXT_FIELD_CONTENT, COLOR_FIELD_BORDER_SUBTLE);
        ffmpegPathField.setText(USER_CUSTOM_FFMPEG_PATH); // Pré-remplir avec la préférence chargée
        ffmpegPathField.setToolTipText("Chemin personnalisé vers l'exécutable FFmpeg (ex: C:\\ffmpeg\\bin\\ffmpeg.exe)");

        browseFfmpegButton = new RoundedButton("Parcourir...", COLOR_ACCENT_PRIMARY, CORNER_RADIUS_BUTTON);
        browseFfmpegButton.setPreferredSize(new Dimension(180, 45)); // Taille plus petite pour ce bouton
        browseFfmpegButton.setToolTipText("Rechercher l'exécutable FFmpeg");

        applyFfmpegPathButton = new RoundedButton("Appliquer Chemin", new Color(100, 180, 100), CORNER_RADIUS_BUTTON);
        applyFfmpegPathButton.setPreferredSize(new Dimension(200, 45));
        applyFfmpegPathButton.setToolTipText("Appliquer et tester le chemin FFmpeg personnalisé");


        progressBar = new JProgressBar();
        styleProgressBar(progressBar);
        progressBar.setVisible(false);

        convertButton = new RoundedButton("CONVERTIR", COLOR_ACCENT_SUCCESS, CORNER_RADIUS_BUTTON);
        convertButton.setToolTipText("Démarrer la conversion du/des fichier(s) audio.");

        clearButton = new RoundedButton("EFFACER", COLOR_ACCENT_DANGER, CORNER_RADIUS_BUTTON);
        clearButton.setToolTipText("Effacer les chemins des fichiers d'entrée et de sortie.");

        statusLabel = new ShadowLabel("Prêt à convertir !", COLOR_STATUS_READY, COLOR_SHADOW_SUBTLE, 2, 2, 0.7f);
        statusLabel.setFont(FONT_STATUS);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void layoutComponents() {
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage, COLOR_PRIMARY_BG);
        backgroundPanel.setLayout(new BorderLayout(25, 25));
        backgroundPanel.setBorder(new EmptyBorder(30, 30, 30, 30)); // Réduit un peu le padding général
        backgroundPanel.setTransferHandler(new FileDropHandler());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        JLabel titleLabel = new ShadowLabel("CONVERTISSEUR AUDIO", COLOR_ACCENT_PRIMARY, COLOR_SHADOW_SUBTLE, 3, 3, 0.8f);
        titleLabel.setFont(FONT_TITLE);
        headerPanel.add(titleLabel);
        backgroundPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new RoundedPanel(COLOR_SECONDARY_PANEL_BG, CORNER_RADIUS_PANEL);
        contentPanel.setLayout(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12); // Espacement réduit
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Ligne 0: Fichier d'entrée
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(createLabel("Fichier(s) d'Entrée :"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        contentPanel.add(inputFilePathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        contentPanel.add(selectFileButton, gbc);

        // Ligne 1: Format de Sortie
        gbc.gridx = 0; gbc.gridy = 1;
        contentPanel.add(createLabel("Format de Sortie :"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        contentPanel.add(outputFormatComboBox, gbc);
        gbc.gridwidth = 1;

        // Ligne 2: Fichier de Sortie
        gbc.gridx = 0; gbc.gridy = 2;
        contentPanel.add(createLabel("Fichier de Sortie :"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        contentPanel.add(outputFilePathField, gbc);
        gbc.gridwidth = 1;

        // --- NOUVEAU: Ligne 3 pour le chemin FFmpeg ---
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        contentPanel.add(createLabel("Chemin FFmpeg :"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        contentPanel.add(ffmpegPathField, gbc);
        gbc.gridx = 2; gbc.weightx = 0;
        contentPanel.add(browseFfmpegButton, gbc);

        // --- NOUVEAU: Ligne 4 pour le bouton Appliquer Chemin FFmpeg ---
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST; // Aligner à droite
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(applyFfmpegPathButton, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; // Restaurer pour les suivants
        gbc.anchor = GridBagConstraints.CENTER; // Restaurer
        gbc.gridwidth = 1;


        // Ligne 5: Barre de progression (était ligne 3)
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3;
        gbc.insets = new Insets(15, 12, 8, 12);
        contentPanel.add(progressBar, gbc);

        // Ligne 6: Boutons d'action (était ligne 4)
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionButtonsPanel.setOpaque(false);
        actionButtonsPanel.add(convertButton);
        actionButtonsPanel.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 12, 8, 12);
        contentPanel.add(actionButtonsPanel, gbc);

        backgroundPanel.add(contentPanel, BorderLayout.CENTER);
        statusLabel.setBorder(new EmptyBorder(12, 0, 12, 0));
        statusLabel.setOpaque(false);
        backgroundPanel.add(statusLabel, BorderLayout.SOUTH);
        add(backgroundPanel);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(COLOR_TEXT_MAIN);
        label.setFont(FONT_PRIMARY);
        return label;
    }

    private void styleProgressBar(JProgressBar bar) {
        bar.setBackground(COLOR_FIELD_BG_LIGHT);
        bar.setForeground(COLOR_ACCENT_PRIMARY);
        bar.setBorder(BorderFactory.createLineBorder(COLOR_ACCENT_PRIMARY.darker(), 1));
        bar.setPreferredSize(new Dimension(200, 18));
        bar.setStringPainted(true);
        bar.setFont(FONT_FIELD.deriveFont(Font.BOLD, 14f));
        bar.setIndeterminate(false);
    }

    private void addEventListeners() {
        selectFileButton.addActionListener(e -> selectFileAction());
        outputFormatComboBox.addActionListener(e -> {
            if (filesToConvert != null && filesToConvert.size() == 1) {
                updateOutputFilePathAction(filesToConvert.get(0));
            } else if (filesToConvert == null || filesToConvert.isEmpty()) {
                if (!inputFilePathField.getText().isEmpty() && !inputFilePathField.getText().contains("fichiers chargés")) {
                    File tempFile = new File(inputFilePathField.getText());
                    if(tempFile.isFile()){
                        updateOutputFilePathAction(tempFile);
                    }
                }
            }
        });
        convertButton.addActionListener(e -> convertFilesAction());
        clearButton.addActionListener(e -> clearFieldsAction());

        // --- NOUVEAU: Écouteurs pour les boutons du chemin FFmpeg ---
        browseFfmpegButton.addActionListener(e -> browseForFfmpegAction());
        applyFfmpegPathButton.addActionListener(e -> applyCustomFfmpegPathAction());
    }

    // --- NOUVEAU: Action pour parcourir FFmpeg ---
    private void browseForFfmpegAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sélectionner l'exécutable FFmpeg");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // Tenter de présélectionner le répertoire du chemin actuel
        File currentFfmpegFile = new File(ffmpegPathField.getText());
        if (currentFfmpegFile.exists()) {
            if (currentFfmpegFile.isFile()) {
                fileChooser.setSelectedFile(currentFfmpegFile);
            } else if (currentFfmpegFile.isDirectory()) {
                fileChooser.setCurrentDirectory(currentFfmpegFile);
            }
        } else if (currentFfmpegFile.getParentFile() != null && currentFfmpegFile.getParentFile().exists()) {
             fileChooser.setCurrentDirectory(currentFfmpegFile.getParentFile());
        }


        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            ffmpegPathField.setText(selectedFile.getAbsolutePath());
            updateStatus("Nouveau chemin FFmpeg sélectionné. Cliquez sur 'Appliquer Chemin' pour l'utiliser.", COLOR_STATUS_READY);
        }
    }

    // --- NOUVEAU: Action pour appliquer le chemin FFmpeg personnalisé ---
    private void applyCustomFfmpegPathAction() {
        String newPath = ffmpegPathField.getText().trim();
        if (newPath.isEmpty()) {
            // Si le champ est vidé, on efface la préférence (pour revenir aux autres méthodes de détection)
            saveUserFfmpegPathToPreferences("");
            USER_CUSTOM_FFMPEG_PATH = "";
            ffmpegPathField.setText(""); // S'assurer que le champ est vide
             updateStatus("Chemin FFmpeg personnalisé effacé. Réinitialisation...", COLOR_STATUS_READY);
        } else {
            File ffmpegFile = new File(newPath);
            if (!ffmpegFile.exists() || !ffmpegFile.isFile()) {
                showErrorMessage("Chemin FFmpeg Invalide", "Le chemin spécifié n'existe pas ou n'est pas un fichier : \n" + newPath);
                return;
            }
            if (!ffmpegFile.canExecute()) {
                // Sur Windows, canExecute peut être trompeur. On se fie plus à l'existence et au fait que ce soit un fichier.
                // Sur Linux/macOS, c'est plus pertinent. On affiche un avertissement.
                 String os = System.getProperty("os.name").toLowerCase();
                 if (!os.contains("win")) {
                    int choice = JOptionPane.showConfirmDialog(this,
                        "Le fichier FFmpeg spécifié existe mais n'a pas la permission d'exécution :\n" + newPath +
                        "\nVoulez-vous quand même essayer de l'utiliser ?",
                        "Permission d'Exécution Manquante", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (choice == JOptionPane.NO_OPTION) {
                        return;
                    }
                 }
            }
            saveUserFfmpegPathToPreferences(newPath);
            USER_CUSTOM_FFMPEG_PATH = newPath; // S'assurer que la variable statique est à jour
            updateStatus("Nouveau chemin FFmpeg sauvegardé. Test en cours...", COLOR_ACCENT_PRIMARY);
        }

        // Réinitialiser le chemin effectif de FFmpeg et tester
        initializeFfmpegPath(); // Cela va re-parcourir la logique de priorité
        checkFfmpegOnStartup(); // Cela va tester le FFMPEG_EXECUTABLE nouvellement défini et afficher un message
        // Le message de checkFfmpegOnStartup indiquera le succès ou l'échec.
        // Si checkFfmpegOnStartup est silencieux (pas de message d'erreur), c'est bon.
        // On pourrait ajouter un message de succès explicite ici après un petit délai si checkFfmpegOnStartup ne le fait pas.
         SwingUtilities.invokeLater(() -> {
            if (isFfmpegAvailable()) {
                updateStatus("Chemin FFmpeg appliqué et fonctionnel : " + FFMPEG_EXECUTABLE, COLOR_ACCENT_SUCCESS);
            } else {
                // checkFfmpegOnStartup aura déjà affiché une erreur plus détaillée.
                // On pourrait juste mettre un statut général ici.
                updateStatus("Échec de l'application du chemin FFmpeg. Vérifiez les messages.", COLOR_STATUS_ERROR);
            }
        });
    }


    private void selectFileAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sélectionner un fichier audio d'entrée");
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] selectedFilesArray = fileChooser.getSelectedFiles();
            if (selectedFilesArray != null && selectedFilesArray.length > 0) {
                filesToConvert.clear();
                filesToConvert.addAll(Arrays.asList(selectedFilesArray));

                if (filesToConvert.size() == 1) {
                    File singleFile = filesToConvert.get(0);
                    inputFilePathField.setText(singleFile.getAbsolutePath());
                    updateOutputFilePathAction(singleFile);
                    updateStatus("Fichier '" + singleFile.getName() + "' chargé.", COLOR_STATUS_READY);
                } else {
                    inputFilePathField.setText(filesToConvert.size() + " fichiers chargés.");
                    outputFilePathField.setText(""); // Clear output for batch
                    updateStatus(filesToConvert.size() + " fichiers prêts pour conversion.", COLOR_STATUS_READY);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateOutputFilePathAction(File inputFile) {
        if (inputFile == null || !inputFile.exists()) {
            outputFilePathField.setText("");
            return;
        }

        String fileNameWithoutExtension = getFileNameWithoutExtension(inputFile.getName());
        String selectedFormat = (String) outputFormatComboBox.getSelectedItem();

        if (selectedFormat == null) {
            if (SUPPORTED_FORMATS.length > 0) {
                selectedFormat = SUPPORTED_FORMATS[0];
                outputFormatComboBox.setSelectedIndex(0);
            } else {
                showErrorMessage("Erreur de Format", "Aucun format de sortie n'est disponible.");
                outputFilePathField.setText("");
                return;
            }
        }

        String outputDir = inputFile.getParent();
        if (outputDir == null) {
            outputDir = ".";
        }
        File outputFile = new File(outputDir, fileNameWithoutExtension + "_converted." + selectedFormat);
        outputFilePathField.setText(outputFile.getAbsolutePath());
    }


    private String getFileNameWithoutExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    private void clearFieldsAction() {
        inputFilePathField.setText("");
        outputFilePathField.setText("");
        if (filesToConvert != null) {
            filesToConvert.clear();
        }
        updateStatus("Prêt à convertir !", COLOR_STATUS_READY);
        setConversionControlsEnabled(true);
        progressBar.setVisible(false);
        progressBar.setValue(0);
        progressBar.setString("");
    }

    private void convertFilesAction() {
        if (FFMPEG_EXECUTABLE == null || !isFfmpegAvailable()) {
            showErrorMessage("FFmpeg Non Configuré", "FFmpeg n'est pas correctement configuré ou introuvable.\n" +
                                                  "Veuillez vérifier le chemin dans les paramètres ou le message au démarrage.");
            checkFfmpegOnStartup(); // Re-trigger check to show detailed message
            return;
        }

        if (filesToConvert == null || filesToConvert.isEmpty()) {
            String singlePath = inputFilePathField.getText();
            if (!singlePath.isEmpty() && !singlePath.contains("fichiers chargés")) {
                File potentialFile = new File(singlePath);
                if (potentialFile.isFile()) {
                    filesToConvert = new ArrayList<>();
                    filesToConvert.add(potentialFile);
                    if(outputFilePathField.getText().isEmpty() || !outputFilePathField.getText().contains(getFileNameWithoutExtension(potentialFile.getName()))) {
                        updateOutputFilePathAction(potentialFile);
                    }
                } else {
                    showErrorMessage("Erreur d'entrée", "Le chemin d'entrée n'est pas un fichier valide ou aucun fichier n'a été sélectionné.");
                    return;
                }
            } else {
                showErrorMessage("Erreur d'entrée", "Veuillez sélectionner un ou plusieurs fichiers d'entrée.");
                return;
            }
        }
        for (File f : filesToConvert) {
            if (!f.exists() || !f.isFile()) {
                showErrorMessage("Fichier Invalide", "La liste contient un fichier invalide ou non existant : " + f.getName());
                filesToConvert.clear();
                inputFilePathField.setText("");
                outputFilePathField.setText("");
                return;
            }
        }
        
        String targetFormat = (String) outputFormatComboBox.getSelectedItem();
        if (targetFormat == null) {
             showErrorMessage("Format de Sortie Manquant", "Veuillez sélectionner un format de sortie.");
             return;
        }
        if (filesToConvert.size() == 1 && outputFilePathField.getText().isEmpty()) {
            updateOutputFilePathAction(filesToConvert.get(0));
            if (outputFilePathField.getText().isEmpty()) {
                 showErrorMessage("Erreur de Sortie", "Impossible de déterminer le chemin du fichier de sortie.");
                 return;
            }
        }

        updateStatus("Préparation de la conversion...", COLOR_STATUS_PROCESSING);
        setConversionControlsEnabled(false);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setMaximum(filesToConvert.size());
        progressBar.setString("0/" + filesToConvert.size() + " fichiers");

        final List<File> currentFilesToConvert = new ArrayList<>(filesToConvert);
        ConversionWorker worker = new ConversionWorker(currentFilesToConvert, targetFormat);
        worker.execute();
    }

    private static class ConversionResult {
        // ... (inchangé)
        private final String inputPath;
        private final int exitCode;
        private final String outputPath;
        private final String ffmpegOutput;
        private final boolean skipped;

        public ConversionResult(String inputPath, int exitCode, String outputPath, String ffmpegOutput, boolean skipped) {
            this.inputPath = inputPath;
            this.exitCode = exitCode;
            this.outputPath = outputPath;
            this.ffmpegOutput = ffmpegOutput;
            this.skipped = skipped;
        }
        public String getInputPath() { return inputPath; }
        public int getExitCode() { return exitCode; }
        public String getOutputPath() { return outputPath; }
        public String getFfmpegOutput() { return ffmpegOutput; }
        public boolean isSkipped() { return skipped; }
    }

    private static class ConversionProgress {
        // ... (inchangé)
        final int currentFileNumber;
        final int totalFiles;
        final String currentFileName;
        final String statusMessage;

        public ConversionProgress(int currentFileNumber, int totalFiles, String currentFileName, String statusMessage) {
            this.currentFileNumber = currentFileNumber;
            this.totalFiles = totalFiles;
            this.currentFileName = currentFileName;
            this.statusMessage = statusMessage;
        }
    }

    class ConversionWorker extends SwingWorker<List<ConversionResult>, ConversionProgress> {
        // ... (inchangé, utilise FFMPEG_EXECUTABLE qui est mis à jour globalement)
        private final List<File> filesToProcess;
        private final String outputFormat;

        public ConversionWorker(List<File> filesToProcess, String outputFormat) {
            this.filesToProcess = filesToProcess; 
            this.outputFormat = outputFormat;
        }

        @Override
        protected List<ConversionResult> doInBackground() throws Exception {
            List<ConversionResult> results = new ArrayList<>();
            int fileNumber = 0;
            for (File inputFile : filesToProcess) {
                fileNumber++;
                String inputPath = inputFile.getAbsolutePath();
                String baseName = getFileNameWithoutExtension(inputFile.getName());
                String outputDirParent = inputFile.getParent();
                if (outputDirParent == null) outputDirParent = ".";
                String outputPath = new File(outputDirParent, baseName + "_converted." + outputFormat).getAbsolutePath();

                publish(new ConversionProgress(fileNumber, filesToProcess.size(), inputFile.getName(), "Conversion de " + inputFile.getName() + "..."));

                if (inputPath.equals(outputPath)) {
                    System.err.println("Conversion ignorée pour " + inputPath + ": les chemins d'entrée et de sortie sont identiques.");
                    results.add(new ConversionResult(inputPath, -2, outputPath, "Ignoré : Entrée et sortie identiques.", true));
                    continue;
                }

                List<String> command = Arrays.asList(FFMPEG_EXECUTABLE, "-i", inputPath, "-y", outputPath);
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = null;
                StringBuilder ffmpegOutputLog = new StringBuilder();
                int exitCode = -1; 

                try {
                    process = pb.start();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            ffmpegOutputLog.append(line).append(System.lineSeparator());
                        }
                    }
                    exitCode = process.waitFor();
                    results.add(new ConversionResult(inputPath, exitCode, outputPath, ffmpegOutputLog.toString(), false));
                } catch (IOException e) {
                    System.err.println("IOException lors de la conversion de " + inputPath + " avec la commande '" + String.join(" ", command) + "': " + e.getMessage());
                    results.add(new ConversionResult(inputPath, -1, outputPath, "IOException: " + e.getMessage() + "\nCommande: " + String.join(" ", command), false));
                } catch (InterruptedException e) {
                    System.err.println("Conversion interrompue pour " + inputPath + ": " + e.getMessage());
                    results.add(new ConversionResult(inputPath, -1, outputPath, "Interrompu: " + e.getMessage(), false));
                    Thread.currentThread().interrupt();
                    break; 
                } finally {
                    if (process != null) {
                        process.destroy(); 
                    }
                }
            }
            return results;
        }

        @Override
        protected void process(List<ConversionProgress> chunks) {
            // ... (inchangé)
            ConversionProgress latestProgress = chunks.get(chunks.size() - 1);
            progressBar.setValue(latestProgress.currentFileNumber);
            progressBar.setString(latestProgress.currentFileNumber + "/" + latestProgress.totalFiles + " fichiers");
            updateStatus(latestProgress.statusMessage, COLOR_STATUS_PROCESSING);
        }

        @Override
        protected void done() {
            // ... (inchangé)
            try {
                List<ConversionResult> batchResults = get();
                int successCount = 0;
                int skippedCount = 0;
                StringBuilder summaryMessage = new StringBuilder("<html>Traitement par lot terminé.<br>");
                StringBuilder errorsDetails = new StringBuilder();

                for (ConversionResult result : batchResults) {
                    if (result.isSkipped()) {
                        skippedCount++;
                        summaryMessage.append("Fichier ignoré (entrée=sortie) : ").append(new File(result.getInputPath()).getName()).append("<br>");
                    } else if (result.getExitCode() == 0) {
                        successCount++;
                    } else {
                        errorsDetails.append("<b>Échec pour : ").append(new File(result.getInputPath()).getName()).append("</b><br>");
                        errorsDetails.append("Sortie prévue : ").append(result.getOutputPath()).append("<br>");
                        errorsDetails.append("Code de retour FFmpeg : ").append(result.getExitCode()).append("<br>");
                        errorsDetails.append("Log FFmpeg (dernières lignes) : <pre>").append(getLastLines(result.getFfmpegOutput(), 5)).append("</pre><br><br>");
                        System.err.println("--- Log FFmpeg complet pour ÉCHEC " + result.getInputPath() + " ---\n" + result.getFfmpegOutput() + "\n--- FIN ---");
                    }
                }
                int attemptedConversions = filesToProcess.size() - skippedCount;
                summaryMessage.append(successCount).append(" sur ").append(Math.max(0, attemptedConversions)).append(" conversion(s) tentée(s) réussie(s).<br>");

                if (skippedCount > 0) {
                    summaryMessage.append(skippedCount).append(" fichier(s) ignoré(s).<br>");
                }
                if (errorsDetails.length() > 0) {
                    summaryMessage.append("<br><b>Détails des erreurs :</b><br>").append(errorsDetails.toString());
                }
                summaryMessage.append("</html>");

                updateStatus("Traitement par lot terminé.", (successCount == attemptedConversions && attemptedConversions >=0 ? COLOR_ACCENT_SUCCESS : COLOR_STATUS_ERROR) );
                showInfoMessage("Résumé de la Conversion", summaryMessage.toString());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                updateStatus("Conversion par lot interrompue.", COLOR_STATUS_ERROR);
                showErrorMessage("Conversion Interrompue", "Le processus de conversion par lot a été interrompu.");
            } catch (ExecutionException e) {
                updateStatus("Erreur d'exécution de la conversion par lot.", COLOR_STATUS_ERROR);
                Throwable cause = e.getCause();
                String causeMessage = (cause != null) ? cause.getMessage() : e.getMessage();
                showErrorMessage("Erreur d'Exécution de Conversion", "Une erreur inattendue s'est produite lors du traitement : " + causeMessage);
                System.err.println("Erreur d'exécution dans ConversionWorker:");
                e.printStackTrace();
            } finally {
                progressBar.setValue(progressBar.getMaximum());
                progressBar.setString("Terminé");
                setConversionControlsEnabled(true);
                if (filesToConvert != null) filesToConvert.clear();
                inputFilePathField.setText("");
                outputFilePathField.setText("");
            }
        }
    }

    private String getLastLines(String text, int lineCount) {
        // ... (inchangé)
        if (text == null || text.isEmpty()) {
            return "";
        }
        String[] lines = text.split("\\r?\\n"); 
        int start = Math.max(0, lines.length - lineCount);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < lines.length; i++) {
            sb.append(lines[i].replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")).append("<br>");
        }
        return sb.toString().trim();
    }

    private void setConversionControlsEnabled(boolean enabled) {
        // ... (inchangé)
        convertButton.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        selectFileButton.setEnabled(enabled);
        outputFormatComboBox.setEnabled(enabled);
        // --- NOUVEAU: Gérer l'état des nouveaux boutons ---
        ffmpegPathField.setEnabled(enabled);
        browseFfmpegButton.setEnabled(enabled);
        applyFfmpegPathButton.setEnabled(enabled);
    }

    private void updateStatus(final String text, final Color color) {
        // ... (inchangé)
        if (SwingUtilities.isEventDispatchThread()) {
            statusLabel.setLabelText(text);
            statusLabel.setLabelColor(color);
        } else {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setLabelText(text);
                statusLabel.setLabelColor(color);
            });
        }
    }

    private boolean isFfmpegAvailable() {
        // ... (inchangé, utilise FFMPEG_EXECUTABLE)
        if (FFMPEG_EXECUTABLE == null || FFMPEG_EXECUTABLE.trim().isEmpty()) {
             System.err.println("FFMPEG_EXECUTABLE path is not set before checking availability.");
             return false;
        }
        try {
            Process process = new ProcessBuilder(FFMPEG_EXECUTABLE, "-version").start();
            try (BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                while (inputStreamReader.readLine() != null || errorStreamReader.readLine() != null) { /* Just consume */ }
            }
            return process.waitFor() == 0;
        } catch (IOException e) {
            System.err.println("IOException lors de la vérification de FFmpeg avec la commande '" + FFMPEG_EXECUTABLE + "': " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("InterruptedException lors de la vérification de FFmpeg: " + e.getMessage());
            return false;
        }
    }

    // --- MODIFIÉ: Message d'erreur au démarrage plus détaillé ---
    private void checkFfmpegOnStartup() {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception { return isFfmpegAvailable(); }
            @Override protected void done() {
                try {
                    if (!get()) {
                        String ffmpegNameToPlace = System.getProperty("os.name").toLowerCase().contains("win") ? "ffmpeg.exe" : "ffmpeg";
                        
                        String details = (LOCAL_FFMPEG_SEARCH_DETAILS != null ? LOCAL_FFMPEG_SEARCH_DETAILS : "Détails de recherche non disponibles.");
                        if (FFMPEG_EXECUTABLE == null || FFMPEG_EXECUTABLE.trim().isEmpty()){
                             details += "\n\nCRITIQUE: Le chemin FFmpeg effectif (FFMPEG_EXECUTABLE) est vide ou nul après initialisation.";
                        } else {
                             details += "\n\nChemin FFmpeg testé (échoué) : '" + FFMPEG_EXECUTABLE + "'";
                        }


                        JTextArea textArea = new JTextArea(
                                "FFmpeg n'a pas pu être démarré ou trouvé.\n\n" +
                                "Détails de la recherche et configuration actuelle :\n" + 
                                "--------------------------------------------------\n" +
                                details + "\n" +
                                "--------------------------------------------------\n\n" +
                                "Solutions suggérées :\n" +
                                "1. Utilisez le champ 'Chemin FFmpeg' ci-dessus pour spécifier le chemin exact vers " + ffmpegNameToPlace + " et cliquez sur 'Appliquer Chemin'.\n" +
                                "2. OU placez " + ffmpegNameToPlace + " dans un des emplacements de recherche prioritaires (voir détails ci-dessus).\n" +
                                "3. OU installez FFmpeg globalement et assurez-vous qu'il est dans le PATH de votre système.\n\n" +
                                "Vous pouvez télécharger FFmpeg depuis ffmpeg.org."
                        );
                        textArea.setEditable(false);
                        textArea.setWrapStyleWord(true);
                        textArea.setLineWrap(true);
                        textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
                        textArea.setBackground(null); 

                        JScrollPane scrollPane = new JScrollPane(textArea);
                        scrollPane.setPreferredSize(new Dimension(650, 400)); 

                        JOptionPane.showMessageDialog(AudioConverter.this, scrollPane, "FFmpeg Introuvable ou Inexécutable", JOptionPane.ERROR_MESSAGE);
                        updateStatus("FFmpeg introuvable ou non fonctionnel. Vérifiez le chemin.", COLOR_STATUS_ERROR);
                    } else {
                         updateStatus("FFmpeg prêt : " + FFMPEG_EXECUTABLE, COLOR_ACCENT_SUCCESS);
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); System.err.println("Vérification FFmpeg interrompue.");
                } catch (ExecutionException e) { 
                    System.err.println("Erreur lors de la vérification de FFmpeg: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                    showErrorMessage("Erreur Vérification FFmpeg", "Une exception s'est produite lors de la vérification de FFmpeg : " + e.getMessage());
                    updateStatus("Erreur lors de la vérification de FFmpeg.", COLOR_STATUS_ERROR);
                }
            }
        }.execute();
    }

    private void showErrorMessage(final String title, final String message) {
        // ... (inchangé)
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(AudioConverter.this, message, title, JOptionPane.ERROR_MESSAGE));
    }

    private void showInfoMessage(final String title, final String message) {
        // ... (inchangé)
        SwingUtilities.invokeLater(() -> {
            JEditorPane ep = new JEditorPane("text/html", message);
            ep.setEditable(false);
            ep.setBackground(this.getBackground()); 
            JScrollPane scrollPane = new JScrollPane(ep);
            scrollPane.setPreferredSize(new Dimension(600, 400));
            JOptionPane.showMessageDialog(AudioConverter.this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AudioConverter().setVisible(true));
    }

    // --- Inner Classes for UI Components (inchangées) ---
    static class RoundedPanel extends JPanel {
        // ... (inchangé)
        private final Color backgroundColor; private final int cornerRadius;
        public RoundedPanel(Color bgColor, int radius) { super(); this.backgroundColor = bgColor; this.cornerRadius = radius; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(backgroundColor); g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius); g2.dispose(); }
    }
    static class ShadowLabel extends JLabel {
        // ... (inchangé)
        private Color mainTextColor; private final Color shadowColor; private final int shadowOffsetX; private final int shadowOffsetY; private final float shadowOpacity;
        public ShadowLabel(String text, Color textColor, Color shadowColor, int shadowOffsetX, int shadowOffsetY, float shadowOpacity) { super(text); this.mainTextColor = textColor; this.shadowColor = shadowColor; this.shadowOffsetX = shadowOffsetX; this.shadowOffsetY = shadowOffsetY; this.shadowOpacity = shadowOpacity; setForeground(textColor); setOpaque(false); }
        public void setLabelText(String text) { setText(text); }
        public void setLabelColor(Color color) { this.mainTextColor = color; setForeground(color); repaint(); }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setFont(getFont()); FontMetrics fm = g2.getFontMetrics(); Insets insets = getInsets(); int textX; int hAlign = getHorizontalAlignment(); if (hAlign==LEADING||hAlign==LEFT) {textX=insets.left;} else if (hAlign==TRAILING||hAlign==RIGHT) {textX=getWidth()-insets.right-fm.stringWidth(getText());} else {textX=insets.left+(getWidth()-insets.left-insets.right-fm.stringWidth(getText()))/2;} int textY; int vAlign = getVerticalAlignment(); if (vAlign==TOP){textY=insets.top+fm.getAscent();} else if (vAlign==BOTTOM){textY=getHeight()-insets.bottom-fm.getDescent();} else {textY=insets.top+(getHeight()-insets.top-insets.bottom-fm.getHeight())/2+fm.getAscent();} g2.setColor(new Color(shadowColor.getRed(),shadowColor.getGreen(),shadowColor.getBlue(),(int)(255*shadowOpacity))); g2.drawString(getText(),textX+shadowOffsetX,textY+shadowOffsetY); g2.setColor(mainTextColor); g2.drawString(getText(),textX,textY); g2.dispose(); }
    }
    static class RoundedTextField extends JTextField {
        // ... (inchangé)
        private final int cornerRadius; private final Color fieldBgColor; private final Color fieldBorderColor;
        public RoundedTextField(int columns, int radius, Color bgColor, Color textColor, Color borderColor) { super(columns); this.cornerRadius=radius; this.fieldBgColor=bgColor; this.fieldBorderColor=borderColor; setOpaque(false); setForeground(textColor); setFont(FONT_FIELD); setCaretColor(textColor); setBorder(new EmptyBorder(10,12,10,12)); }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(fieldBgColor); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,cornerRadius,cornerRadius); g2.dispose(); super.paintComponent(g); }
        @Override protected void paintBorder(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(fieldBorderColor); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,cornerRadius,cornerRadius); g2.dispose(); }
    }
    static class RoundedComboBox<E> extends JComboBox<E> {
        // ... (inchangé)
        private final int cornerRadius; private final Color fieldBgColor; private final Color fieldBorderColor;
        public RoundedComboBox(E[] items, int radius, Color bgColor, Color textColor, Color borderColor) { super(items); this.cornerRadius=radius; this.fieldBgColor=bgColor; this.fieldBorderColor=borderColor; setOpaque(false); setForeground(textColor); setFont(FONT_FIELD); setBorder(new EmptyBorder(7,10,7,10)); setRenderer(new RoundedListCellRenderer(bgColor,textColor,COLOR_ACCENT_PRIMARY.darker())); if (items != null && items.length > 0) { setSelectedIndex(0); } }
        @Override protected void paintComponent(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(fieldBgColor); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,cornerRadius,cornerRadius); g2.dispose(); super.paintComponent(g); }
        @Override protected void paintBorder(Graphics g) { Graphics2D g2 = (Graphics2D) g.create(); g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2.setColor(fieldBorderColor); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,cornerRadius,cornerRadius); g2.dispose(); }
        private static class RoundedListCellRenderer extends BasicComboBoxRenderer {
             // ... (inchangé)
            private final Color defaultBg; private final Color defaultFg; private final Color selectionBg;
            public RoundedListCellRenderer(Color dbg, Color dfg, Color sbg){super();this.defaultBg=dbg;this.defaultFg=dfg;this.selectionBg=sbg;setOpaque(true);setBorder(new EmptyBorder(5,8,5,8));}
            @Override public Component getListCellRendererComponent(JList l,Object v,int i,boolean s,boolean f){super.getListCellRendererComponent(l,v,i,s,f); if(s){setBackground(selectionBg);setForeground(Color.WHITE);}else{setBackground(defaultBg);setForeground(defaultFg);} return this;}
        }
    }
    static class RoundedButton extends JButton {
        // ... (inchangé)
        private final Color originalBgColor; private final Color originalBorderColor; private final int cornerRadius; private Color currentBgColor;
        public RoundedButton(String text, Color bgColor, int radius) { super(text); this.originalBgColor=bgColor; this.currentBgColor=bgColor; this.originalBorderColor=bgColor.darker(); this.cornerRadius=radius; setFont(FONT_BUTTON); setForeground(Color.WHITE); setFocusPainted(false); setBorder(new EmptyBorder(12,25,12,25)); setCursor(new Cursor(Cursor.HAND_CURSOR)); setPreferredSize(new Dimension(220,55)); setContentAreaFilled(false); setOpaque(false); addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){if(isEnabled()){currentBgColor=originalBgColor.brighter();repaint();}} @Override public void mouseExited(MouseEvent e){if(isEnabled()){currentBgColor=originalBgColor;repaint();}} @Override public void mousePressed(MouseEvent e){if(isEnabled()){currentBgColor=originalBgColor.darker();repaint();}} @Override public void mouseReleased(MouseEvent e){if(isEnabled()){if(contains(e.getPoint())){currentBgColor=originalBgColor.brighter();}else{currentBgColor=originalBgColor;}repaint();}}});}
        @Override public void setEnabled(boolean b){super.setEnabled(b);if(!b){currentBgColor=new Color(180,180,180);setForeground(new Color(220,220,220));}else{currentBgColor=originalBgColor;setForeground(Color.WHITE);}repaint();}
        @Override protected void paintComponent(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g2.setColor(currentBgColor); g2.fillRoundRect(0,0,getWidth()-1,getHeight()-1,cornerRadius,cornerRadius);super.paintComponent(g);g2.dispose();}
        @Override protected void paintBorder(Graphics g){Graphics2D g2=(Graphics2D)g.create();g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);if(isEnabled()){g2.setColor(originalBorderColor);}else{g2.setColor(new Color(150,150,150));} g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,cornerRadius,cornerRadius);g2.dispose();}
    }
    static class BackgroundPanel extends JPanel {
        // ... (inchangé)
        private final Image backgroundImage; private final Color fallbackBackgroundColor;
        public BackgroundPanel(Image image, Color fallbackColor) { this.backgroundImage=image; this.fallbackBackgroundColor=fallbackColor; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) { super.paintComponent(g); Graphics2D g2 = (Graphics2D) g.create(); if (backgroundImage!=null) { g2.drawImage(backgroundImage,0,0,getWidth(),getHeight(),this); } else { g2.setColor(fallbackBackgroundColor); g2.fillRect(0,0,getWidth(),getHeight()); } g2.dispose(); }
    }

    class FileDropHandler extends TransferHandler {
        // ... (inchangé)
        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) return false;
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            Transferable t = support.getTransferable();
            try {
                final List<File> droppedFiles = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                if (droppedFiles != null && !droppedFiles.isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        filesToConvert.clear(); 
                        filesToConvert.addAll(droppedFiles);

                        if (filesToConvert.size() == 1) {
                            File singleFile = filesToConvert.get(0);
                            inputFilePathField.setText(singleFile.getAbsolutePath());
                            updateOutputFilePathAction(singleFile); 
                            updateStatus("Fichier '" + singleFile.getName() + "' chargé par glisser-déposer.", COLOR_STATUS_READY);
                        } else {
                            inputFilePathField.setText(filesToConvert.size() + " fichiers chargés par glisser-déposer.");
                            outputFilePathField.setText(""); 
                            updateStatus(filesToConvert.size() + " fichiers prêts pour conversion.", COLOR_STATUS_READY);
                        }
                    });
                    return true;
                }
            } catch (UnsupportedFlavorException e) {
                System.err.println("UnsupportedFlavorException lors du glisser-déposer : " + e.getMessage());
                showErrorMessage("Erreur Glisser-Déposer", "Type de données non supporté. Veuillez déposer des fichiers audio.");
            } catch (IOException e) {
                System.err.println("IOException lors du glisser-déposer : " + e.getMessage());
                showErrorMessage("Erreur Glisser-Déposer", "Erreur lors de la lecture du/des fichier(s) déposé(s).");
            }
            return false;
        }
    }
}
