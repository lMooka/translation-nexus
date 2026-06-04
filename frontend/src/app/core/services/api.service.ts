import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ConfigService } from './config.service';
import {
  HistoryEntry,
  LoginResponse,
  Page,
  TranslationDocument,
  UserDTO,
  UserCreateDTO,
  UserUpdateDTO,
  Locale,
  AppVersion,
  Category
} from '../models/translation.model';

@Injectable({ providedIn: 'root' })
export class ApiService {

  constructor(
    private http: HttpClient,
    private configService: ConfigService
  ) {}

  private get base(): string {
    return this.configService.apiUrl;
  }

  // ── Auth ─────────────────────────────────────────────────────────────
  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.base}/auth/login`, { username, password });
  }

  // ── Translations ─────────────────────────────────────────────────────
  listTranslations(filters: {
    version?: string; tag?: string | string[]; category?: string; search?: string;
    page?: number; size?: number;
  }): Observable<Page<TranslationDocument>> {
    let params = new HttpParams();
    if (filters.version)  params = params.set('version', filters.version);
    if (filters.tag) {
      if (Array.isArray(filters.tag)) {
        filters.tag.forEach(t => {
          params = params.append('tag', t);
        });
      } else {
        params = params.set('tag', filters.tag);
      }
    }
    if (filters.category) params = params.set('category', filters.category);
    if (filters.search)   params = params.set('search', filters.search);
    params = params.set('page', filters.page ?? 0);
    params = params.set('size', filters.size ?? 20);
    return this.http.get<Page<TranslationDocument>>(`${this.base}/translations`, { params });
  }

  getLocales(): Observable<Locale[]> {
    return this.http.get<Locale[]>(`${this.base}/locales`);
  }

  createLocale(locale: Locale): Observable<Locale> {
    return this.http.post<Locale>(`${this.base}/locales`, locale);
  }

  updateLocale(id: string, locale: Locale): Observable<Locale> {
    return this.http.put<Locale>(`${this.base}/locales/${id}`, locale);
  }

  deleteLocale(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/locales/${id}`);
  }

  createKey(dto: {
    keyCode: string; version: string; category: string;
    tags: string[]; contextInfo: string; baseValue: string;
  }): Observable<TranslationDocument> {
    return this.http.post<TranslationDocument>(`${this.base}/translations/keys`, dto);
  }

  deleteKey(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/translations/${id}`);
  }

  autoTranslate(text: string, targetLanguage: string): Observable<{ translatedText: string }> {
    return this.http.post<{ translatedText: string }>(`${this.base}/translations/translate`, { text, targetLanguage });
  }

  updateTranslation(id: string, locale: string, value: string): Observable<TranslationDocument> {
    return this.http.put<TranslationDocument>(`${this.base}/translations/${id}/${locale}`, { value });
  }

  getPending(): Observable<TranslationDocument[]> {
    return this.http.get<TranslationDocument[]>(`${this.base}/translations/pending`);
  }

  approve(id: string, locale: string): Observable<TranslationDocument> {
    return this.http.post<TranslationDocument>(`${this.base}/translations/${id}/${locale}/approve`, {});
  }

  updateStatus(id: string, locale: string, status: string): Observable<TranslationDocument> {
    return this.http.put<TranslationDocument>(`${this.base}/translations/${id}/${locale}/status`, {}, {
      params: { status }
    });
  }

  getHistory(id: string): Observable<HistoryEntry[]> {
    return this.http.get<HistoryEntry[]>(`${this.base}/translations/${id}/history`);
  }

  // ── Export ───────────────────────────────────────────────────────────
  exportTranslations(version?: string): Observable<Blob> {
    let params = new HttpParams();
    if (version) params = params.set('version', version);
    return this.http.get(`${this.base}/export`, {
      params,
      responseType: 'blob'
    });
  }

  // ── Users (Admin) ────────────────────────────────────────────────────
  getUsers(): Observable<UserDTO[]> {
    return this.http.get<UserDTO[]>(`${this.base}/users`);
  }

  createUser(dto: UserCreateDTO): Observable<UserDTO> {
    return this.http.post<UserDTO>(`${this.base}/users`, dto);
  }

  updateUser(id: string, dto: UserUpdateDTO): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.base}/users/${id}`, dto);
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/users/${id}`);
  }

  getVersions(): Observable<AppVersion[]> {
    return this.http.get<AppVersion[]>(`${this.base}/versions`);
  }

  createVersion(version: string): Observable<AppVersion> {
    return this.http.post<AppVersion>(`${this.base}/versions`, { version });
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.base}/categories`);
  }

  createCategory(category: Category): Observable<Category> {
    return this.http.post<Category>(`${this.base}/categories`, category);
  }

  updateCategory(id: string, category: Category): Observable<Category> {
    return this.http.put<Category>(`${this.base}/categories/${id}`, category);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.base}/categories/${id}`);
  }
}
