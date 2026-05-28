# Frontend

Application frontend Angular du projet.

## Prerequis

- Node.js
- npm (le projet utilise `npm@11.7.0`)

## Installation

```bash
npm install
```

## Lancer en local

```bash
npm start
```

Le frontend est accessible sur `http://localhost:4200/`.

## Scripts utiles

- `npm start` : demarre le serveur de developpement Angular.
- `npm run build` : build de production.
- `npm run watch` : build en mode watch avec la configuration `development`.
- `npm test` : lance les tests (`ng test` / Vitest selon la configuration Angular).
- `npm run cypress:open` : ouvre Cypress.
- `npm run generate:api` : regenere le client TypeScript Angular depuis l'OpenAPI du backend.

## Regenerer le client API

Le script `generate:api` utilise :

- URL OpenAPI: `http://localhost:8080/v3/api-docs`
- Dossier de sortie: `src/app/api`

Assure-toi que le backend tourne sur le port `8080` avant d'executer :

```bash
npm run generate:api
```
