import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app';
import { KeycloakService } from './app/services/keycloak.service';

async function initializeApp() {
  try {
    console.log('🔐 Initializing Keycloak...');

    // Create a temporary instance to initialize Keycloak
    const keycloakService = new KeycloakService();
    const authenticated = await keycloakService.init();

    console.log('✅ Keycloak initialized successfully');
    console.log('🔑 Authentication status:', authenticated);

    // Bootstrap the Angular application
    const app = await bootstrapApplication(AppComponent, appConfig);
    console.log('🚀 Angular application bootstrapped successfully');

    return app;
  } catch (error) {
    console.error('❌ Failed to initialize application:', error);
    throw error;
  }
}

// Initialize and start the application
initializeApp().catch((err) => {
  console.error('💥 Application startup failed:', err);
});
