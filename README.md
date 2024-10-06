# MkConfig

MkConfig è una libreria per la gestione della configurazione in progetti Java. Semplifica il caricamento e la gestione dei valori di configurazione tramite un file `.properties`, consentendo agli sviluppatori di concentrarsi sulla logica della loro applicazione.

## Installazione

### Con Maven

Per importare la libreria nel tuo progetto Maven, aggiungi la seguente dipendenza nel file `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.MicheleIlPro</groupId>
    <artifactId>mkconfig</artifactId>
    <version>1.0.1</version>
</dependency>
```

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.MicheleIlPro:mkconfig:1.0.1'
}
```

## Utilizzo

Creare la classe di configurazione: L'utente deve creare una classe di configurazione estendendo `MkConfig` con il parametro della propria classe.

```java
public class NomeClasseConfig extends MkConfig<NomeClasseConfig> {
    public NomeClasseConfig() {
        super(NomeClasseConfig.class); // Passa la classe corrente
    }
    public static String connectionKey;
    public static Long ownerId;
    public static Boolean isEnabled;
}
```

Inizializzare la configurazione: Nel metodo `main`, istanziare la classe di configurazione per far sì che il file di configurazione venga creato o letto.



```java
public class Main {
    public static void main(String[] args) {
        new NomeClasseConfig(); // Inizializzerà MkConfig e caricherà i valori
        System.out.println("Connection Key: " + Config.connectionKey);
        System.out.println("Owner ID: " + Config.ownerId);
        System.out.println("Is Enabled: " + Config.isEnabled);
    }
}
```

File di configurazione: Al primo avvio, ti creerà un file di configurazione con i vari campi da compilare e riempire chiamato mkconfig.properties nella tua directory di progetto. Inserisci i valori per le variabili dichiarate nella tua classe di configurazione in questo file. Il formato sarà simile a questo:
```mmakefile
connectionKey=
ownerId=123456789
isEnabled=true
```

Un esempio dei vari tipi gestiti nel file di configurazione sono:
```mmakefile
stringa=valore
intero=10
long=10000
localdate=2024-10-21
booleano=true
listastring=val1,val2,val3
listainteri=2,5,7
listalong=1000,2000,3000
setstring=val1,val2,val3
setinteri=2,5,7
setlong=1000,2000,3000
arraystring=val1,val2,val3
arrayinteri=2,5,7
arraylong=1000,2000,3000
```

## Tipo custom

Se hai un oggetto personalizzato, dovrai creare il costruttore che prenda una stringa, divida la stringa passata come parametro in un array di String dividendo i vari valori tra le virgole:`,` poi popola l'oggetto con i vari valori.
Ecco un esempio:
```java
public class Person {
    private String nome;
    private String cognome;

    public Person(String data) {
        String[] datas = data.split(",");
        nome = datas[0];
        cognome = datas[1];
    }
    
    // getter e setter
}
```
Mentre, nel file di configurazione, avremo:
```mmakefile
oggettocustom=Mario,Rossi
```

## Contribuire

Se desideri contribuire al progetto, sentiti libero di fare una fork e inviare una pull request con le tue modifiche.


## Licenza

Questo progetto è concesso in licenza sotto la MIT License.