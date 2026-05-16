import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../models/order.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CartService {
  private apiUrl = `${environment.apiUrl}/api/orders`;

  constructor(private http: HttpClient) {}

  getCart(): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/cart`);
  }

  addToCart(productId: number, quantity: number = 1): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/cart/items`, { productId, quantity });
  }

  removeFromCart(itemId: number): Observable<Order> {
    return this.http.delete<Order>(`${this.apiUrl}/cart/items/${itemId}`);
  }

  clearCart(): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/cart`);
  }

  checkout(): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/cart/checkout`, {});
  }
}
