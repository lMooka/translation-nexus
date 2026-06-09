import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { Category, PathMapping } from '../../core/models/translation.model';

@Component({
  selector: 'app-admin-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-categories.html',
  styleUrl: './admin-categories.css'
})
export class AdminCategoriesComponent implements OnInit {

  categories: Category[] = [];
  loading = false;
  saving = false;

  // Modal State
  showModal = false;
  isEditMode = false;
  selectedCategoryId: string | null = null;

  // Form Fields
  name = '';
  pathMappings: PathMapping[] = [];
  priority = 3;

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    setTimeout(() => this.loadCategories());
  }

  loadCategories() {
    this.loading = true;
    this.api.getCategories().subscribe({
      next: res => {
        this.categories = res || [];
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
    this.selectedCategoryId = null;
    this.name = '';
    this.pathMappings = [{ pattern: '', filename: '' }];
    this.priority = 3;
    this.showModal = true;
  }

  openEditModal(category: Category) {
    this.isEditMode = true;
    this.selectedCategoryId = category.id || null;
    this.name = category.name;
    this.pathMappings = category.pathMappings ? category.pathMappings.map(pm => ({ ...pm })) : [];
    this.priority = category.priority || 3;
    this.showModal = true;
  }

  addPathMapping() {
    this.pathMappings.push({ pattern: '', filename: '' });
  }

  removePathMapping(index: number) {
    this.pathMappings.splice(index, 1);
  }

  saveCategory() {
    if (!this.name) {
      alert('Please fill out Category Name.');
      return;
    }

    // Validate pathMappings
    for (const pm of this.pathMappings) {
      if (!pm.pattern.trim() || !pm.filename.trim()) {
        alert('All pattern and filename fields must be filled out.');
        return;
      }

      // Check wildcard count: pattern must contain exactly one '*'
      const starCount = (pm.pattern.match(/\*/g) || []).length;
      if (starCount !== 1) {
        alert(`Pattern "${pm.pattern}" must contain exactly one wildcard (*)`);
        return;
      }

      if (!pm.filename.trim().toLowerCase().endsWith('.csv')) {
        pm.filename = pm.filename.trim() + '.csv';
      } else {
        pm.filename = pm.filename.trim();
      }
      pm.pattern = pm.pattern.trim();
    }

    this.saving = true;

    const categoryPayload: Category = {
      name: this.name.trim(),
      pathMappings: this.pathMappings,
      priority: Number(this.priority)
    };

    if (this.isEditMode && this.selectedCategoryId) {
      this.api.updateCategory(this.selectedCategoryId, categoryPayload).subscribe({
        next: () => {
          this.saving = false;
          this.showModal = false;
          this.loadCategories();
        },
        error: err => {
          alert(err.error?.message || err.error || 'Error updating category');
          this.saving = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      this.api.createCategory(categoryPayload).subscribe({
        next: () => {
          this.saving = false;
          this.showModal = false;
          this.loadCategories();
        },
        error: err => {
          alert(err.error?.message || err.error || 'Error creating category');
          this.saving = false;
          this.cdr.detectChanges();
        }
      });
    }
  }

  deleteCategory(category: Category) {
    if (!category.id) return;
    if (!confirm(`Are you sure you want to delete category ${category.name}?`)) return;

    this.api.deleteCategory(category.id).subscribe({
      next: () => {
        this.loadCategories();
      },
      error: err => {
        alert(err.error?.message || err.error || 'Error deleting category');
      }
    });
  }
}
