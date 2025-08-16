import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Order, OrderStatus } from '../../models/order.model';
import { OrderService } from '../../services/order.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule
  ],
  templateUrl: './orders.html',
  styleUrl: './orders.scss'
})
export class OrdersComponent implements OnInit {
  orders: Order[] = [];
  displayedColumns: string[] = ['id', 'customerName', 'customerEmail', 'status', 'totalAmount', 'createdAt', 'actions'];
  selectedStatus: OrderStatus | '' = '';
  searchEmail: string = '';
  orderStatuses = Object.values(OrderStatus);

  constructor(
    private orderService: OrderService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.orderService.getOrders().subscribe({
      next: (data) => {
        this.orders = data;
      },
      error: (error) => {
        console.error('Error loading orders:', error);
        this.snackBar.open('Error loading orders', 'Close', { duration: 3000 });
      }
    });
  }

  updateOrderStatus(order: Order, status: OrderStatus): void {
    this.orderService.updateOrderStatus(order.id!, status).subscribe({
      next: () => {
        this.loadOrders();
        this.snackBar.open('Order status updated successfully', 'Close', { duration: 3000 });
      },
      error: (error) => {
        console.error('Error updating order status:', error);
        this.snackBar.open('Error updating order status', 'Close', { duration: 3000 });
      }
    });
  }

  deleteOrder(id: number): void {
    if (confirm('Are you sure you want to delete this order?')) {
      this.orderService.deleteOrder(id).subscribe({
        next: () => {
          this.loadOrders();
          this.snackBar.open('Order deleted successfully', 'Close', { duration: 3000 });
        },
        error: (error) => {
          console.error('Error deleting order:', error);
          this.snackBar.open('Error deleting order', 'Close', { duration: 3000 });
        }
      });
    }
  }

  filterByStatus(): void {
    if (this.selectedStatus) {
      this.orderService.getOrdersByStatus(this.selectedStatus).subscribe({
        next: (data) => {
          this.orders = data;
        },
        error: (error) => {
          console.error('Error filtering orders:', error);
          this.snackBar.open('Error filtering orders', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.loadOrders();
    }
  }

  searchByEmail(): void {
    if (this.searchEmail.trim()) {
      this.orderService.getOrdersByCustomerEmail(this.searchEmail).subscribe({
        next: (data) => {
          this.orders = data;
        },
        error: (error) => {
          console.error('Error searching orders:', error);
          this.snackBar.open('Error searching orders', 'Close', { duration: 3000 });
        }
      });
    } else {
      this.loadOrders();
    }
  }

  getStatusColor(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.PENDING:
        return 'status-badge status-pending';
      case OrderStatus.CONFIRMED:
        return 'status-badge status-confirmed';
      case OrderStatus.SHIPPED:
        return 'status-badge status-shipped';
      case OrderStatus.DELIVERED:
        return 'status-badge status-delivered';
      case OrderStatus.CANCELLED:
        return 'status-badge status-cancelled';
      default:
        return 'status-badge text-gray-600 bg-gray-100';
    }
  }

  getStatusDotColor(status: OrderStatus): string {
    switch (status) {
      case OrderStatus.PENDING:
        return 'bg-yellow-400';
      case OrderStatus.CONFIRMED:
        return 'bg-blue-400';
      case OrderStatus.SHIPPED:
        return 'bg-purple-400';
      case OrderStatus.DELIVERED:
        return 'bg-green-400';
      case OrderStatus.CANCELLED:
        return 'bg-red-400';
      default:
        return 'bg-gray-400';
    }
  }

  getTotalRevenue(): string {
    const total = this.orders.reduce((sum, order) => sum + order.totalAmount, 0);
    return total.toFixed(2);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
}
