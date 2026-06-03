import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ApiService } from '../../core/services/api.service';
import { HistoryEntry, TranslationDocument } from '../../core/models/translation.model';

interface PendingItem {
  doc: TranslationDocument;
  locale: string;
}

@Component({
  selector: 'app-review-queue',
  imports: [DatePipe],
  templateUrl: './review-queue.html',
  styleUrl: './review-queue.css'
})
export class ReviewQueueComponent implements OnInit {

  pendingItems: PendingItem[] = [];
  selected: PendingItem | null = null;
  history: HistoryEntry[] = [];
  loading = false;
  approving = false;

  constructor(
    private api: ApiService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    setTimeout(() => this.load());
  }

  load() {
    this.loading = true;
    this.api.getPending().subscribe({
      next: docs => {
        this.pendingItems = (docs || []).flatMap(doc =>
          doc.translations ? Object.entries(doc.translations)
            .filter(([, tv]) => tv.status === 'REVIEW')
            .map(([locale]) => ({ doc, locale })) : []
        );
        this.loading = false;
        if (this.pendingItems.length > 0 && !this.selected) {
          this.select(this.pendingItems[0]);
        }
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  select(item: PendingItem) {
    this.selected = item;
    this.history = [];
    this.api.getHistory(item.doc.id).subscribe({
      next: h => {
        this.history = (h || []).filter(e => e.locale === item.locale).slice().reverse();
        this.cdr.detectChanges();
      },
      error: () => {
        this.cdr.detectChanges();
      }
    });
  }

  isSelected(item: PendingItem): boolean {
    return this.selected !== null &&
      this.selected.doc.id === item.doc.id &&
      this.selected.locale === item.locale;
  }

  approve() {
    if (!this.selected) return;
    this.approving = true;
    this.api.approve(this.selected.doc.id, this.selected.locale).subscribe({
      next: () => {
        this.approving = false;
        this.selected = null;
        this.load();
      },
      error: () => {
        this.approving = false;
        this.cdr.detectChanges();
      }
    });
  }

  translationOf(item: PendingItem): string {
    return item.doc.translations?.[item.locale]?.translatedValue ?? '';
  }

  previousValueOf(item: PendingItem): string {
    const entries = (item.doc.history || [])
      .filter(h => h.locale === item.locale && h.action === 'EDIT');
    if (entries.length < 2) return '—';
    return entries[entries.length - 2]?.newValue ?? '—';
  }
}
