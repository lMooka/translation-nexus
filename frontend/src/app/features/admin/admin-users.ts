import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { UserDTO } from '../../core/models/translation.model';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.css'
})
export class AdminUsersComponent implements OnInit {

  users: UserDTO[] = [];
  loading = false;
  saving = false;

  // Modals state
  showModal = false;
  isEditMode = false;
  selectedUserId: string | null = null;

  // Form Fields
  username = '';
  password = '';
  selectedRoles: Record<string, boolean> = {};
  selectedLocales: Record<string, boolean> = {};

  readonly LOCALES = ['pt', 'es', 'fr', 'de', 'ja'];
  readonly ROLES = ['TRANSLATOR', 'REVIEWER', 'MANAGER', 'ADMIN'];

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    setTimeout(() => this.loadUsers());
  }

  loadUsers() {
    this.loading = true;
    this.api.getUsers().subscribe({
      next: res => {
        this.users = res || [];
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  openCreateModal() {
    this.isEditMode = false;
    this.selectedUserId = null;
    this.username = '';
    this.password = '';
    this.selectedRoles = {};
    this.ROLES.forEach(r => this.selectedRoles[r] = r === 'TRANSLATOR');
    this.selectedLocales = {};
    this.LOCALES.forEach(l => this.selectedLocales[l] = false);
    this.showModal = true;
  }

  openEditModal(user: UserDTO) {
    this.isEditMode = true;
    this.selectedUserId = user.id;
    this.username = user.username;
    this.password = '';
    this.selectedRoles = {};
    this.ROLES.forEach(r => {
      this.selectedRoles[r] = user.roles?.includes(r) || false;
    });
    this.selectedLocales = {};
    this.LOCALES.forEach(l => {
      this.selectedLocales[l] = user.allowedLocales?.includes(l) || false;
    });
    this.showModal = true;
  }

  getLocalesList(): string[] {
    return Object.keys(this.selectedLocales).filter(l => this.selectedLocales[l]);
  }

  getRolesList(): string[] {
    return Object.keys(this.selectedRoles).filter(r => this.selectedRoles[r]);
  }

  isTranslatorSelected(): boolean {
    return this.selectedRoles['TRANSLATOR'] || false;
  }

  onRoleChange(role: string) {
    if (role === 'TRANSLATOR' && !this.selectedRoles['TRANSLATOR']) {
      this.LOCALES.forEach(l => this.selectedLocales[l] = false);
    }
  }

  saveUser() {
    if (!this.username) return;
    const rolesList = this.getRolesList();
    if (rolesList.length === 0) {
      alert('Please select at least one role.');
      return;
    }
    this.saving = true;

    if (this.isEditMode && this.selectedUserId) {
      this.api.updateUser(this.selectedUserId, {
        roles: rolesList,
        allowedLocales: this.isTranslatorSelected() ? this.getLocalesList() : [],
        password: this.password || undefined
      }).subscribe({
        next: () => {
          this.saving = false;
          this.showModal = false;
          this.loadUsers();
        },
        error: err => {
          alert(err.error ?? 'Error updating user');
          this.saving = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      if (!this.password) {
        alert('Password is required for new users.');
        this.saving = false;
        return;
      }
      this.api.createUser({
        username: this.username,
        password: this.password,
        roles: rolesList,
        allowedLocales: this.isTranslatorSelected() ? this.getLocalesList() : []
      }).subscribe({
        next: () => {
          this.saving = false;
          this.showModal = false;
          this.loadUsers();
        },
        error: err => {
          alert(err.error ?? 'Error creating user');
          this.saving = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  deleteUser(user: UserDTO) {
    if (!confirm(`Are you sure you want to delete user ${user.username}?`)) return;

    this.api.deleteUser(user.id).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: err => {
        alert(err.error ?? 'Error deleting user');
      }
    });
  }
}
