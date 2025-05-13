# Application Android - Gestionnaire de tâches (To-Do List)

Cette application Android permet à un utilisateur de gérer ses tâches quotidiennes, consulter un calendrier, suivre ses statistiques, et afficher la météo en temps réel.  
Elle repose sur une architecture moderne (MVVM) avec Room, Retrofit et LiveData.

---

## Fonctionnalités principales

- **Authentification utilisateur** (inscription / connexion)
- **Ajout, édition, suppression de tâches**
- **Gestion des sous-tâches** liées à chaque tâche principale
- **Vue calendrier** avec filtrage des tâches par jour
- **Affichage de la météo** (via API OpenWeatherMap)
- **Profil utilisateur** avec statistiques et modification du mot de passe
- **Déconnexion et session persistante** via SharedPreferences
- **Architecture MVVM** (ViewModel + LiveData)
- **Interface soignée** conforme aux directives Material Design

---

## Technologies utilisées

- **Java**
- **Room** (base de données locale)
- **Retrofit** (client HTTP pour API REST)
- **LiveData & ViewModel**
- **RecyclerView**
- **CalendarView (Kizitonwose)**
- **SharedPreferences**

---

## Lancer l'application

1. Cloner le dépôt ou importer le projet dans Android Studio
2. Ajouter une clé API OpenWeatherMap dans `MainActivity`
3. Lancer sur un appareil réel ou un émulateur avec Internet

---

## Clé API météo

Pour l’affichage de la météo, créez un compte gratuit sur  
https://openweathermap.org/api  
Puis ajoutez la clé dans le code à l’endroit prévu (`MainActivity.java`).





