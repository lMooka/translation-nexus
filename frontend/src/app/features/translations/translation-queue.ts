import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { TranslationDocument, Locale } from '../../core/models/translation.model';

@Component({
  selector: 'app-translation-queue',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './translation-queue.html',
  styleUrl: './translation-queue.css'
})
export class TranslationQueueComponent implements OnInit {
  LOCALES: Locale[] = [];
  authorizedLocales: Locale[] = [];
  selectedLocale = '';

  currentDoc: TranslationDocument | null = null;
  draftValue = '';
  loading = false;
  saving = false;
  translating = false;
  errorMessage = '';
  infoMessage = '';

  constructor(
    public api: ApiService,
    public auth: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadLocales();
  }

  loadLocales() {
    this.loading = true;
    this.api.getLocales().subscribe({
      next: locales => {
        this.LOCALES = locales || [];
        this.filterAuthorizedLocales();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  filterAuthorizedLocales() {
    const isPowerUser = this.auth.isAdmin() || this.auth.isManager() || this.auth.isReviewer();
    if (isPowerUser) {
      this.authorizedLocales = this.LOCALES;
    } else {
      const allowed = this.auth.allowedLocales() || [];
      this.authorizedLocales = this.LOCALES.filter(l => 
        allowed.some(a => a.toLowerCase() === l.id.toLowerCase())
      );
    }

    if (this.authorizedLocales.length > 0) {
      this.selectedLocale = this.authorizedLocales[0].id;
      this.loadRandom();
    }
  }

  onLocaleChange() {
    this.currentDoc = null;
    this.draftValue = '';
    this.errorMessage = '';
    this.infoMessage = '';
    if (this.selectedLocale) {
      this.loadRandom();
    }
  }

  loadRandom() {
    if (!this.selectedLocale) return;
    this.loading = true;
    this.errorMessage = '';
    this.infoMessage = '';
    this.api.getRandomTranslation(this.selectedLocale).subscribe({
      next: doc => {
        this.loading = false;
        if (doc) {
          this.currentDoc = doc;
          this.draftValue = doc.translations?.[this.selectedLocale]?.translatedValue ?? '';
        } else {
          this.currentDoc = null;
          this.draftValue = '';
          this.infoMessage = `No translation tasks left for locale "${this.selectedLocale.toUpperCase()}". Everything is up-to-date!`;
        }
        this.cdr.detectChanges();
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || err.message || 'Error loading next translation';
        this.cdr.detectChanges();
      }
    });
  }

  save() {
    if (!this.currentDoc || !this.selectedLocale) return;
    this.saving = true;
    this.errorMessage = '';
    this.api.updateTranslation(this.currentDoc.id, this.selectedLocale, this.draftValue).subscribe({
      next: () => {
        this.saving = false;
        this.loadRandom();
      },
      error: err => {
        this.saving = false;
        this.errorMessage = err.error?.message || err.error || 'Error saving translation';
        this.cdr.detectChanges();
      }
    });
  }

  skip() {
    this.loadRandom();
  }

  autoTranslate() {
    if (!this.currentDoc || !this.currentDoc.baseValue || !this.selectedLocale) return;
    this.translating = true;
    this.errorMessage = '';
    this.api.autoTranslate(this.currentDoc.baseValue, this.selectedLocale).subscribe({
      next: res => {
        this.draftValue = res.translatedText || '';
        this.translating = false;
        this.cdr.detectChanges();
      },
      error: err => {
        this.errorMessage = err.error?.message || err.message || 'Auto translation failed';
        this.translating = false;
        this.cdr.detectChanges();
      }
    });
  }

  statusOf(): string {
    if (!this.currentDoc || !this.selectedLocale) return 'PENDING';
    return this.currentDoc.translations?.[this.selectedLocale]?.status ?? 'PENDING';
  }
}
