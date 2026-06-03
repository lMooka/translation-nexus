import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { reviewerGuard } from './core/guards/reviewer.guard';
import { adminGuard } from './core/guards/admin.guard';
import { managerGuard } from './core/guards/manager.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'translations', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login').then(m => m.LoginComponent)
  },
  {
    path: 'translations',
    canActivate: [authGuard],
    loadComponent: () => import('./features/translations/translation-list').then(m => m.TranslationListComponent)
  },
  {
    path: 'review',
    canActivate: [authGuard, reviewerGuard],
    loadComponent: () => import('./features/review/review-queue').then(m => m.ReviewQueueComponent)
  },
  {
    path: 'categories',
    canActivate: [authGuard, managerGuard],
    loadComponent: () => import('./features/admin/admin-categories').then(m => m.AdminCategoriesComponent)
  },
  {
    path: 'languages',
    canActivate: [authGuard, managerGuard],
    loadComponent: () => import('./features/admin/admin-locales').then(m => m.AdminLocalesComponent)
  },
  {
    path: 'admin',
    canActivate: [authGuard, adminGuard],
    loadComponent: () => import('./features/admin/admin-users').then(m => m.AdminUsersComponent)
  },
  { path: '**', redirectTo: 'translations' }
];
