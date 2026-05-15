export interface ProductImage {
  id: number;
  imageUrl: string;
  productName: string;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  images: ProductImage[];
}
