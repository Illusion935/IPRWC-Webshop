import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ProductService, ProductRequest } from '../../services/product.service';
import { Product } from '../../models/product.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './admin.html',
  styleUrl: './admin.scss'
})
export class AdminComponent implements OnInit {
  products = signal<Product[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  successMsg = signal<string | null>(null);

  // Formulier toestand
  showForm = signal(false);
  editingProduct = signal<Product | null>(null);
  formData: ProductRequest = { name: '', description: '', price: 0, stock: 0 };

  // Afbeelding toevoegen
  selectedProductForImage = signal<Product | null>(null);
  newImageUrl = '';

  constructor(
    private productService: ProductService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading.set(true);
    this.productService.getAll().subscribe({
      next: data => {
        this.products.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Fout bij ophalen producten.');
        this.loading.set(false);
      }
    });
  }

  openAddForm(): void {
    this.editingProduct.set(null);
    this.formData = { name: '', description: '', price: 0, stock: 0 };
    this.showForm.set(true);
    this.error.set(null);
  }

  openEditForm(product: Product): void {
    this.editingProduct.set(product);
    this.formData = {
      name: product.name,
      description: product.description,
      price: product.price,
      stock: product.stock
    };
    this.showForm.set(true);
    this.selectedProductForImage.set(null);
    this.error.set(null);
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.editingProduct.set(null);
  }

  saveProduct(): void {
    const editing = this.editingProduct();
    const action = editing
      ? this.productService.update(editing.id, this.formData)
      : this.productService.create(this.formData);

    action.subscribe({
      next: () => {
        this.showForm.set(false);
        this.editingProduct.set(null);
        this.showSuccess(editing ? 'Product bijgewerkt.' : 'Product toegevoegd.');
        this.loadProducts();
      },
      error: () => this.error.set('Opslaan mislukt.')
    });
  }

  deleteProduct(product: Product): void {
    if (!confirm(`Weet je zeker dat je "${product.name}" wilt verwijderen?`)) return;

    this.productService.delete(product.id).subscribe({
      next: () => {
        this.showSuccess('Product verwijderd.');
        this.loadProducts();
      },
      error: () => this.error.set('Verwijderen mislukt.')
    });
  }

  toggleImagePanel(product: Product): void {
    const current = this.selectedProductForImage();
    this.selectedProductForImage.set(current?.id === product.id ? null : product);
    this.newImageUrl = '';
    this.showForm.set(false);
  }

  addImage(product: Product): void {
    if (!this.newImageUrl.trim()) return;

    this.productService.addImage(product.id, this.newImageUrl.trim()).subscribe({
      next: () => {
        this.newImageUrl = '';
        this.showSuccess('Afbeelding toegevoegd.');
        this.loadProducts();
      },
      error: () => this.error.set('Afbeelding toevoegen mislukt.')
    });
  }

  deleteImage(product: Product, imageId: number): void {
    this.productService.deleteImage(product.id, imageId).subscribe({
      next: () => {
        this.showSuccess('Afbeelding verwijderd.');
        this.loadProducts();
      },
      error: () => this.error.set('Afbeelding verwijderen mislukt.')
    });
  }

  private showSuccess(msg: string): void {
    this.successMsg.set(msg);
    setTimeout(() => this.successMsg.set(null), 3000);
  }
}

