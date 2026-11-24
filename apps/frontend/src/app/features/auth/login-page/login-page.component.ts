import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../auth.service';
import { UserSessionService } from '../user-session.service';

@Component({
  selector: 'app-login-page',
  imports: [FormsModule, RouterLink],
  templateUrl: './login-page.component.html',
  styleUrl: './login-page.component.css'
})
export class LoginPageComponent {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  readonly session = inject(UserSessionService);

  identifier = '';
  password = '';
  isLoggingIn = false;
  errorMessage = '';

  login(): void {
    if (!this.identifier.trim() || !this.password) {
      this.errorMessage = 'Ingresá tu correo o usuario y tu contraseña.';
      return;
    }
    this.isLoggingIn = true;
    this.errorMessage = '';
    this.authService.login({ identifier: this.identifier.trim(), password: this.password }).subscribe({
      next: (login) => {
        this.session.startSession(login);
        this.router.navigate(['/issues']);
      },
      error: () => {
        this.errorMessage = 'El correo/usuario o la contraseña no son correctos, o la cuenta está suspendida.';
        this.isLoggingIn = false;
      }
    });
  }

  logout(): void {
    this.session.endSession();
  }
}
