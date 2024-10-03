package it.mikedmc;

import java.io.*;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Properties;

public class MkConfig {

    private static final Properties configProperties = new Properties();
    private static final String configFileName = "mkconfig.properties";

    // Blocco statico per caricare la configurazione all'avvio
    static {
        File configFile = new File(configFileName);

        // Controlla se il file esiste
        if (!configFile.exists()) {
            // Crea il file di configurazione se non esiste
            try {
                createConfigFile();
                System.out.println("Ho creato il file " + configFileName + ". Compilalo prima di riavviare il programma.");
                System.exit(0); // Termina il programma
            } catch (IOException e) {
                throw new RuntimeException("MkConfig: Errore durante la creazione del file di configurazione", e);
            }
        }

        // Carica i dati dal file esistente
        try (FileInputStream fis = new FileInputStream(configFileName)) {
            configProperties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("MkConfig: Errore durante il caricamento del file di configurazione", e);
        }

        // Inizializza automaticamente i campi
        initializeFields();
    }

    // Metodo per ottenere una stringa dalla configurazione
    private static String get(String key) {
        String value = configProperties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("MkConfig: " + key + " mancante");
        }
        return value;
    }

    // Inizializza automaticamente i campi statici definiti nella classe
    private static void initializeFields() {
        Field[] fields = MkConfig.class.getDeclaredFields();

        for (Field field : fields) {
            try {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    field.setAccessible(true); // Rende accessibili i campi privati

                    String fieldName = field.getName();
                    String value = get(fieldName);

                    if (fieldName.equals("configProperties") || field.getType() == Properties.class) {
                        continue;
                    }

                    // Verifica il tipo di dato del campo e assegna il valore appropriato
                    if (field.getType() == String.class) {
                        field.set(null, value);
                    } else if (field.getType() == Long.class || field.getType() == long.class) {
                        field.set(null, Long.parseLong(value));
                    } else if (field.getType() == Integer.class || field.getType() == int.class) {
                        field.set(null, Integer.parseInt(value));
                    } else if (field.getType() == Double.class || field.getType() == double.class) {
                        field.set(null, Double.parseDouble(value));
                    } else if (field.getType() == Float.class || field.getType() == float.class) {
                        field.set(null, Float.parseFloat(value));
                    } else if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                        field.set(null, Boolean.parseBoolean(value));
                    } else if (field.getType() == LocalDate.class) {
                        field.set(null, LocalDate.parse(value));
                    } else if (field.getType() == Character.class || field.getType() == char.class) {
                        if (value.length() != 1) {
                            throw new RuntimeException("MkConfig: Il campo " + fieldName + " non è un carattere valido");
                        }
                        field.set(null, value.charAt(0));
                    }
                }

            } catch (IllegalAccessException e) {
                throw new RuntimeException("MkConfig: Errore durante l'inizializzazione del campo " + field.getName(), e);
            } catch (NumberFormatException e) {
                throw new RuntimeException("MkConfig: " + field.getName() + " non è un valore numerico valido", e);
            }
        }
    }


    // Metodo per creare il file di configurazione
    private static void createConfigFile() throws IOException {
        File configFile = new File(configFileName);
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(configFile))) {
            Field[] fields = MkConfig.class.getDeclaredFields();
            for (Field field : fields) {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    String fieldName = field.getName();
                    String exampleValue = "";


                    if (fieldName.equals("configProperties") || field.getType() == Properties.class) {
                        continue;
                    }
                    // Imposta un valore di esempio basato sul tipo del campo
                    if (field.getType() == String.class) {
                        exampleValue = "xxxxxx"; // Esempio per String
                    } else if (field.getType() == Long.class || field.getType() == long.class) {
                        exampleValue = "123456789"; // Esempio per Long
                    } else if (field.getType() == Integer.class || field.getType() == int.class) {
                        exampleValue = "1234"; // Esempio per Integer
                    } else if (field.getType() == Double.class || field.getType() == double.class) {
                        exampleValue = "123.45"; // Esempio per Double
                    } else if (field.getType() == Float.class || field.getType() == float.class) {
                        exampleValue = "123.45"; // Esempio per Float
                    } else if (field.getType() == Boolean.class || field.getType() == boolean.class) {
                        exampleValue = "false"; // Esempio per Boolean
                    } else if (field.getType() == LocalDate.class) {
                        exampleValue = "2024-01-01"; // Esempio per LocalDate
                    } else if (field.getType() == Character.class || field.getType() == char.class) {
                        exampleValue = "x"; // Esempio per char
                    }

                    // Scrive la chiave e il valore di esempio nel file
                    writer.println(fieldName + "=" + exampleValue);
                }
            }
        }
    }
}
