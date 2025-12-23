// Import nécessaire pour la compilation JIT
import '@angular/compiler';

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

// Active le mode développement
import { enableProdMode } from '@angular/core';

// Désactive le mode production pour le développement
// (à supprimer en production)
// enableProdMode();

bootstrapApplication(AppComponent, appConfig)
  .catch((err) => console.error(err));
