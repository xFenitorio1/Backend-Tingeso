-- 1. POBLAR USUARIOS (users)
-- Los IDs se generan automáticamente: 1=Admin, 2=Juan (Cliente), 3=Maria (Cliente)
INSERT INTO users (full_name, email, phone, document_id, password, is_active, role) VALUES ('Administrador General', 'admin@travelagency.com', '+123456789', 'DOC-001', 'admin123', true, 'ADMIN');
INSERT INTO users (full_name, email, phone, document_id, password, is_active, role) VALUES ('Juan Pérez', 'juan.perez@email.com', '+987654321', 'DOC-002', 'password', true, 'CLIENT');
INSERT INTO users (full_name, email, phone, document_id, password, is_active, role) VALUES ('Maria Lopez', 'maria.lopez@email.com', '+010101010', 'DOC-003', 'password', true, 'CLIENT');

-- 2. POBLAR PAQUETES (travel_packages)
-- IDs generados: 1=Cancún, 2=Machupicchu, 3=París
INSERT INTO travel_packages (name, destination, description, start_date, end_date, price, total_capacity, available_spots, status) VALUES ('Escapada a Cancún', 'Cancún, México', '5 días de todo incluido en las hermosas playas de Cancún.', '2026-06-01', '2026-06-06', 899.00, 20, 16, 'AVAILABLE');
INSERT INTO travel_packages (name, destination, description, start_date, end_date, price, total_capacity, available_spots, status) VALUES ('Aventura en Machupicchu', 'Cusco, Perú', 'Caminata de 7 días por los andes.', '2026-07-10', '2026-07-17', 1250.00, 10, 0, 'SOLD_OUT');
INSERT INTO travel_packages (name, destination, description, start_date, end_date, price, total_capacity, available_spots, status) VALUES ('Fin de Semana Romántico', 'París, Francia', '3 días espectaculares en la ciudad del amor.', '2026-05-20', '2026-05-23', 650.00, 15, 15, 'AVAILABLE');

-- 3. POBLAR PROMOCIONES (promotions)
INSERT INTO promotions (name, discount_percentage, valid_from, valid_to, active) VALUES ('Verano Anticipado', 0.15, '2026-04-01', '2026-05-01', true);
INSERT INTO promotions (name, discount_percentage, valid_from, valid_to, active) VALUES ('Mes del amor', 0.2, '2026-02-01', '2026-02-28', false);

-- 4. POBLAR RESERVAS (bookings)
-- Se crea una reserva para Juan (customer_id=2) en Cancún (travel_package_id=1)
-- passenger_count=4 aplica descuento por grupo (según lógica de negocio)
INSERT INTO bookings (customer_id, travel_package_id, passenger_count, base_price, total_discount, final_amount, status, created_at) VALUES (2, 1, 4, 899.00, 359.60, 3236.40, 'PAID', '2026-04-01 10:00:00');
INSERT INTO bookings (customer_id, travel_package_id, passenger_count, base_price, total_discount, final_amount, status, created_at) VALUES (3, 3, 2, 650.00, 0.00, 1300.00, 'PENDING_PAYMENT', '2026-04-02 12:00:00');

-- 5. POBLAR PAGOS (payments)
-- Pago asociado a la reserva 1 (ID de booking autogenerado=1)
INSERT INTO payments (booking_id, amount, payment_method, transaction_id, payment_date) VALUES (1, 3236.40, 'Credit Card', 'TXN-778899', '2026-04-01 10:05:00');