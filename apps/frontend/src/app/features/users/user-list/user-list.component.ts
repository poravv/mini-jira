import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { User, UserRole } from '../user.model';
import { UserService } from '../user.service';
import { UserSessionService } from '../user-session.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-user-list',
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './user-list.component.html',
  styleUrl: './user-list.component.css'
})
export class UserListComponent implements OnInit {
  private readonly userService = inject(UserService);
  private readonly router = inject(Router);
  private readonly session = inject(UserSessionService);

  users: User[] = [];
  isLoading = true;
  updatingUserId: number | null = null;
  errorMessage = '';
  activeFilter: 'all' | 'active' | 'inactive' = 'all';

  ngOnInit(): void {
    if (!this.session.currentUser()) {
      this.router.navigate(['/users/account']);
      return;
    }
    this.loadUsers();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.errorMessage = '';
    const isActive = this.activeFilter === 'all' ? undefined : this.activeFilter === 'active';
    this.userService.getAll(isActive).subscribe({
      next: (users) => {
        this.users = users;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los usuarios.';
        this.isLoading = false;
      }
    });
  }

  changeStatus(user: User): void {
    const action = user.isActive ? 'desactivar' : 'activar';
    if (!window.confirm(`¿Seguro que querés ${action} a "${user.username}"?`)) {
      return;
    }

    this.updatingUserId = user.id;
    this.errorMessage = '';
    this.userService.updateStatus(user.id, !user.isActive).subscribe({
      next: (updatedUser) => {
        if (this.activeFilter === 'all') {
          this.users = this.users.map((currentUser) => currentUser.id === user.id ? updatedUser : currentUser);
        } else {
          this.users = this.users.filter((currentUser) => currentUser.id !== user.id);
        }
        this.updatingUserId = null;
      },
      error: () => {
        this.errorMessage = 'No se pudo actualizar el estado del usuario.';
        this.updatingUserId = null;
      }
    });
  }

  roleLabel(role: UserRole): string {
    return { ADMIN: 'Administrador', SUPPORT: 'Soporte', USER: 'Usuario' }[role];
  }
}
