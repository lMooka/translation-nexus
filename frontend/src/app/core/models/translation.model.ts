export interface TranslationValue {
  translatedValue: string;
  status: 'PENDING' | 'REVIEW' | 'APPROVED';
  lastModifiedBy: string;
  updatedAt: string;
}

export interface HistoryEntry {
  locale: string;
  modifiedBy: string;
  previousValue: string | null;
  newValue: string;
  action: 'EDIT' | 'APPROVE';
  timestamp: string;
}

export interface TranslationDocument {
  id: string;
  keyCode: string;
  version: string;
  category: string;
  tags: string[];
  contextInfo: string;
  baseValue: string;
  translations: Record<string, TranslationValue>;
  history: HistoryEntry[];
  createdAt: string;
  updatedAt: string;
}

export interface Page<T> {
  content: T[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
  size?: number;
  page?: {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
  };
}

export interface LoginResponse {
  token: string;
  roles: string[];
  username: string;
  allowedLocales: string[];
}

export interface UserDTO {
  id: string;
  username: string;
  roles: string[];
  allowedLocales: string[];
}

export interface UserCreateDTO {
  username?: string;
  password?: string;
  roles?: string[];
  allowedLocales?: string[];
}

export interface UserUpdateDTO {
  roles?: string[];
  allowedLocales?: string[];
}

export interface Locale {
  id: string;
  name: string;
  googleCode?: string;
  sortOrder?: number;
}

export interface AppVersion {
  id: string;
  version: string;
  active: boolean;
  createdAt: string;
}

export interface PathMapping {
  pattern: string;
  filename: string;
}

export interface Category {
  id?: string;
  name: string;
  pathMappings: PathMapping[];
  createdAt?: string;
  updatedAt?: string;
}
