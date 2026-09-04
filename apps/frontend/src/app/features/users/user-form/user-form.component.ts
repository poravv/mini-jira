import { Component, OnInit, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { USER_ROLES, UserCreateInput, UserRole, UserUpdateInput } from '../user.model';
import { UserService } from '../user.service';
import { UserSessionService } from '../user-session.service';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-user-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.css'
})
export class UserFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly session = inject(UserSessionService);

  readonly roles = USER_ROLES;
  errorMessage = '';
  isEditing = false;
  isLoading = false;
  isSaving = false;
  private userId: number | null = null;
  isAccountCreation = false;

  readonly form = this.fb.group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(254)]],
    password: ['', [Validators.minLength(8), Validators.maxLength(72)]],
    firstName: ['', [Validators.required, Validators.maxLength(100)]],
    lastName: ['', [Validators.required, Validators.maxLength(100)]],
    role: this.fb.control<UserRole>('USER')
  });

  ngOnInit(): void {
    this.isAccountCreation = this.route.snapshot.data['isAccountCreation'] === true;
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam === null) {
      if (!this.isAccountCreation && !this.session.currentUser()) {
        this.router.navigate(['/users/account']);
        return;
      }
      this.form.controls.password.addValidators(Validators.required);
      if (this.isAccountCreation) {
        this.form.controls.role.setValue('USER');
      }
      return;
    }

    const id = Number(idParam);
    if (!Number.isInteger(id) || id <= 0) {
      this.errorMessage = 'El usuario solicitado no es válido.';
      return;
    }

    if (!this.session.currentUser()) {
      this.router.navigate(['/users/account']);
      return;
    }

    this.isEditing = true;
    this.userId = id;
    this.isLoading = true;
    this.userService.getById(id).subscribe({
      next: (user) => {
        this.form.patchValue({
          username: user.username,
          email: user.email,
          firstName: user.firstName,
          lastName: user.lastName,
          role: user.role
        });
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No se pudo cargar el usuario.';
        this.isLoading = false;
      }
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSaving = true;
    this.errorMessage = '';
    const value = this.form.getRawValue();
    if (this.isEditing && this.userId !== null) {
      const input: UserUpdateInput = { ...value, password: value.password || undefined };
      this.userService.update(this.userId, input).subscribe(this.saveObserver());
    } else {
      const input: UserCreateInput = value;
      if (this.isAccountCreation) {
        this.userService.create(input).pipe(
          switchMap(() => this.userService.login({ identifier: input.username, password: input.password }))
        ).subscribe({
          next: (auth) => {
            this.session.startSession(auth);
            this.router.navigate(['/issues']);
          },
          error: () => this.handleSaveError()
        });
      } else {
        this.userService.create(input).subscribe({
          next: () => this.router.navigate(['/users']),
          error: () => this.handleSaveError()
        });
      }
    }
  }

  private saveObserver() {
    return {
      next: () => this.router.navigate(['/users']),
      error: () => this.handleSaveError()
    };
  }

  private handleSaveError(): void {
    this.errorMessage = 'No se pudo guardar el usuario. Verificá que el nombre y email no estén usados.';
    this.isSaving = false;
  }
}
