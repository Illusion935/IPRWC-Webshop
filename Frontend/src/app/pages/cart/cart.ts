import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CartService } from '../../services/cart.service';
import { Order } from '../../models/order.model';

@Component({
  selector: 'app-cart',
  imports: [CommonModule, RouterModule],
  templateUrl: './cart.html',
  styleUrl: './cart.scss'
})
export class CartComponent implements OnInit {
  cart = signal<Order | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);
  successMsg = signal<string | null>(null);

  constructor(private cartService: CartService) {}

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.loading.set(true);
    this.cartService.getCart().subscribe({
      next: data => {
        this.cart.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Fout bij ophalen van winkelmandje.');
        this.loading.set(false);
      }
    });
  }

  removeItem(itemId: number): void {
    this.cartService.removeFromCart(itemId).subscribe({
      next: data => this.cart.set(data),
      error: () => this.error.set('Verwijderen mislukt.')
    });
  }

  clearCart(): void {
    if (!confirm('Wil je het winkelmandje leegmaken?')) return;
    this.cartService.clearCart().subscribe({
      next: () => this.loadCart(),
      error: () => this.error.set('Leegmaken mislukt.')
    });
  }

  checkout(): void {
    this.cartService.checkout().subscribe({
      next: () => {
        this.successMsg.set('Bestelling geplaatst! Bedankt voor je aankoop.');
        this.cart.set(null);
        setTimeout(() => this.successMsg.set(null), 4000);
      },
      error: () => this.error.set('Afrekenen mislukt.')
    });
  }

  getTotal(): number {
    const items = this.cart()?.items ?? [];
    return items.reduce((sum, item) => sum + item.product.price * item.quantity, 0);
  }
}
