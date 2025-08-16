import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import { KeycloakService } from '../services/keycloak.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private keycloakService: KeycloakService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Skip interceptor for Keycloak endpoints
    if (req.url.includes('/realms/') || req.url.includes('/auth/')) {
      return next.handle(req);
    }

    // Add authorization header if user is authenticated
    if (this.keycloakService.isLoggedIn()) {
      const token = this.keycloakService.getToken();
      if (token) {
        req = this.addTokenToRequest(req, token);
      }
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Token might be expired, try to refresh
          return this.handle401Error(req, next);
        }
        return throwError(() => error);
      })
    );
  }

  private addTokenToRequest(req: HttpRequest<any>, token: string): HttpRequest<any> {
    return req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  private handle401Error(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (this.keycloakService.isTokenExpired()) {
      // Try to refresh the token
      return new Observable(observer => {
        this.keycloakService.refreshToken().then(refreshed => {
          if (refreshed) {
            // Token refreshed successfully, retry the request
            const token = this.keycloakService.getToken();
            if (token) {
              const newReq = this.addTokenToRequest(req, token);
              next.handle(newReq).subscribe({
                next: event => observer.next(event),
                error: err => observer.error(err),
                complete: () => observer.complete()
              });
            } else {
              observer.error(new Error('Token refresh failed'));
            }
          } else {
            // Token refresh failed, redirect to login
            this.keycloakService.login();
            observer.error(new Error('Authentication required'));
          }
        }).catch(error => {
          console.error('Token refresh error:', error);
          this.keycloakService.login();
          observer.error(error);
        });
      });
    } else {
      // Token is not expired but 401 received, probably invalid token
      this.keycloakService.logout();
      return throwError(() => new Error('Authentication required'));
    }
  }
}
