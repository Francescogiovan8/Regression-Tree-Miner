# Regression Tree Miner

Progetto sviluppato per il corso di **Metodi Avanzati di Programmazione (MAP)** — Università degli Studi di Bari Aldo Moro.

Il progetto implementa un sistema client-server per la costruzione e l'utilizzo di **alberi di regressione**, con supporto per dataset numerici, salvataggio e caricamento degli alberi e predizione remota.

Il progetto è stato successivamente esteso con un **client Telegram**, che permette di interagire con il server tramite un bot.

---

## 📌 Caratteristiche

### Progetto base

Il sistema principale è costituito da un'architettura **client-server**:

- caricamento di dataset di training;
- costruzione di alberi di regressione;
- supporto per attributi discreti e continui;
- predizione di valori numerici;
- salvataggio degli alberi;
- caricamento di alberi precedentemente salvati;
- comunicazione client-server tramite socket TCP;
- persistenza dei dati tramite database MySQL.

### Estensione Telegram

Il progetto include inoltre un client basato su un **bot Telegram**, sviluppato utilizzando la libreria Java open source **TelegramBots**.

Il bot permette di:

- avviare una sessione di lavoro;
- caricare dataset;
- apprendere un albero di regressione;
- caricare alberi salvati;
- effettuare predizioni interattive;
- ripetere le predizioni;
- terminare la sessione;
- ricevere informazioni e aiuto tramite comandi Telegram.


