import { Component, OnInit, ChangeDetectorRef, HostListener } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { TranslationDocument, AppVersion, Category } from '../../core/models/translation.model';

@Component({
  selector: 'app-translation-list',
  imports: [FormsModule],
  templateUrl: './translation-list.html',
  styleUrl: './translation-list.css'
})
export class TranslationListComponent implements OnInit {

  docs: TranslationDocument[] = [];
  loading = false;
  totalPages = 0;
  currentPage = 0;

  // Filters
  filterVersion = '';
  filterTags: string[] = [];
  inputTag = '';
  filterCategory = '';
  filterSearch = '';
  showSuggestions = false;

  // Inline edit state: docId → locale → draft value
  drafts: Record<string, Record<string, string>> = {};
  saving: Record<string, boolean> = {};
  translating: Record<string, boolean> = {};
  errors: Record<string, string> = {};

  // New key modal
  showNewKeyModal = false;
  newKeyCategoryName = '';
  newKeyAlias = '';
  newKeyManualCode = '';
  newKeyBaseValues: Record<string, string> = {};
  newKeyTags = '';
  newKeyContextInfo = '';

  VERSIONS: AppVersion[] = [];
  activeVersion: AppVersion | null = null;
  showNewVersionModal = false;
  newVersionName = '';

  LOCALES: string[] = [];
  selectedLocales: Record<string, boolean> = {};
  showLocaleDropdown = false;

  categories: Category[] = [];

  constructor(
    public api: ApiService,
    public auth: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  @HostListener('document:click')
  closeDropdowns() {
    this.showLocaleDropdown = false;
  }

  getFilteredLocales(): string[] {
    return this.LOCALES.filter(l => this.selectedLocales[l]);
  }

  getSelectedCount(): number {
    return this.LOCALES.filter(l => this.selectedLocales[l]).length;
  }

  isAllSelected(): boolean {
    return this.LOCALES.length > 0 && this.LOCALES.every(l => this.selectedLocales[l]);
  }

  toggleAll() {
    const targetState = !this.isAllSelected();
    this.LOCALES.forEach(l => this.selectedLocales[l] = targetState);
    this.cdr.detectChanges();
  }

  ngOnInit() {
    this.loadCategories();
    this.api.getLocales().subscribe({
      next: locales => {
        this.LOCALES = locales.map(l => l.id);
        this.LOCALES.forEach(l => {
          this.selectedLocales[l] = true;
        });
        this.loadVersionsAndData();
      },
      error: () => {
        this.loadVersionsAndData();
      }
    });
  }

  loadVersionsAndData() {
    this.api.getVersions().subscribe({
      next: versions => {
        this.VERSIONS = versions || [];
        this.activeVersion = this.VERSIONS.find(v => v.active) || null;
        if (this.activeVersion && !this.filterVersion) {
          this.filterVersion = this.activeVersion.version;
        }
        this.load();
      },
      error: () => {
        this.load();
      }
    });
  }

  load(page = 0) {
    this.loading = true;
    this.api.listTranslations({
      version: this.filterVersion || undefined,
      tag: this.filterTags.length > 0 ? this.filterTags : undefined,
      category: this.filterCategory || undefined,
      search: this.filterSearch || undefined,
      page, size: 15
    }).subscribe({
      next: res => {
        this.docs = res.content || [];
        this.totalPages = res.totalPages ?? res.page?.totalPages ?? 0;
        this.currentPage = res.number ?? res.page?.number ?? 0;
        this.loading = false;
        // Initialise drafts from current values
        this.docs.forEach(doc => {
          this.drafts[doc.id] = {};
          this.LOCALES.forEach(l => {
            this.drafts[doc.id][l] = doc.translations?.[l]?.translatedValue ?? '';
          });
        });
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  save(doc: TranslationDocument, locale: string) {
    const key = `${doc.id}-${locale}`;
    this.saving[key] = true;
    this.errors[key] = '';
    const value = this.drafts[doc.id]?.[locale] ?? '';

    this.api.updateTranslation(doc.id, locale, value).subscribe({
      next: updated => {
        const idx = this.docs.findIndex(d => d.id === updated.id);
        if (idx > -1) this.docs[idx] = updated;
        this.saving[key] = false;
        this.cdr.detectChanges();
      },
      error: err => {
        this.errors[key] = err.error ?? 'Error saving translation';
        this.saving[key] = false;
        this.cdr.detectChanges();
      }
    });
  }

  updateStatus(doc: TranslationDocument, locale: string, status: string) {
    const key = doc.id + '-' + locale;
    this.saving[key] = true;
    this.errors[key] = '';
    this.api.updateStatus(doc.id, locale, status).subscribe({
      next: updated => {
        const idx = this.docs.findIndex(d => d.id === updated.id);
        if (idx > -1) this.docs[idx] = updated;
        this.saving[key] = false;
        this.cdr.detectChanges();
      },
      error: err => {
        this.errors[key] = err.error ?? 'Error updating status';
        this.saving[key] = false;
        this.cdr.detectChanges();
      }
    });
  }

  statusOf(doc: TranslationDocument, locale: string): string {
    return doc.translations?.[locale]?.status ?? 'PENDING';
  }

  isEditable(doc: TranslationDocument, locale: string): boolean {
    return this.statusOf(doc, locale) === 'PENDING';
  }

  submitNewKey() {
    if (!this.newKeyCategoryName) {
      alert('Please select a Category.');
      return;
    }
    const cat = this.getSelectedCategory();
    if (!cat || !cat.pathMappings || cat.pathMappings.length === 0) {
      alert('Selected category has no path mappings.');
      return;
    }

    const hasGeneral = cat.pathMappings.some(pm => pm.pattern === '*');
    if (!hasGeneral && !this.newKeyAlias.trim()) {
      alert('Please enter an alias.');
      return;
    }
    if (hasGeneral && !this.newKeyManualCode.trim()) {
      alert('Please enter a Key Code.');
      return;
    }

    const requests = [];
    for (const pm of cat.pathMappings) {
      const val = this.newKeyBaseValues[pm.pattern] || '';
      if (!val.trim()) {
        alert(`English text for "${pm.pattern}" must be filled out.`);
        return;
      }
      const keyCode = this.getComputedKeyCodeForPattern(pm.pattern);
      requests.push(this.api.createKey({
        keyCode,
        category: this.newKeyCategoryName,
        tags: this.newKeyTags.split(',').map(t => t.trim()).filter(Boolean),
        contextInfo: this.newKeyContextInfo,
        baseValue: val.trim(),
        version: ''
      }));
    }

    this.loading = true;
    forkJoin(requests).subscribe({
      next: () => {
        this.showNewKeyModal = false;
        this.newKeyCategoryName = '';
        this.newKeyAlias = '';
        this.newKeyManualCode = '';
        this.newKeyBaseValues = {};
        this.newKeyTags = '';
        this.newKeyContextInfo = '';
        this.load();
      },
      error: err => {
        alert(err.error?.message ?? err.error ?? 'Error creating keys');
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  submitNewVersion() {
    if (!this.newVersionName) return;
    this.api.createVersion(this.newVersionName).subscribe({
      next: () => {
        this.showNewVersionModal = false;
        this.filterVersion = this.newVersionName.trim();
        this.newVersionName = '';
        this.loadVersionsAndData();
      },
      error: err => {
        alert(err.error?.message ?? err.error ?? 'Error creating version');
        this.cdr.detectChanges();
      }
    });
  }

  applyFilters() { this.load(0); }
  prevPage() { if (this.currentPage > 0) this.load(this.currentPage - 1); }
  nextPage() { if (this.currentPage < this.totalPages - 1) this.load(this.currentPage + 1); }

  loadCategories() {
    this.api.getCategories().subscribe({
      next: cats => {
        this.categories = cats || [];
        this.cdr.detectChanges();
      }
    });
  }

  getFilteredCategories(): Category[] {
    const val = this.filterCategory.toLowerCase().trim();
    if (!val) {
      return this.categories;
    }
    return this.categories.filter(c => c.name.toLowerCase().includes(val));
  }

  selectCategory(name: string) {
    this.filterCategory = name;
    this.showSuggestions = false;
    this.applyFilters();
  }

  onCategoryBlur() {
    setTimeout(() => {
      this.showSuggestions = false;
      this.cdr.detectChanges();
    }, 200);
  }

  clearCategory() {
    this.filterCategory = '';
    this.applyFilters();
  }

  addTag() {
    const tag = this.inputTag.trim().toLowerCase();
    if (tag && !this.filterTags.includes(tag)) {
      this.filterTags.push(tag);
      this.inputTag = '';
      this.applyFilters();
    }
  }

  removeTag(tag: string) {
    this.filterTags = this.filterTags.filter(t => t !== tag);
    this.applyFilters();
  }

  resolveFilename(doc: TranslationDocument): string {
    const category = this.categories.find(c => c.name.toLowerCase() === doc.category.toLowerCase());
    if (!category || !category.pathMappings) return '';
    
    for (const pm of category.pathMappings) {
      if (this.matchPattern(doc.keyCode, pm.pattern)) {
        return pm.filename;
      }
    }
    return '';
  }

  matchPattern(keyCode: string, pattern: string): boolean {
    if (!pattern || !keyCode) return false;
    if (pattern === '*') return true;
    
    const regexStr = '^' + pattern
      .replace(/\./g, '\\.')
      .replace(/\*/g, '([^.]+)') + '$';
    try {
      const regex = new RegExp(regexStr);
      return regex.test(keyCode);
    } catch (e) {
      return false;
    }
  }

  exportAll() {
    this.api.exportTranslations(this.filterVersion || undefined).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = this.filterVersion ? `translations-${this.filterVersion}.zip` : 'translations.zip';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err) => {
        alert('Error exporting translations: ' + (err.error?.message || err.message));
      }
    });
  }

  getSelectedCategory(): Category | null {
    return this.categories.find(c => c.name.toLowerCase() === this.newKeyCategoryName.toLowerCase()) || null;
  }

  getComputedKeyCodeForPattern(pattern: string): string {
    if (!pattern) return '';
    if (pattern === '*') {
      return this.newKeyManualCode.trim();
    }
    return pattern.replace('*', this.newKeyAlias.trim());
  }

  onCategoryChange() {
    this.newKeyAlias = '';
    this.newKeyManualCode = '';
    this.newKeyBaseValues = {};
    const cat = this.getSelectedCategory();
    if (cat && cat.pathMappings) {
      cat.pathMappings.forEach(pm => {
        this.newKeyBaseValues[pm.pattern] = '';
      });
    }
  }

  deleteKey(doc: TranslationDocument) {
    if (!confirm(`Are you sure you want to delete translation key "${doc.keyCode}"?`)) {
      return;
    }
    this.api.deleteKey(doc.id).subscribe({
      next: () => {
        this.load(this.currentPage);
      },
      error: err => {
        alert(err.error?.message ?? err.error ?? 'Error deleting translation key');
      }
    });
  }

  autoTranslate(doc: TranslationDocument, locale: string) {
    if (!doc.baseValue) {
      alert('Source English text is empty.');
      return;
    }
    const key = doc.id + '-' + locale;
    this.translating[key] = true;
    this.errors[key] = '';
    this.api.autoTranslate(doc.baseValue, locale).subscribe({
      next: res => {
        if (!this.drafts[doc.id]) {
          this.drafts[doc.id] = {};
        }
        this.drafts[doc.id][locale] = res.translatedText || '';
        this.translating[key] = false;
        this.cdr.detectChanges();
      },
      error: err => {
        this.errors[key] = err.error?.message || err.message || 'Translation failed';
        this.translating[key] = false;
        this.cdr.detectChanges();
      }
    });
  }
}
