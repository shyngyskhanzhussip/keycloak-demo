export interface Order {
  id?: number;
  customerName: string;
  customerEmail: string;
  customerPhone: string;
  shippingAddress: string;
  status: OrderStatus;
  totalAmount: number;
  orderItems: OrderItem[];
  createdAt?: string;
  updatedAt?: string;
}

export interface OrderItem {
  id?: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}
