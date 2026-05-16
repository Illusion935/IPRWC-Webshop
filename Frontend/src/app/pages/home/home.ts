import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProductService } from '../../services/product.service';
import { CartService } from '../../services/cart.service';
import { AuthService } from '../../services/auth.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-home',
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class HomeComponent implements OnInit {
  products = signal<Product[]>([]);
  addedProductId = signal<number | null>(null);

  constructor(
    private productService: ProductService,
    private cartService: CartService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.productService.getAll().subscribe({
      next: data => this.products.set(data),
      error: err => console.error('Fout bij ophalen producten:', err)
    });
  }

  getFirstImage(product: Product): string {
    return product.images[0]?.imageUrl ?? '';
  }

  addToCart(product: Product): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.cartService.addToCart(product.id).subscribe({
      next: () => {
        this.addedProductId.set(product.id);
        setTimeout(() => this.addedProductId.set(null), 1500);
      }
    });
  }
}
