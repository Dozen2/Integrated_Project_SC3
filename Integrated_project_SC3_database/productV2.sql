-- drop database proj_sc3;
create database if not exists proj_sc3;
use proj_sc3;

create table if not exists seller(
id int primary key auto_increment,
mobileNumber varchar(10) not null,
bankAccountNumber varchar(20) not null,
bankName varchar(50) not null,
nationalId varchar(13) not null,
nationalIdPhotoFront varchar(70) not null,
nationalIdPhotoBack varchar(70) not null
);
 
create table if not exists buyer(
	id int primary key auto_increment,
    nickName varchar(50) not null,
    email varchar(70) not null,
    passwords varchar(100) not null,
    fullName varchar(50) not null,
    seller_id int ,
    isActive boolean,
    foreign key (seller_id) references seller(id)
);
 
CREATE TABLE IF NOT EXISTS Brand (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL UNIQUE CHECK (TRIM(name) <> ''),
    webSiteUrl VARCHAR(40),
    isActive BOOLEAN NOT NULL,
    countryOfOrigin VARCHAR(80),
    createdOn datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  --   updatedOn datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ,
    CHECK (websiteUrl IS NULL OR TRIM(websiteUrl) <> ''),
    CHECK (countryOfOrigin IS NULL OR TRIM(countryOfOrigin) <> '')
);
 
CREATE TABLE IF NOT EXISTS saleItem (
    id INT PRIMARY KEY AUTO_INCREMENT,
    brand_id INT NOT NULL,
    model VARCHAR(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL CHECK (TRIM(model) <> ''),
    description TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL CHECK (TRIM(description) <> ''),
    quantity INT NOT NULL DEFAULT 1,
    price INT NOT NULL,
    screenSizeInch DECIMAL(4,2),
    ramGb INT,
    storageGb int,
    color VARCHAR(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    seller_id int not null,
    createdOn datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (brand_id) REFERENCES Brand(id),
    foreign key (seller_id) references seller(id)
);
 
CREATE TABLE IF NOT EXISTS saleItemImage (
    id INT PRIMARY KEY AUTO_INCREMENT,
    fileName VARCHAR(70) NOT NULL UNIQUE CHECK (TRIM(fileName) <> ''),
    imageViewOrder INT ,
    originalFileName VARCHAR(50),
    saleItem_id int not null,
	FOREIGN KEY (saleItem_id) REFERENCES saleItem(id)
);
 
create or replace view storageGb_view as 
select distinct p.storageGb 
from saleItem p;
 
CREATE TABLE buyer_roles (
    buyer_id INT NOT NULL,
    role VARCHAR(50) NOT NULL,
    FOREIGN KEY (buyer_id) REFERENCES buyer(id)
);
 
create table if not exists verifyToken(
id int primary key auto_increment,
verify_token varchar(100),
buyer_id int not null,
expiredDate datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
foreign key (buyer_id) references buyer(id)
);
 
INSERT INTO Brand (id, name, countryOfOrigin, webSiteUrl, isActive) VALUES
(1, 'Samsung', 'South Korea', 'https://www.samsung.com', 1),
(2, 'Apple', 'United States', 'https://www.apple.com', 1),
(3, 'Xiaomi', 'China', 'https://www.mi.com', 1),
(4, 'Huawei', 'China', 'https://www.huawei.com', 1),
(5, 'OnePlus', 'China', 'https://www.oneplus.com', 1),
(6, 'Sony', 'Japan', 'https://www.sony.com', 1),
(7, 'LG', 'South Korea', 'https://www.lg.com', 1),
(8, 'Nokia', 'Finland', 'https://www.nokia.com', 0),
(9, 'Motorola', 'United States', 'https://www.motorola.com', 0),
(10, 'OPPO', 'China', 'https://www.oppo.com', 1),
(11, 'Vivo', 'China', 'https://www.vivo.com', 1),
(12, 'ASUS', 'Taiwan', 'https://www.asus.com', 1),
(13, 'Google', 'United States', 'https://store.google.com', 1),
(14, 'Realme', 'China', 'https://www.realme.com', 1),
(15, 'BlackBerry', 'Canada', 'https://www.blackberry.com', 1),
(16, 'HTC', 'Taiwan', 'https://www.htc.com', 1),
(17, 'ZTE', 'China', 'https://www.zte.com', 1),
(18, 'Lenovo', 'China', 'https://www.lenovo.com', 1),
(19, 'Honor', 'China', 'https://www.hihonor.com', 1),
(20, 'Nothing', 'United Kingdom', 'https://nothing.tech', 1);
