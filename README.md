# Convertisseur Audio Élégant 🎶

Le Convertisseur Audio Élégant est une application de bureau avancée et de qualité professionnelle, conçue pour répondre à vos besoins de conversion audio les plus exigeants. Développé avec Java Swing, il offre une interface conviviale pour convertir vos fichiers audio entre divers formats, avec un contrôle granulaire sur les paramètres de sortie. 🎚️✨

## Fonctionnalités Clés 🚀

  * Prise en charge de multiples formats audio : MP3, WAV, FLAC, AAC, OGG, M4A, WMA, AIFF. 🎧
  * Capacités de conversion par lots avec sélection flexible du répertoire de sortie. 📂➡️📂
  * Contrôle granulaire sur les paramètres audio de sortie : débit binaire (kbps), fréquence d'échantillonnage (Hz) et canaux (Mono/Stéréo/Original). 🎯
  * Interface intuitive avec fonction glisser-déposer pour une addition facile des fichiers. 🖱️📥
  * Diagnostics d'erreurs améliorés et rapports d'état clairs. ✅❌
  * Détection automatique et configuration personnalisable du chemin de l'exécutable FFmpeg. 🛠️

## Formats Pris en Charge 🔄

L'application prend en charge les formats audio suivants pour la conversion : `mp3`, `wav`, `flac`, `aac`, `ogg`, `m4a`, `wma`, `aiff`.

## Prérequis 📋

Pour exécuter ou compiler le Convertisseur Audio Élégant, vous aurez besoin de :

  * **Java Runtime Environment (JRE) 8 ou supérieur** : Assurez-vous que Java est installé sur votre système. ☕
  * **FFmpeg** : Cette application s'appuie sur FFmpeg pour toutes les opérations de conversion audio. Vous devez avoir FFmpeg installé sur votre système. Son exécutable doit être accessible via la variable d'environnement PATH de votre système, ou vous devrez configurer son chemin directement dans l'application. ➡️🔗

## Comment Utiliser l'Application 💡

1.  **Télécharger et Exécuter** :

      * Téléchargez le fichier `AudioConverter.jar`. 📦
      * Exécutez l'application via la commande : `java -jar AudioConverter.jar`. 🖥️

2.  **Configuration de FFmpeg** :

      * Au premier lancement, l'application tentera de localiser `ffmpeg.exe` (sur Windows) ou `ffmpeg` (sur Linux/macOS) dans le PATH de votre système ou dans le même répertoire que le JAR de l'application. 🔍
      * Si FFmpeg n'est pas trouvé, le statut affichera "FFmpeg Statut: Non Trouvé". Cliquez sur le bouton "Configurer Chemin FFmpeg / Configure FFmpeg Path" pour sélectionner manuellement l'exécutable `ffmpeg` sur votre système. 📂➕

3.  **Ajouter des Fichiers** :

      * Glissez et déposez vos fichiers audio directement dans la zone de liste des fichiers de la fenêtre principale. 📂➡️🖥️
      * Alternativement, cliquez sur le bouton "Ajouter Fichiers / Add Files" pour parcourir et sélectionner des fichiers depuis votre système. ➕📁

4.  **Sélectionner la Sortie** :

      * Choisissez le "Format de Sortie / Output Format" désiré dans le menu déroulant. 📄➡️🎵
      * Cliquez sur "Parcourir / Browse" à côté de "Répertoire de Sortie / Output Directory" pour choisir l'emplacement de sauvegarde de vos fichiers convertis. 📍💾

5.  **Paramètres Avancés (Optionnel)** :

      * Cliquez sur "Paramètres Avancés / Advanced Settings" pour révéler des options supplémentaires. ⚙️
      * Cochez "Activer les paramètres personnalisés / Enable Custom Settings" pour définir un débit binaire (kbps), une fréquence d'échantillonnage (Hz) ou sélectionner les canaux (Mono/Stéréo/Original) personnalisés. 📏🔊

6.  **Convertir** :

      * Cliquez sur le bouton "Convertir Audio / Convert Audio" pour démarrer le processus de conversion. ▶️
      * Le label de statut en bas de l'écran indiquera la progression et l'achèvement de la conversion. 🟢

## Compiler depuis les Sources 💻

Si vous souhaitez compiler l'application à partir de son code source :

1.  **Clonez le dépôt** :
    ```bash
    git clone git clone https://github.com/technerdsam/AudioConverter.git
    ```
2.  **Naviguez vers le répertoire du projet** :
    ```bash
    cd AudioConverter
    ```
3.  **Compilez les fichiers source Java** :
    ```bash
    javac AudioConverter.java
    ```
    *(Note : Assurez-vous que tous les fichiers `.java` sont compilés, y compris les classes internes comme `AudioConverter$CustomComboBoxRenderer.java` si elles existent en tant que fichiers séparés, ou si elles sont dans `AudioConverter.java`, une seule commande `javac AudioConverter.java` suffira.)*
4.  **Exécutez l'application** :
    ```bash
    java AudioConverter
    ```

## Dépannage 🆘

  * **FFmpeg non trouvé** : Assurez-vous que FFmpeg est correctement installé et que son exécutable est ajouté à la variable d'environnement PATH de votre système. Sinon, utilisez le bouton "Configurer Chemin FFmpeg" dans l'application pour pointer manuellement vers l'exécutable `ffmpeg`. 🚫🔗
  * **Erreurs de conversion** : Vérifiez la sortie de la console (où vous avez exécuté le JAR) pour les messages d'erreur détaillés de FFmpeg. Assurez-vous que vos fichiers d'entrée sont des fichiers audio valides et que le répertoire de sortie sélectionné dispose des autorisations d'écriture. 📝⚠️

## Licence 📄

Ce logiciel est sous licence Creative Commons Zero v1.0 Universal.
Copyright (C) 2025 

## Auteur ✍️

Samyn-Antoy ABASSE.

## Contact 📧

Pour toute question, commentaire ou pour signaler des problèmes, n'hésitez pas à ouvrir une issue sur le dépôt GitHub. 💬

-----

# Elegant Audio Converter 🎶

The Elegant Audio Converter is an advanced, professional-grade desktop application designed to meet your most demanding audio conversion needs. Developed with Java Swing, it provides a user-friendly interface for converting your audio files between various formats, with granular control over output parameters. 🎚️✨

## Key Features 🚀

  * Support for multiple audio formats: MP3, WAV, FLAC, AAC, OGG, M4A, WMA, AIFF. 🎧
  * Batch conversion capabilities with flexible output directory selection. 📂➡️📂
  * Granular control over output audio parameters: bitrate (kbps), sample rate (Hz), and channels (Mono/Stereo/Original). 🎯
  * Intuitive drag-and-drop interface for easy file addition. 🖱️📥
  * Enhanced error diagnostics and clear status reporting. ✅❌
  * Automatic and customizable FFmpeg executable path detection. 🛠️

## Supported Formats 🔄

The application supports the following audio formats for conversion: `mp3`, `wav`, `flac`, `aac`, `ogg`, `m4a`, `wma`, `aiff`.

## Prerequisites 📋

To run or build the Elegant Audio Converter, you will need:

  * **Java Runtime Environment (JRE) 8 or higher**: Ensure Java is installed on your system. ☕
  * **FFmpeg**: This application relies on FFmpeg for all audio conversion operations. You must have FFmpeg installed on your system. Its executable needs to be accessible via your system's PATH environment variable, or you will need to configure its path directly within the application. ➡️🔗

## How to Use the Application 💡

1.  **Download & Run**:

      * Download the `AudioConverter.jar` file. 📦
      * Run the application using the command: `java -jar AudioConverter.jar`. 🖥️

2.  **FFmpeg Setup**:

      * Upon first launch, the application will attempt to locate `ffmpeg.exe` (on Windows) or `ffmpeg` (on Linux/macOS) in your system's PATH or in the same directory as the application JAR. 🔍
      * If FFmpeg is not found, the "FFmpeg Status" label will show "Non Trouvé / Not Found". Click the "Configurer Chemin FFmpeg / Configure FFmpeg Path" button to manually select the `ffmpeg` executable on your system. 📂➕

3.  **Add Files**:

      * Drag and drop your audio files directly onto the main window's file list area. 📂➡️🖥️
      * Alternatively, click the "Ajouter Fichiers / Add Files" button to browse and select files from your system. ➕📁

4.  **Select Output**:

      * Choose your desired "Format de Sortie / Output Format" from the dropdown menu. 📄➡️🎵
      * Click "Parcourir / Browse" next to "Répertoire de Sortie / Output Directory" to select where your converted files will be saved. 📍💾

5.  **Advanced Settings (Optional)**:

      * Click "Paramètres Avancés / Advanced Settings" to reveal additional options. ⚙️
      * Check "Activer les paramètres personnalisés / Enable Custom Settings" to set a custom bitrate (kbps), sample rate (Hz), or select channels (Mono/Stéréo/Original). 📏🔊

6.  **Convert**:

      * Click the "Convertir Audio / Convert Audio" button to start the conversion process. ▶️
      * The status label at the bottom will indicate the progress and completion of the conversion. 🟢

## Building from Source 💻

If you wish to build the application from its source code:

1.  **Clone the repository**:
    ```bash
    git clone git clone https://github.com/technerdsam/AudioConverter.git
    ```
2.  **Navigate to the project directory**:
    ```bash
    cd AudioConverter
    ```
3.  **Compile the Java source files**:
    ```bash
    javac AudioConverter.java
    ```
    *(Note: Ensure all `.java` files are compiled, including any inner classes like `AudioConverter$CustomComboBoxRenderer.java` if they exist as separate files, or if they are within `AudioConverter.java`, a single `javac AudioConverter.java` command will suffice.)*
4.  **Run the application**:
    ```bash
    java AudioConverter
    ```

## Troubleshooting 🆘

  * **FFmpeg Not Found**: Ensure FFmpeg is correctly installed and its executable is added to your system's PATH environment variable. If not, use the "Configure FFmpeg Path" button in the application to manually point to the `ffmpeg` executable. 🚫🔗
  * **Conversion Errors**: Check the console output (where you ran the JAR) for detailed FFmpeg error messages. Verify that your input files are valid audio files and that the selected output directory has write permissions. 📝⚠️

## License 📄

This software is licensed under the Creative Commons Zero v1.0 Universal.
Copyright (C) 2025.

## Author ✍️

Samyn-Antoy ABASSE.

## Contact 📧

For any inquiries, feedback, or to report issues, please open an issue on the GitHub repository. 💬
