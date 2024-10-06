package it.mikedmc;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class MkConfig<T> {
    private static final String CONFIG_FILE = "mkconfig.properties";
    private final Class<T> configClass;

    public MkConfig(Class<T> configClass) {
        this.configClass = configClass;

        try {
            File configFile = new File(CONFIG_FILE);

            if (!configFile.exists()) {
                createConfigFile();
                System.out.println("Ho creato il file " + CONFIG_FILE + ". Compilalo prima di riavviare il programma.");
                System.exit(0); // Termina il programma
            }

            Properties configProperties = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                configProperties.load(fis);
            }

            initializeFields(configProperties);

        } catch (IOException e) {
            throw new RuntimeException("MkConfig: Errore durante il caricamento del file di configurazione", e);
        }
    }

    /***
     * Ricarica i config da mkconfig.properties.
     */
    public void reload() {
        try {
            Properties configProperties = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                configProperties.load(fis);
            }
            initializeFields(configProperties);
        } catch (IOException e) {
            throw new RuntimeException("MkConfig: Errore durante il ricaricamento del file di configurazione", e);
        }
    }

    private void initializeFields(Properties configProperties) {
        Field[] fields = configClass.getDeclaredFields();

        for (Field field : fields) {
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    String value = configProperties.getProperty(field.getName());
                    if (value != null) {
                        if (field.getType() == String.class) {
                            field.set(null, value);
                        } else if (field.getType() == Long.class) {
                            field.set(null, Long.parseLong(value));
                        } else if (field.getType() == Integer.class) {
                            field.set(null, Integer.parseInt(value));
                        } else if (field.getType() == Boolean.class) {
                            field.set(null, Boolean.parseBoolean(value));
                        } else if (field.getType() == LocalDate.class) {
                            field.set(null, LocalDate.parse(value));
                        } else if (field.getType() == Double.class) {
                            field.set(null, Double.parseDouble(value));
                        } else if (field.getType() == Float.class) {
                            field.set(null, Float.parseFloat(value));
                        } else if (field.getType() == BigDecimal.class) {
                            field.set(null, new BigDecimal(value));
                        } else if (isCollection(field)) {
                            populateCollection(field, value);
                        } else if (field.getType().isArray()) {
                            populateArray(field, value);
                        } else if (isCustomType(field)) {
                            field.set(null, deserializeCustomType(field, value));
                        }
                    }
                }
            } catch (IllegalAccessException | NumberFormatException e) {
                throw new RuntimeException("MkConfig: Errore durante l'inizializzazione del campo " + field.getName(), e);
            }
        }
    }

    private boolean isCustomType(Field field) {
        // Controlla se il tipo è una classe personalizzata
        return !(field.getType().isPrimitive() || isCollection(field) || field.getType().isArray());
    }

    private Object deserializeCustomType(Field field, String value) {
        // Implementa la logica di deserializzazione per tipi personalizzati
        // Ad esempio, supponiamo che il tipo personalizzato abbia un costruttore che prende una stringa
        try {
            Class<?> clazz = field.getType();
            return clazz.getConstructor(String.class).newInstance(value);
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la deserializzazione del tipo personalizzato " + field.getName(), e);
        }
    }

    private boolean isCollection(Field field) {
        return List.class.isAssignableFrom(field.getType()) || Set.class.isAssignableFrom(field.getType());
    }

    private void populateCollection(Field field, String value) throws IllegalAccessException {
        String[] values = value.split(",");
        Collection<Object> collection;

        if (List.class.isAssignableFrom(field.getType())) {
            collection = new ArrayList<>();
        } else if (Set.class.isAssignableFrom(field.getType())) {
            collection = new HashSet<>();
        } else {
            return; // Non gestito
        }

        ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
        Class<?> elementType = (Class<?>) parameterizedType.getActualTypeArguments()[0];

        for (String item : values) {
            item = item.trim();
            if (elementType == String.class) {
                collection.add(item);
            } else if (elementType == Integer.class) {
                collection.add(Integer.parseInt(item));
            } else if (elementType == Long.class) {
                collection.add(Long.parseLong(item));
            }
        }

        field.set(null, collection);
    }

    private void populateArray(Field field, String value) throws IllegalAccessException {
        String[] values = value.split(",");
        Class<?> componentType = field.getType().getComponentType();
        Object array = Array.newInstance(componentType, values.length);

        for (int i = 0; i < values.length; i++) {
            String item = values[i].trim();
            if (componentType == String.class) {
                Array.set(array, i, item);
            } else if (componentType == Integer.class) {
                Array.set(array, i, Integer.parseInt(item));
            } else if (componentType == Long.class) {
                Array.set(array, i, Long.parseLong(item));
            }
            // Aggiungere altri tipi se necessario
        }

        field.set(null, array);
    }

    private void createConfigFile() throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(CONFIG_FILE))) {
            Field[] fields = configClass.getDeclaredFields();

            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    String exampleValue = getExampleValue(field);
                    writer.println(field.getName() + "=" + exampleValue);
                }
            }
        }
    }

    private String getExampleValue(Field field) {
        if (field.getType() == Boolean.class) {
            return "false";
        } else if (field.getType() == LocalDate.class) {
            return "2024-01-01";
        } else if (isCollection(field)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Class<?> elementType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            if (elementType == String.class) {
                return "val1,val2,val3"; // Esempio per List<String>
            } else if (elementType == Integer.class) {
                return "10,42,53"; // Esempio per List<Integer>
            } else if (elementType == Long.class) {
                return "1000,2000,3000"; // Esempio per List<Long>
            }
        } else if (field.getType().isArray()) {
            Class<?> componentType = field.getType().getComponentType();
            if (componentType == String.class) {
                return "val1,val2,val3"; // Esempio per String[]
            } else if (componentType == Integer.class) {
                return "1,2,3"; // Esempio per Integer[]
            } else if (componentType == Long.class) {
                return "1000,2000,3000"; // Esempio per Long[]
            }
        }
        return ""; // Valore di default se il tipo non è supportato
    }
}