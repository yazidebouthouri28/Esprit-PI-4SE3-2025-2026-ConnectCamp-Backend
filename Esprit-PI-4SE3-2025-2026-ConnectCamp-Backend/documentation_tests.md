# Guide des Tests Unitaires : Méthodologie et Validation

Ce document explique la stratégie de test mise en place pour le projet **CampConnect**, son utilité technique et comment présenter ces résultats à votre professeur.

---

## 1. Qu'est-ce qu'un Test Unitaire ?

Un **test unitaire** sert à vérifier le bon fonctionnement d'une "unité" précise de code (généralement une méthode de service) de manière isolée.

### Pourquoi est-ce important ?
- **Fiabilité** : On s'assure que la logique métier (ex: calcul de prix, droits d'accès) est correcte.
- **Non-régression** : Si vous modifiez le code plus tard, les tests vous diront immédiatement si vous avez cassé quelque chose.
- **Isolation** : On n'utilise pas la vraie base de données. On utilise des **Mocks** (faux objets) pour simuler les réponses des dépendances.

---

## 2. Architecture des Tests (Ce que j'ai fait)

### Backend (Spring Boot)
- **Outils** : JUnit 5 & Mockito.
- **Méthode** : Utilisation de `@Mock` pour simuler les Repositories et `@InjectMocks` pour tester le Service.
- **Fichiers clés** :
  - `EmergencyAlertServiceTest` : Vérifie que seul un `CAMPER` peut lancer un SOS.
  - `PackServiceTest` : Vérifie que le calcul de la remise (discount) est mathématiquement correct.

### Frontend (Angular)
- **Outils** : Jasmine (framework de test) & Karma (lanceur de tests).
- **Méthode** : Utilisation de `HttpClientTestingModule` pour intercepter les appels API sans envoyer de vraies requêtes au serveur.
- **Fichiers clés** :
  - `backend-alert.service.spec.ts` : Teste la structure des requêtes HTTP.
  - `alerte-create.component.spec.ts` : Vérifie la validité du formulaire et l'intégration de la carte Leaflet.

---

## 3. Comment les Valider avec le Professeur ?

Voici les étapes à suivre pour faire une démonstration réussie :

### Étape 1 : Démonstration Backend (IntelliJ / Eclipse)
1. Ouvrez la classe `PackServiceTest.java`.
2. Faites un clic droit sur le nom de la classe -> **Run 'PackServiceTest'**.
3. **À expliquer au prof** : "J'utilise Mockito pour simuler la base de données. Ici, je teste que si un pack coûte plus cher que la somme de ses services, le système lève une `BusinessException`."

### Étape 2 : Démonstration Frontend (Terminal)
1. Ouvrez un terminal dans le dossier `frontConnect/frontConnect`.
2. Tapez la commande : `ng test` (ou `npx ng test --watch=false`).
3. Une fenêtre de navigateur s'ouvrira montrant les cercles verts.
4. **À expliquer au prof** : "Les tests Jasmine vérifient que mes composants réagissent correctement. Par exemple, le formulaire SOS reste bloqué tant que le titre n'a pas au moins 5 caractères."

---

## 4. Glossaire pour la soutenance
- **Mock** : Un objet simulé qui remplace une dépendance réelle (ex: simuler un utilisateur connecté).
- **Assertion (`assertEquals`, `expect`)** : La vérification que le résultat obtenu est égal au résultat attendu.
- **Injection de dépendance** : Le fait de fournir au service les outils dont il a besoin (mocker son repository).

---

> [!TIP]
> Si le prof demande : "Est-ce que vous testez la base de données ?", répondez :
> "Non, c'est du **Test Unitaire**. Pour la base de données, on ferait des **Tests d'Intégration**. Ici, on valide uniquement la logique pure de notre code Java et TypeScript."
