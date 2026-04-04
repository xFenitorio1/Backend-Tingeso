-- POBLAR USUARIOS (users)
INSERT INTO users (full_name, email, phone, document_id, password, is_active, role) VALUES ('Administrador General', 'admin@travelagency.com', '+123456789', 'DOC-001', 'admin123', true, 'ADMIN');
INSERT INTO users (full_name, email, phone, document_id, password, is_active, role) VALUES ('Juan Pérez', 'juan.perez@email.com', '+987654321', 'DOC-002', 'password', true, 'CLIENT');
INSERT INTO users (full_name, email, phone, document_id, password, is_active, role) VALUES ('Maria Lopez', 'maria.lopez@email.com', '+010101010', 'DOC-003', 'password', true, 'CLIENT');

-- POBLAR PAQUETES (travel_packages)
INSERT INTO travel_packages (name, destination, description, start_date, end_date, price, total_capacity, available_spots, status) VALUES ('Escapada a Cancún', 'Cancún, México', '5 días de todo incluido en las hermosas playas de Cancún.', '2026-06-01', '2026-06-06', 899.00, 20, 16, 'AVAILABLE');
INSERT INTO travel_packages (name, destination, description, start_date, end_date, price, total_capacity, available_spots, status) VALUES ('Aventura en Machupicchu', 'Cusco, Perú', 'Caminata de 7 días por los andes.', '2026-07-10', '2026-07-17', 1250.00, 10, 0, 'SOLD_OUT');
INSERT INTO travel_packages (name, destination, description, start_date, end_date, price, total_capacity, available_spots, status) VALUES ('Fin de Semana Romántico', 'París, Francia', '3 días espectaculares en la ciudad del amor.', '2026-05-20', '2026-05-23', 650.00, 15, 15, 'AVAILABLE');
INSERT INTO travel_packages (name, destination, description, start_date, end_date, price, total_capacity, available_spots, status) VALUES ('Luces de Tokio', 'Tokio, Japón', 'Tour tecnológico y cultural de 10 días.', '2026-09-15', '2026-09-25', 2100.00, 12, 11, 'AVAILABLE');
INSERT INTO travel_packages (name, destination, description, start_date, end_date, price, total_capacity, available_spots, status) VALUES ('Historia Viva: Roma', 'Roma, Italia', 'Recorrido histórico por el Coliseo y el Vaticano.', '2026-06-10', '2026-06-17', 950.00, 25, 23, 'AVAILABLE');
INSERT INTO travel_packages (name, destination, description, start_date, end_date, price, total_capacity, available_spots, status) VALUES ('Safari Salvaje', 'Masái Mara, Kenia', 'Aventura de 6 días observando la gran migración.', '2026-08-05', '2026-08-11', 1800.00, 8, 8, 'AVAILABLE');
INSERT INTO travel_packages (name, destination, description, start_date, end_date, price, total_capacity, available_spots, status) VALUES ('Crucero Express', 'Bahamas', '3 noches de fiesta y sol en alta mar.', '2026-05-01', '2026-05-04', 450.00, 50, 50, 'AVAILABLE');

-- POBLAR PROMOCIONES (promotions)
INSERT INTO promotions (name, discount_percentage, valid_from, valid_to, active) VALUES ('Verano Anticipado', 0.15, '2026-04-01', '2026-05-01', true);
INSERT INTO promotions (name, discount_percentage, valid_from, valid_to, active) VALUES ('Mes del amor', 0.2, '2026-02-01', '2026-02-28', false);
INSERT INTO promotions (name, discount_percentage, valid_from, valid_to, active) VALUES ('Black Friday Viajero', 0.25, '2026-11-20', '2026-11-30', false);
INSERT INTO promotions (name, discount_percentage, valid_from, valid_to, active) VALUES ('Explora Asia', 0.10, '2026-04-01', '2026-08-31', true);
INSERT INTO promotions (name, discount_percentage, valid_from, valid_to, active) VALUES ('Liquidación de Temporada', 0.25, '2026-04-01', '2026-04-10', true);

-- POBLAR RESERVAS (bookings)
INSERT INTO bookings (customer_id, travel_package_id, passenger_count, base_price, total_discount, final_amount, status, created_at) VALUES (2, 1, 4, 899.00, 359.60, 3236.40, 'PAID', '2026-04-01 10:00:00');
INSERT INTO bookings (customer_id, travel_package_id, passenger_count, base_price, total_discount, final_amount, status, created_at) VALUES (3, 3, 2, 650.00, 0.00, 1300.00, 'PENDING_PAYMENT', '2026-04-02 12:00:00');
INSERT INTO bookings (customer_id, travel_package_id, passenger_count, base_price, total_discount, final_amount, status, created_at) VALUES (3, 4, 1, 2100.00, 210.00, 1890.00, 'PAID', '2026-04-03 15:30:00');
INSERT INTO bookings (customer_id, travel_package_id, passenger_count, base_price, total_discount, final_amount, status, created_at) VALUES (2, 5, 2, 950.00, 0.00, 1900.00, 'PENDING_PAYMENT', '2026-04-04 09:00:00');
INSERT INTO bookings (customer_id, travel_package_id, passenger_count, base_price, total_discount, final_amount, status, created_at) VALUES (3, 6, 2, 1800.00, 0.00, 3600.00, 'CANCELLED', '2026-03-15 11:20:00');
INSERT INTO bookings (customer_id, travel_package_id, passenger_count, base_price, total_discount, final_amount, status, created_at) VALUES (2, 4, 3, 2100.00, 0.00, 6300.00, 'PAID', '2026-04-02 15:30:00');


-- POBLAR PAGOS (payments)
INSERT INTO payments (booking_id, amount, payment_method, transaction_id, payment_date) VALUES (1, 3236.40, 'Credit Card', 'TXN-778899', '2026-04-01 10:05:00');
INSERT INTO payments (booking_id, amount, payment_method, transaction_id, payment_date) VALUES (3, 1890.00, 'PayPal', 'TXN-994422', '2026-04-03 15:45:00');