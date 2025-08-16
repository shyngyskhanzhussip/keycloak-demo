import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule
  ],
  templateUrl: './product-form.html',
  styleUrl: './product-form.scss'
})
export class ProductFormComponent implements OnInit {
  product: Product = {
    name: '',
    description: '',
    price: 0,
    stockQuantity: 0,
    category: ''
  };

  categories = ['Electronics', 'Home & Kitchen', 'Sports', 'Fashion', 'Books', 'Toys'];

  constructor(
    public dialogRef: MatDialogRef<ProductFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Product
  ) {}

  ngOnInit(): void {
    if (this.data && this.data.id) {
      this.product = { ...this.data };
    }
  }

  onSubmit(): void {
    if (this.isFormValid()) {
      this.dialogRef.close(this.product);
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  isFormValid(): boolean {
    return !!(
      this.product.name &&
      this.product.description &&
      this.product.price > 0 &&
      this.product.stockQuantity >= 0 &&
      this.product.category
    );
  }
}
