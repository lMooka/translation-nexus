import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { Locale } from '../../core/models/translation.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-admin-locales',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-locales.html',
  styleUrl: './admin-locales.css'
})
export class AdminLocalesComponent implements OnInit {

  locales: Locale[] = [];
  loading = false;
  saving = false;

  // Modal State
  showModal = false;
  isEditMode = false;
  selectedLocaleId = '';

  // Form Fields
  localeId = '';
  name = '';
  googleCode = '';
  sortOrder = 0;

  // Drag State
  draggedIndex: number | null = null;

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    setTimeout(() => this.loadLocales());
  }

  loadLocales() {
    this.loading = true;
    this.api.getLocales().subscribe({
      next: res => {
        this.locales = res || [];
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
    this.selectedLocaleId = '';
    this.localeId = '';
    this.name = '';
    this.googleCode = '';
    this.sortOrder = this.locales.length + 1;
    this.showModal = true;
  }

  openEditModal(locale: Locale) {
    this.isEditMode = true;
    this.selectedLocaleId = locale.id;
    this.localeId = locale.id;
    this.name = locale.name;
    this.googleCode = locale.googleCode || '';
    this.sortOrder = locale.sortOrder || 0;
    this.showModal = true;
  }

  saveLocale() {
    if (!this.localeId || !this.name) {
      alert('Please fill out Language Code and Descriptive Name.');
      return;
    }

    const payload: Locale = {
      id: this.localeId.trim().toLowerCase(),
      name: this.name.trim(),
      googleCode: this.googleCode.trim() || undefined,
      sortOrder: this.sortOrder
    };

    this.saving = true;

    if (this.isEditMode) {
      this.api.updateLocale(this.selectedLocaleId, payload).subscribe({
        next: () => {
          this.saving = false;
          this.showModal = false;
          this.loadLocales();
        },
        error: err => {
          alert(err.error?.message || err.error || 'Error updating language');
          this.saving = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      this.api.createLocale(payload).subscribe({
        next: () => {
          this.saving = false;
          this.showModal = false;
          this.loadLocales();
        },
        error: err => {
          alert(err.error?.message || err.error || 'Error creating language');
          this.saving = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  deleteLocale(locale: Locale) {
    if (!confirm(`Are you sure you want to delete the language ${locale.name} (${locale.id.toUpperCase()})?`)) {
      return;
    }

    this.api.deleteLocale(locale.id).subscribe({
      next: () => {
        this.loadLocales();
      },
      error: err => {
        alert(err.error?.message || err.error || 'Error deleting language');
      }
    });
  }

  // ── Drag & Drop Rows ──────────────────────────────────────────────────
  onDragStart(event: DragEvent, index: number) {
    this.draggedIndex = index;
    if (event.dataTransfer) {
      event.dataTransfer.effectAllowed = 'move';
      event.dataTransfer.setData('text/plain', index.toString());
    }
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
  }

  onDrop(event: DragEvent, targetIndex: number) {
    event.preventDefault();
    if (this.draggedIndex === null || this.draggedIndex === targetIndex) {
      return;
    }

    const draggedLocale = this.locales[this.draggedIndex];
    this.locales.splice(this.draggedIndex, 1);
    this.locales.splice(targetIndex, 0, draggedLocale);
    this.draggedIndex = null;

    // Recalculate sortOrder for all items
    const updates = this.locales.map((locale, idx) => {
      const newSortOrder = idx + 1;
      const originalSortOrder = locale.sortOrder;
      
      if (originalSortOrder !== newSortOrder) {
        locale.sortOrder = newSortOrder;
        return this.api.updateLocale(locale.id, locale);
      }
      return null;
    }).filter(obs => obs !== null);

    if (updates.length > 0) {
      this.loading = true;
      forkJoin(updates).subscribe({
        next: () => {
          this.loadLocales();
        },
        error: (err) => {
          alert('Error updating sort order: ' + (err.error?.message || err.error || err.message));
          this.loadLocales();
        }
      });
    }
  }
}
