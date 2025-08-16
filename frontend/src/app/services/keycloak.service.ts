import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { BehaviorSubject, Observable } from 'rxjs';

export interface UserProfile {
  id?: string;
  username?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  roles: string[];
  groups: string[];
}

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {
  private keycloak: Keycloak;
  private userProfileSubject = new BehaviorSubject<UserProfile | null>(null);
  private authenticationStatusSubject = new BehaviorSubject<boolean>(false);

  public userProfile$ = this.userProfileSubject.asObservable();
  public isAuthenticated$ = this.authenticationStatusSubject.asObservable();

  constructor() {
    this.keycloak = new Keycloak({
      url: 'http://localhost:8180',
      realm: 'ecommerce-demo',
      clientId: 'ecommerce-frontend'
    });
  }

  async init(): Promise<boolean> {
    try {
      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
        checkLoginIframe: false,
        pkceMethod: 'S256'
      });

      this.authenticationStatusSubject.next(authenticated);

      if (authenticated) {
        await this.loadUserProfile();
        this.setupTokenRefresh();
      }

      // Listen for token expiration
      this.keycloak.onTokenExpired = () => {
        this.refreshToken();
      };

      // Listen for authentication state changes
      this.keycloak.onAuthSuccess = () => {
        this.authenticationStatusSubject.next(true);
        this.loadUserProfile();
      };

      this.keycloak.onAuthLogout = () => {
        this.authenticationStatusSubject.next(false);
        this.userProfileSubject.next(null);
      };

      return authenticated;
    } catch (error) {
      console.error('Failed to initialize Keycloak:', error);
      return false;
    }
  }

  login(): Promise<void> {
    return this.keycloak.login({
      redirectUri: window.location.origin
    });
  }

  logout(): Promise<void> {
    this.userProfileSubject.next(null);
    this.authenticationStatusSubject.next(false);
    return this.keycloak.logout({
      redirectUri: window.location.origin
    });
  }

  register(): Promise<void> {
    return this.keycloak.register({
      redirectUri: window.location.origin
    });
  }

  getToken(): string | undefined {
    return this.keycloak.token;
  }

  isLoggedIn(): boolean {
    return !!this.keycloak.token && !this.keycloak.isTokenExpired();
  }

  isTokenExpired(): boolean {
    return this.keycloak.isTokenExpired();
  }

  async refreshToken(): Promise<boolean> {
    try {
      const refreshed = await this.keycloak.updateToken(30);
      if (refreshed) {
        console.log('Token refreshed');
      }
      return refreshed;
    } catch (error) {
      console.error('Failed to refresh token:', error);
      await this.logout();
      return false;
    }
  }

  async loadUserProfile(): Promise<void> {
    try {
      const profile = await this.keycloak.loadUserProfile();
      const tokenParsed = this.keycloak.tokenParsed;

      const userProfile: UserProfile = {
        id: profile.id,
        username: profile.username,
        email: profile.email,
        firstName: profile.firstName,
        lastName: profile.lastName,
        roles: this.extractRoles(tokenParsed),
        groups: this.extractGroups(tokenParsed)
      };

      this.userProfileSubject.next(userProfile);
    } catch (error) {
      console.error('Failed to load user profile:', error);
    }
  }

  private extractRoles(tokenParsed: any): string[] {
    const roles: string[] = [];

    // Extract realm roles
    if (tokenParsed?.realm_access?.roles) {
      roles.push(...tokenParsed.realm_access.roles);
    }

    // Extract client roles
    if (tokenParsed?.resource_access?.['ecommerce-frontend']?.roles) {
      roles.push(...tokenParsed.resource_access['ecommerce-frontend'].roles);
    }

    return roles.filter((role, index, self) => self.indexOf(role) === index);
  }

  private extractGroups(tokenParsed: any): string[] {
    return tokenParsed?.groups || [];
  }

  hasRole(role: string): boolean {
    const currentProfile = this.userProfileSubject.value;
    return currentProfile?.roles.includes(role) || false;
  }

  hasAnyRole(roles: string[]): boolean {
    const currentProfile = this.userProfileSubject.value;
    if (!currentProfile) return false;
    return roles.some(role => currentProfile.roles.includes(role));
  }

  hasGroup(group: string): boolean {
    const currentProfile = this.userProfileSubject.value;
    return currentProfile?.groups.includes(group) || false;
  }

  isAdmin(): boolean {
    return this.hasRole('admin');
  }

  isManager(): boolean {
    return this.hasRole('manager');
  }

  isEmployee(): boolean {
    return this.hasRole('employee');
  }

  isCustomer(): boolean {
    return this.hasRole('customer');
  }

  canManageProducts(): boolean {
    return this.hasAnyRole(['admin', 'manager']);
  }

  canManageOrders(): boolean {
    return this.hasAnyRole(['admin', 'manager', 'employee']);
  }

  canDeleteItems(): boolean {
    return this.hasRole('admin');
  }

  getCurrentUser(): UserProfile | null {
    return this.userProfileSubject.value;
  }

  getUserFullName(): string {
    const user = this.getCurrentUser();
    if (user?.firstName && user?.lastName) {
      return `${user.firstName} ${user.lastName}`;
    }
    return user?.username || 'Unknown User';
  }

  private setupTokenRefresh(): void {
    // Auto refresh token every 30 seconds if it expires within 60 seconds
    setInterval(() => {
      if (this.keycloak.token && this.keycloak.isTokenExpired(60)) {
        this.refreshToken();
      }
    }, 30000);
  }
}
