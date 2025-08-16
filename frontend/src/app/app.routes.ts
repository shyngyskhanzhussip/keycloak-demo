import { Routes } from '@angular/router';
import { ProductsComponent } from './components/products/products';
import { OrdersComponent } from './components/orders/orders';

export const routes: Routes = [
  { path: '', redirectTo: '/products', pathMatch: 'full' },
  { path: 'products', component: ProductsComponent },
  { path: 'orders', component: OrdersComponent }
];
