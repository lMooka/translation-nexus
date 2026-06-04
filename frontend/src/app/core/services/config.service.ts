import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class ConfigService {
  private config: any = null;

  constructor(private http: HttpClient) {}

  loadConfig(): Promise<void> {
    return firstValueFrom(this.http.get<any>('/config.json'))
      .then((config) => {
        this.config = config;
      })
      .catch((err) => {
        console.error('Could not load configuration file, falling back to default /api', err);
        this.config = { apiUrl: '/api' };
      });
  }

  get apiUrl(): string {
    return this.config?.apiUrl || '/api';
  }
}
