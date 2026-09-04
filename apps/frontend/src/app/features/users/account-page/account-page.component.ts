import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { UserService } from '../user.service';
import { UserSessionService } from '../user-session.service';

@Component({
  selector: 'app-account-page',
  imports: [FormsModule, RouterLink],
  templateUrl: './account-page.component.html',
  styleUrl: './account-page.component.css'
})
export class AccountPageComponent {
  private readonly router = inject(Router);
  private readonly userService = inject(UserService);
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
    this.userService.login({ identifier: this.identifier.trim(), password: this.password }).subscribe({
      next: (auth) => {
        this.session.startSession(auth);
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
