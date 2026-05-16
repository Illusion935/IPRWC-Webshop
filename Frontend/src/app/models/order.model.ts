import { Product } from './product.model';

export interface OrderItem {
  id: number;
  product: Product;
  quantity: number;
}

export interface Order {
  id: number;
  items: OrderItem[];
  status: 'CART' | 'PLACED';
  createdAt: string;
}
