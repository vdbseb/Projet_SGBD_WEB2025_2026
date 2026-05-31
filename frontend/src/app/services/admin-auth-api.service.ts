import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface AdminLoginResponse {
  token: string;
  admin: any;
}

@Injectable({
  providedIn: 'root'
})
export class AdminAuthApiService {
  private readonly httpClient = inject(HttpClient);
  private readonly apiBaseUrl = 'http://localhost:8080/api';

  loginAdmin(matricule: string, password: string): Observable<AdminLoginResponse> {
    return this.httpClient.post<AdminLoginResponse>(
      `${this.apiBaseUrl}/auth/admin/login`,
      {
        matricule,
        password
      }
    );
  }
}
