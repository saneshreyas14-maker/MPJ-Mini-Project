-- Create Database
CREATE DATABASE IF NOT EXISTS BankManagementSystem;
USE BankManagementSystem;

-- Customers Table
CREATE TABLE Customers (
    customerId INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Accounts Table
CREATE TABLE Accounts (
    accountId INT PRIMARY KEY AUTO_INCREMENT,
    customerId INT NOT NULL,
    balance DECIMAL(15, 2) DEFAULT 0.0,
    FOREIGN KEY (customerId) REFERENCES Customers(customerId) ON DELETE CASCADE
);

-- Transactions Table
CREATE TABLE Transactions (
    transactionId INT PRIMARY KEY AUTO_INCREMENT,
    accountId INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (accountId) REFERENCES Accounts(accountId) ON DELETE CASCADE
);

-- Sample Data Insertion (Optional but helpful for testing)
INSERT INTO Customers (name, email, phone, password) VALUES 
('Alice Smith', 'alice@example.com', '1234567890', 'password123'),
('Bob Johnson', 'bob@example.com', '0987654321', 'password456');

INSERT INTO Accounts (customerId, balance) VALUES 
(1, 1000.50),
(2, 500.00);

-- Insert sample transactions for Alice (accountId 1) and Bob (accountId 2)
INSERT INTO Transactions (accountId, type, amount) VALUES 
(1, 'Deposit', 1000.50),
(2, 'Deposit', 500.00);
