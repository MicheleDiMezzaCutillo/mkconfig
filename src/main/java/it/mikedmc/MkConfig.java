package it.mikedmc;

import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Properties;

public class MkConfig<T> {
    private static final Properties configProperties = new Properties();
    private static final String CONFIG_FILE = "mkconfig.properties";
    private final Class<T> configClass;

    // Costruttore che accetta la classe di configurazione
    public MkConfig(Class<T> configClass) {
        this.configClass = configClass;

        try {
            File configFile = new File(CONFIG_FILE);

            // Controlla se il file esiste
            if (!configFile.exists()) {
                createConfigFile();
                System.out.println("Ho creato il file " + CONFIG_FILE + ". Compilalo prima di riavviare il programma.");
                System.exit(0); // Termina il programma
            }

            // Carica i dati dal file esistente
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                configProperties.load(fis);
            }

            // Inizializza automaticamente i campi della classe di configurazione
            initializeFields();

        } catch (IOException e) {
            throw new RuntimeException("MkConfig: Errore durante il caricamento del file di configurazione", e);
        }
    }

    // Metodo per inizializzare i campi della classe di configurazione
    private void initializeFields() {
        Field[] fields = configClass.getDeclaredFields();

        for (Field field : fields) {
            try {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    String value = configProperties.getProperty(field.getName());
                    if (value != null) {
                        if (field.getType() == String.class) {
                            field.set(null, value);
                        } else if (field.getType() == Long.class) {
                            field.set(null, Long.parseLong(value));
                        } else if (field.getType() == Boolean.class) {
                            field.set(null, Boolean.parseBoolean(value));
                        } else if (field.getType() == LocalDate.class) {
                            field.set(null, LocalDate.parse(value));
                        }
                        // Aggiungi altri tipi di dati qui se necessario
                    }
                }
            } catch (IllegalAccessException | NumberFormatException e) {
                throw new RuntimeException("MkConfig: Errore durante l'inizializzazione del campo " + field.getName(), e);
            }
        }
    }

    // Metodo per creare il file di configurazione
    private void createConfigFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(CONFIG_FILE))) {
            Field[] fields = configClass.getDeclaredFields();

            for (Field field : fields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    String exampleValue = getExampleValue(field);
                    writer.println(field.getName() + "=" + exampleValue);
                }
            }
        }
    }

    // Metodo per fornire un valore di esempio in base al tipo del campo
    private String getExampleValue(Field field) {
        if (field.getType() == Boolean.class) {
            return "false"; // Esempio per Boolean
        } else if (field.getType() == LocalDate.class) {
            return "2024-01-01"; // Esempio per LocalDate
        }
        return ""; // Valore di fallback se il tipo non è supportato
    }
}
