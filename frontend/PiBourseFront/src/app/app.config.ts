import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { routes } from './app.routes';
import { authInterceptor } from './services/player/auth.interceptor';

// Import des modules nécessaires pour le composant de graphique
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    importProvidersFrom([
      // Modules nécessaires pour le composant de graphique
      BrowserAnimationsModule,
      MatButtonModule,
      MatSelectModule,
      MatInputModule,
      MatTabsModule,
      MatTooltipModule,
      MatIconModule
    ])
  ]
};
