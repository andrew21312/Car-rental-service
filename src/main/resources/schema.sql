CREATE DATABASE IF NOT EXISTS car_rental_service;
USE car_rental_service;


DROP TABLE IF EXISTS rental_extra_assignments;
DROP TABLE IF EXISTS rentals;
DROP TABLE IF EXISTS logs;
DROP TABLE IF EXISTS rental_extras;
DROP TABLE IF EXISTS car_issue_reports;
DROP TABLE IF EXISTS cars;
DROP TABLE IF EXISTS car_models;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS rental_statuses;
DROP TABLE IF EXISTS car_statuses;
DROP TABLE IF EXISTS roles;


-- Таблиця ролей
CREATE TABLE roles
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)       NULL
);

-- Таблиця статусів машин
CREATE TABLE car_statuses
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Таблиця статусів оренди
CREATE TABLE rental_statuses
(
    id   INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Таблиця користувачів (адміністратори + клієнти)
CREATE TABLE users
(
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50) UNIQUE NOT NULL,
    first_name    VARCHAR(50)        NOT NULL,
    last_name     VARCHAR(50)        NOT NULL,
    password_hash VARCHAR(255)       NOT NULL,
    role_id       INT                NOT NULL,
    created_at    TIMESTAMP          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    phone_number  VARCHAR(10) UNIQUE NOT NULL CHECK (phone_number REGEXP '^[0-9]{10}$'),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- Таблиця моделей авто
CREATE TABLE car_models
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    price        DECIMAL(10, 2)                                  NOT NULL CHECK ( price > 0 ),
    brand        VARCHAR(50)                                     NOT NULL,
    name         VARCHAR(50)                                     NOT NULL,
    year         YEAR                                            NOT NULL,
    engine_type  ENUM ('PETROL', 'DIESEL', 'ELECTRIC', 'HYBRID') NOT NULL,
    seats        INT                                             NOT NULL CHECK ( seats > 0 ),
    transmission ENUM ('MANUAL', 'AUTOMATIC')                    NOT NULL
);

-- Тригер для перевірки року випуску
DELIMITER $$

CREATE TRIGGER check_car_year
    BEFORE INSERT
    ON car_models
    FOR EACH ROW
BEGIN
    IF NEW.year < 1886 OR NEW.year > YEAR(CURDATE()) + 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid year: must be between 1886 and next year';
    END IF;
END $$

DELIMITER ;


-- Таблиця конкретних автомобілів (екземпляри моделей)
CREATE TABLE cars
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    model_id     INT               NOT NULL,
    plate_number VARCHAR(8) UNIQUE NOT NULL,
    color        VARCHAR(50)       NOT NULL,
    status_id    INT               NOT NULL,
    FOREIGN KEY (model_id) REFERENCES car_models (id) ON DELETE CASCADE,
    FOREIGN KEY (status_id) REFERENCES car_statuses (id)
);

CREATE TABLE car_issue_reports
(
    id          INT PRIMARY KEY AUTO_INCREMENT,
    car_id      INT                                            NOT NULL,
    message     TEXT                                           NOT NULL,
    created_at  TIMESTAMP                    DEFAULT CURRENT_TIMESTAMP,
    status      ENUM ('PENDING', 'RESOLVED') DEFAULT 'PENDING' NOT NULL,
    resolved_at TIMESTAMP                                      NULL,
    FOREIGN KEY (car_id) REFERENCES cars (id) ON DELETE CASCADE
);

-- Таблиця послуг
CREATE TABLE rental_extras
(
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(50)    NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK ( price > 0)
);

-- Таблиця оренд
CREATE TABLE rentals
(
    id             INT AUTO_INCREMENT PRIMARY KEY,
    client_id      INT,
    car_id         INT,
    -- ----------------Snapshot--------------- --
    plate_number   VARCHAR(8),                              -- Номерний знак авто на момент оренди
    car_model_name VARCHAR(50),                             -- Назва моделі на момент оренди
    car_brand      VARCHAR(50),                             -- Виробник на момент оренди
    car_year       YEAR,                                    -- Рік випуску на момент оренди
    daily_rate     DECIMAL(10, 2) CHECK ( daily_rate > 0 ), -- Добова ставка на момент оренди
    -- ---------------------------------------- --
    start_date     DATE           NOT NULL,
    end_date       DATE           NOT NULL,
    total_cost     DECIMAL(10, 2) NOT NULL CHECK ( total_cost > 0 ),
    payment_status ENUM ('PENDING','PAID','CANCELLED') DEFAULT 'PENDING',
    status_id      INT            NOT NULL,
    created_at     TIMESTAMP                           DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP                           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (car_id) REFERENCES cars (id) ON DELETE SET NULL,
    FOREIGN KEY (status_id) REFERENCES rental_statuses (id)
);

-- Таблиця прив'язки оренди до послуг
CREATE TABLE rental_extra_assignments
(
    rental_id   INT NOT NULL,
    extra_id    INT NOT NULL,
    -- ----------------Snapshot--------------- --
    extra_name  VARCHAR(50),                              -- Назва послуги на момент оренди
    extra_price DECIMAL(10, 2) CHECK ( extra_price > 0 ), -- Ціна послуги на момент оренди
    -- ---------------------------------------- --
    PRIMARY KEY (rental_id, extra_id),
    FOREIGN KEY (rental_id) REFERENCES rentals (id) ON DELETE CASCADE,
    FOREIGN KEY (extra_id) REFERENCES rental_extras (id) ON DELETE CASCADE
);

-- Таблиця логів
CREATE TABLE logs
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id    INT,
    event_type VARCHAR(50) NOT NULL,
    text       TEXT        NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

-- Заповнення таблиці ролей (roles)
INSERT INTO roles (name, description)
VALUES ('SUPER_ADMIN', 'Super Admin'),
       ('ADMIN', 'Administrator'),
       ('CLIENT', 'Client'),
       ('FLEET_MANAGER', 'Fleet Manager');

-- Заповнення таблиці статусів машин (car_statuses)
INSERT INTO car_statuses (name)
VALUES ('AVAILABLE'),
       ('MAINTENANCE');

-- Заповнення таблиці статусів оренди (rental_statuses)
INSERT INTO rental_statuses (name)
VALUES ('PENDING'),
       ('APPROVED'),
       ('READY_FOR_PICKUP'),
       ('CANCELLED'),
       ('ACTIVE'),
       ('COMPLETED'),
       ('REJECTED_TECHNICAL');

-- Заповнення таблиці моделей автомобілів (car_models)
INSERT INTO car_models (price, brand, name, year, engine_type, seats, transmission)
VALUES (35, 'Toyota', 'Corolla', 2020, 'PETROL', 4, 'AUTOMATIC'),
       (30, 'Honda', 'Civic', 2019, 'PETROL', 5, 'MANUAL'),
       (100, 'Tesla', 'Model 3', 2023, 'ELECTRIC', 5, 'AUTOMATIC'),
       (45, 'Ford', 'Mustang', 2021, 'PETROL', 5, 'AUTOMATIC'),
       (50, 'BMW', '3 Series', 2022, 'PETROL', 5, 'AUTOMATIC'),
       (55, 'Mercedes-Benz', 'C-Class', 2023, 'PETROL', 5, 'AUTOMATIC'),
       (70, 'Tesla', 'Model S', 2023, 'ELECTRIC', 5, 'AUTOMATIC'),
       (75, 'Tesla', 'Model X', 2023, 'ELECTRIC', 5, 'AUTOMATIC'),
       (80, 'Tesla', 'Model Y', 2023, 'ELECTRIC', 5, 'AUTOMATIC'),
       (60, 'Audi', 'A4', 2023, 'DIESEL', 5, 'AUTOMATIC'),
       (65, 'Audi', 'Q7', 2023, 'DIESEL', 5, 'AUTOMATIC'),
       (55, 'Audi', 'RS7', 2023, 'DIESEL', 5, 'AUTOMATIC'),
       (28, 'Hyundai', 'Elantra', 2021, 'PETROL', 5, 'AUTOMATIC'),
       (40, 'Kia', 'Sorento', 2022, 'DIESEL', 7, 'AUTOMATIC'),
       (55, 'Volkswagen', 'Golf', 2023, 'PETROL', 5, 'MANUAL'),
       (90, 'Porsche', 'Taycan', 2023, 'ELECTRIC', 4, 'AUTOMATIC'),
       (20, 'Renault', 'Clio', 2020, 'PETROL', 5, 'MANUAL'),
       (65, 'Volvo', 'XC90', 2022, 'HYBRID', 7, 'AUTOMATIC'),
       (38, 'Mazda', 'CX-5', 2021, 'PETROL', 5, 'AUTOMATIC'),
       (25, 'Ford', 'Focus', 2020, 'DIESEL', 5, 'MANUAL'),
       (45, 'Subaru', 'Outback', 2022, 'PETROL', 5, 'AUTOMATIC'),
       (30, 'Nissan', 'Leaf', 2023, 'ELECTRIC', 5, 'AUTOMATIC'),
       (75, 'Jeep', 'Wrangler', 2022, 'PETROL', 5, 'MANUAL'),
       (50, 'Chevrolet', 'Tahoe', 2021, 'PETROL', 7, 'AUTOMATIC'),
       (55, 'Honda', 'CR-V', 2023, 'HYBRID', 5, 'AUTOMATIC');


-- Заповнення таблиці автомобілів (cars)
INSERT INTO cars (model_id, plate_number, color, status_id)

VALUES
    -- Toyota Corolla (model_id = 1)
    (1, 'ABC1234', 'Black', 1),
    (1, 'ABC5678', 'Silver', 1),
    (1, 'ABC9012', 'White', 1),

    -- Honda Civic (model_id = 2)
    (2, 'XYZ1234', 'Blue', 1),
    (2, 'XYZ5678', 'Red', 1),
    (2, 'XYZ9012', 'White', 1),

    -- Tesla Model 3 (model_id = 3)
    (3, 'TES1234', 'White', 1),
    (3, 'TES5678', 'Black', 1),
    (3, 'TES9012', 'Red', 1),

    -- Ford Mustang (model_id = 4)
    (4, 'FDR1234', 'Red', 1),
    (4, 'FDR5678', 'Blue', 1),
    (4, 'FDR9012', 'Black', 1),

    -- BMW 3 Series (model_id = 5)
    (5, 'BMW1234', 'Grey', 1),
    (5, 'BMW5678', 'White', 1),
    (5, 'BMW9012', 'Black', 1),

    -- Mercedes-Benz C-Class (model_id = 6)
    (6, 'MBC1234', 'Black', 1),
    (6, 'MBC5678', 'Silver', 1),
    (6, 'MBC9012', 'White', 1),

    -- Tesla Model S (model_id = 7)
    (7, 'TSS1234', 'White', 1),
    (7, 'TSS5678', 'Black', 1),
    (7, 'TSS9012', 'Red', 1),

    -- Tesla Model X (model_id = 8)
    (8, 'TSX1234', 'White', 1),
    (8, 'TSX5678', 'Black', 1),
    (8, 'TSX9012', 'Blue', 1),

    -- Tesla Model Y (model_id = 9)
    (9, 'TSY1234', 'White', 1),
    (9, 'TSY5678', 'Black', 1),
    (9, 'TSY9012', 'Blue', 1),

    -- Audi A4 (model_id = 10)
    (10, 'AUD1234', 'Black', 1),
    (10, 'AUD5678', 'Silver', 1),
    (10, 'AUD9012', 'White', 1),

    -- Audi Q7 (model_id = 11)
    (11, 'AUQ1234', 'Black', 1),
    (11, 'AUQ5678', 'Grey', 1),
    (11, 'AUQ9012', 'White', 1),

    -- Audi RS7 (model_id = 12)
    (12, 'AUR1234', 'Black', 1),
    (12, 'AUR5678', 'Blue', 1),
    (12, 'AUR9012', 'White', 1),

    -- Hyundai Elantra (model_id = 13)
    (13, 'HYU1234', 'Blue', 1),
    (13, 'HYU5678', 'White', 1),
    (13, 'HYU9012', 'Gray', 1),

    -- Kia Sorento (model_id = 14)
    (14, 'KIA1234', 'Black', 1),
    (14, 'KIA5678', 'Red', 1),
    (14, 'KIA9012', 'Silver', 1),

    -- Volkswagen Golf (model_id = 15)
    (15, 'VWG1234', 'White', 1),
    (15, 'VWG5678', 'Blue', 1),
    (15, 'VWG9012', 'Black', 1),

    -- Porsche Taycan (model_id = 16)
    (16, 'PCH1234', 'White', 1),
    (16, 'PCH5678', 'Red', 1),
    (16, 'PCH9012', 'Silver', 1),

    -- Renault Clio (model_id = 17)
    (17, 'RNO1234', 'Yellow', 1),
    (17, 'RNO5678', 'Blue', 1),
    (17, 'RNO9012', 'White', 1),

    -- Volvo XC90 (model_id = 18)
    (18, 'VOL1234', 'Black', 1),
    (18, 'VOL5678', 'Silver', 1),
    (18, 'VOL9012', 'White', 1),

    -- Mazda CX-5 (model_id = 19)
    (19, 'MZD1234', 'Red', 1),
    (19, 'MZD5678', 'White', 1),
    (19, 'MZD9012', 'Blue', 1),

    -- Ford Focus (model_id = 20)
    (20, 'FDF1234', 'Gray', 1),
    (20, 'FDF5678', 'Black', 1),
    (20, 'FDF9012', 'White', 1),

    -- Subaru Outback (model_id = 21)
    (21, 'SUB1234', 'Green', 1),
    (21, 'SUB5678', 'Black', 1),
    (21, 'SUB9012', 'Silver', 1),

    -- Nissan Leaf (model_id = 22)
    (22, 'NSN1234', 'White', 1),
    (22, 'NSN5678', 'Blue', 1),
    (22, 'NSN9012', 'Black', 1),

    -- Jeep Wrangler (model_id = 23)
    (23, 'JWP1234', 'Green', 1),
    (23, 'JWP5678', 'Black', 1),
    (23, 'JWP9012', 'Orange', 1),

    -- Chevrolet Tahoe (model_id = 24)
    (24, 'CHT1234', 'Black', 1),
    (24, 'CHT5678', 'White', 1),
    (24, 'CHT9012', 'Silver', 1),

    -- Honda CR-V (model_id = 25)
    (25, 'HCR1234', 'Blue', 1),
    (25, 'HCR5678', 'White', 1),
    (25, 'HCR9012', 'Grey', 1);

-- Заповнення таблиці користувачів (users)
INSERT INTO users (username, first_name, last_name, password_hash, role_id, phone_number)
VALUES ('sadmin', 'Super', 'Admin', '123', 1, '1111111111'),
       ('admin', 'Admin', 'Admin', '123', 2, '2222222222'),
       ('client', 'Client1', 'Client1', '123', 3, '3333333333'),
       ('client2', 'Client2', 'Client2', '123', 3, '4444444444'),
       ('client3', 'Client3', 'Client3', '123', 3, '5555555555'),
       ('client4', 'Client4', 'Client4', '123', 3, '6666666666'),
       ('fleet_manager', 'Fleet', 'Manager', '123', 4, '7777777777'),

       -- Додані користувачі
       ('jsmith', 'John', 'Smith', '123', 3, '5012345678'),
       ('mjohnson', 'Michael', 'Johnson', '123', 3, '5012345688'),
       ('rwilliams', 'Robert', 'Williams', '123', 3, '5012345699'),
       ('djones', 'David', 'Jones', '123', 3, '5012345700'),
       ('mgarcia', 'Maria', 'Garcia', '123', 3, '5012345711'),
       ('bmiller', 'Brian', 'Miller', '123', 3, '5012345722'),
       ('edavis', 'Emily', 'Davis', '123', 3, '5012345733'),
       ('jwilson', 'James', 'Wilson', '123', 3, '5012345744'),
       ('nanderson', 'Noah', 'Anderson', '123', 3, '5012345755'),
       ('vtaylor', 'Victoria', 'Taylor', '123', 3, '5012345766');

-- Заповнення таблиці послуг (rental_extras)
INSERT INTO rental_extras (name, price)
VALUES ('GPS', 5.00),
       ('Child Seat', 5.00),
       ('Phone charger/holder', 5.00),
       ('Extra Baggage', 30.00);

-- Запис проблем для автомобілів
INSERT INTO car_issue_reports (car_id, message, status)
VALUES
-- Проблеми з автомобілем Toyota Corolla
(1, 'Engine malfunction. Needs immediate repair.', 'PENDING'),

-- Проблеми з автомобілем Tesla Model 3
(3, 'Flat tire, needs to be replaced.', 'PENDING'),

-- Проблеми з автомобілем Ford Mustang
(4, 'Brake system failure. Should be inspected immediately.', 'PENDING'),

-- Проблеми з Audi RS7
(12, 'Suspension issues, not safe to drive.', 'PENDING');

-- Зміна статусів автомобілів
UPDATE cars
SET status_id = 2
WHERE id IN (1, 3, 4, 12);


-- Тестові записи оренди з різними статусами
INSERT INTO rentals (client_id, car_id, plate_number, car_model_name, car_brand, car_year, daily_rate,
                     start_date, end_date, total_cost, payment_status, status_id)
VALUES
-- PENDING (1)
(3, 1, 'ABC1234', 'Corolla', 'Toyota', 2020, 35.00, '2025-06-10', '2025-06-12', 70.00, 'PENDING', 1),

-- APPROVED (2)
(4, 2, 'ABC5678', 'Corolla', 'Toyota', 2020, 35.00, '2025-06-11', '2025-06-14', 105.00, 'PAID', 2),

-- READY_FOR_PICKUP (3)
(5, 3, 'ABC9012', 'Corolla', 'Toyota', 2020, 35.00, '2025-06-12', '2025-06-15', 105.00, 'PAID', 3),

-- CANCELLED (4)
(6, 4, 'XYZ1234', 'Civic', 'Honda', 2019, 30.00, '2025-06-13', '2025-06-16', 90.00, 'CANCELLED', 4),

-- ACTIVE (5)
(3, 5, 'XYZ5678', 'Civic', 'Honda', 2019, 30.00, '2025-06-04', '2025-06-10', 180.00, 'PAID', 5),

-- COMPLETED (6)
(4, 6, 'XYZ9012', 'Civic', 'Honda', 2019, 30.00, '2025-05-20', '2025-05-25', 150.00, 'PAID', 6),

-- REJECTED_TECHNICAL (7)
(5, 7, 'TES1234', 'Model 3', 'Tesla', 2023, 100.00, '2025-06-14', '2025-06-18', 400.00, 'PENDING', 7),

-- Додаткові завершені оренди (COMPLETED)
(6, 8, 'TES5678', 'Model 3', 'Tesla', 2023, 100.00, '2025-05-01', '2025-05-05', 400.00, 'PAID', 6),
(3, 9, 'TES9012', 'Model 3', 'Tesla', 2023, 100.00, '2025-04-20', '2025-04-25', 500.00, 'PAID', 6),
(4, 10, 'FDR1234', 'Mustang', 'Ford', 2021, 45.00, '2025-05-10', '2025-05-15', 225.00, 'PAID', 6);

-- Додаткові послуги до оренди
INSERT INTO rental_extra_assignments (rental_id, extra_id, extra_name, extra_price)
VALUES
-- До оренди 6 (перша COMPLETED з попереднього запиту)
(6, 1, 'GPS', 5.00),
(6, 4, 'Extra Baggage', 30.00),

-- До оренди 8
(8, 2, 'Child Seat', 5.00),
(8, 3, 'Phone charger/holder', 5.00),

-- До оренди 9
(9, 1, 'GPS', 5.00),

-- До оренди 10
(10, 4, 'Extra Baggage', 30.00),

-- До оренди 2 (APPROVED)
(2, 2, 'Child Seat', 5.00),

-- До оренди 5 (ACTIVE)
(5, 3, 'Phone charger/holder', 5.00);


