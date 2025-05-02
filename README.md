# Progetti Dispositivi Mobili | Fumetti: traccia 8
## Introduzione
L'applicazione è un'applicazione sviluppata per il corso di Dispositivi Mobili, A.A. 2024/2025.
L'obiettivo ultimo dell'applicazione è quello di gestire una collezione di fumetti, aiutando l'utente a tracciare gli acquisti dell'utente.
### Scelte di sviluppo
Questa applicazione è stata sviluppata utilizzando Kotlin come linguaggio di programmazione, mentre per quanto riguarda la GUI sono stati utilizzati XML.
Il progetto è stato strutturato in modo da garantire modularità e manutenibilità.
## Installazione
Per poter installare l'applicazione è necessario l'utilizzo di un dispositivo Android.
### Passaggi:
1. Clonare il repository: 'git clone https://github.com/Sindoni750760/Fumetti/'
2. Aprire il progetto in Android Studio
3. Mediante un emulatore o dispositivo, eseguire l'applicativo
## Funzionalità principali
- Login e registrazione utenti con Firebase Authentication
- Archiviazione dei fumetti personali tramite Firestore
- Visualizzazione dei fumetti in base a:
  - Serie
  - Nome
  - Numero
- Ricerca avanzata per nome, serie o numero
- Stato dei fumetti rappresentato con semaforo (verde, giallo, rosso)
- Sistema di prenotazione, restituzione e lista d'attesa
- Backup automatico dei dati su cloud, associati all'utente
- Mancolist: lista dei fumetti mancanti
- Interfaccia ispirata a Steam
## Architettura

- **MVVM** (Model-View-ViewModel)
- **Firebase** per database (Firestore) e autenticazione
- **RecyclerView** con `ComicsAdapter` per gestire dinamicamente le liste

## Requisiti

- Android Studio Electric Eel o superiore
- Connessione a Internet
- Un account Firebase configurato con il file `google-services.json`

## Autore

Progetto realizzato da **Mattia Sindoni**  
Matricola: **750760**  
Corso di **Dispositivi Mobili** – A.A. 2024/2025
