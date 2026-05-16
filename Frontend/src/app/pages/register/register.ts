import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrl: '../login/login.scss'
})
export class RegisterComponent {
  email = '';
  password = '';
  error = signal<string | null>(null);
  loading = signal(false);
  success = signal(false);

  constructor(private authService: AuthService, private router: Router) {}

  onSubmit(): void {
    this.error.set(null);
    this.loading.set(true);

    this.authService.register({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.loading.set(false);
        this.success.set(true);
        setTimeout(() => this.router.navigate(['/']), 2000);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Registratie mislukt. Probeer een ander e-mailadres.');
      }
    });
  }
}
