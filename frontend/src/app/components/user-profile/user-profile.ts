import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { KeycloakService, UserProfile } from '../../services/keycloak.service';
import { Subject, takeUntil } from 'rxjs';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatMenuModule,
    MatDividerModule
  ],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.scss'
})
export class UserProfileComponent implements OnInit, OnDestroy {
  userProfile: UserProfile | null = null;
  isAuthenticated = false;
  private destroy$ = new Subject<void>();

  constructor(private keycloakService: KeycloakService) {}

  ngOnInit(): void {
    this.keycloakService.userProfile$
      .pipe(takeUntil(this.destroy$))
      .subscribe(profile => {
        this.userProfile = profile;
      });

    this.keycloakService.isAuthenticated$
      .pipe(takeUntil(this.destroy$))
      .subscribe(authenticated => {
        this.isAuthenticated = authenticated;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  login(): void {
    this.keycloakService.login();
  }

  logout(): void {
    this.keycloakService.logout();
  }

  getUserDisplayName(): string {
    return this.keycloakService.getUserFullName();
  }

  getUserInitials(): string {
    if (this.userProfile?.firstName && this.userProfile?.lastName) {
      return `${this.userProfile.firstName.charAt(0)}${this.userProfile.lastName.charAt(0)}`.toUpperCase();
    }
    if (this.userProfile?.username) {
      return this.userProfile.username.charAt(0).toUpperCase();
    }
    return 'U';
  }

  getRoleDisplayName(): string {
    if (!this.userProfile?.roles) return 'User';

    if (this.userProfile.roles.includes('admin')) return 'Administrator';
    if (this.userProfile.roles.includes('manager')) return 'Manager';
    if (this.userProfile.roles.includes('employee')) return 'Employee';
    if (this.userProfile.roles.includes('customer')) return 'Customer';

    return 'User';
  }

  getRoleIcon(): string {
    if (!this.userProfile?.roles) return 'person';

    if (this.userProfile.roles.includes('admin')) return 'admin_panel_settings';
    if (this.userProfile.roles.includes('manager')) return 'supervisor_account';
    if (this.userProfile.roles.includes('employee')) return 'badge';
    if (this.userProfile.roles.includes('customer')) return 'person';

    return 'person';
  }
}
