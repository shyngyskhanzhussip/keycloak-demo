import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { map, take } from 'rxjs/operators';
import { KeycloakService } from '../services/keycloak.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(
    private keycloakService: KeycloakService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    const requiredRoles = route.data['roles'] as string[];
    const requiredPermission = route.data['permission'] as string;

    return this.keycloakService.isAuthenticated$.pipe(
      take(1),
      map(isAuthenticated => {
        if (!isAuthenticated) {
          this.keycloakService.login();
          return false;
        }

        // Check specific roles
        if (requiredRoles && requiredRoles.length > 0) {
          const hasRole = this.keycloakService.hasAnyRole(requiredRoles);
          if (!hasRole) {
            console.log('Access denied: Required roles not found', requiredRoles);
            this.router.navigate(['/access-denied']);
            return false;
          }
        }

        // Check specific permissions
        if (requiredPermission) {
          const hasPermission = this.checkPermission(requiredPermission);
          if (!hasPermission) {
            console.log('Access denied: Required permission not found', requiredPermission);
            this.router.navigate(['/access-denied']);
            return false;
          }
        }

        return true;
      })
    );
  }

  private checkPermission(permission: string): boolean {
    switch (permission) {
      case 'manage_products':
        return this.keycloakService.canManageProducts();
      case 'manage_orders':
        return this.keycloakService.canManageOrders();
      case 'delete_items':
        return this.keycloakService.canDeleteItems();
      case 'admin_access':
        return this.keycloakService.isAdmin();
      case 'manager_access':
        return this.keycloakService.isManager() || this.keycloakService.isAdmin();
      case 'employee_access':
        return this.keycloakService.isEmployee() || this.keycloakService.isManager() || this.keycloakService.isAdmin();
      default:
        return false;
    }
  }
}
