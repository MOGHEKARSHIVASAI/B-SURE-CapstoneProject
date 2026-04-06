-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: localhost    Database: binsurance_db
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `businesses`
--

DROP TABLE IF EXISTS `businesses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `businesses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_line1` varchar(255) DEFAULT NULL,
  `address_line2` varchar(255) DEFAULT NULL,
  `annual_revenue` decimal(15,2) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `company_name` varchar(255) NOT NULL,
  `company_reg_number` varchar(255) DEFAULT NULL,
  `country` varchar(100) NOT NULL DEFAULT 'India',
  `created_at` datetime(6) NOT NULL,
  `industry_type` varchar(255) DEFAULT NULL,
  `num_employees` int DEFAULT NULL,
  `postal_code` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `tax_id` varchar(255) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKe1hbub5c2on5poeuoepvtfyx4` (`company_reg_number`),
  UNIQUE KEY `UK3yalgsx72iwdxg4v75n64osdv` (`tax_id`),
  KEY `FKg8wf081dyjc8mwodmg5mairv6` (`user_id`),
  CONSTRAINT `FKg8wf081dyjc8mwodmg5mairv6` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `claims`
--

DROP TABLE IF EXISTS `claims`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `claims` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `approved_amount` decimal(15,2) DEFAULT NULL,
  `claim_date` date NOT NULL,
  `claim_number` varchar(255) NOT NULL,
  `claimed_amount` decimal(15,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `incident_date` date NOT NULL,
  `incident_description` text NOT NULL,
  `rejection_reason` text,
  `settled_amount` decimal(15,2) DEFAULT NULL,
  `settlement_date` date DEFAULT NULL,
  `status` enum('APPEALED','APPROVED','ASSIGNED','DRAFT','REJECTED','SETTLED','SUBMITTED','UNDER_INVESTIGATION') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `assigned_officer_id` bigint DEFAULT NULL,
  `business_id` bigint NOT NULL,
  `policy_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8prfn2h4t4bpdy5s6lonblk7m` (`claim_number`),
  KEY `FKo1og2qs5egfscchtbej8xq765` (`assigned_officer_id`),
  KEY `FK7kg8iwei4tt7nfyas9ls2ktww` (`business_id`),
  KEY `FKm0w2xffwe13pmkusoxnxuim7j` (`policy_id`),
  CONSTRAINT `FK7kg8iwei4tt7nfyas9ls2ktww` FOREIGN KEY (`business_id`) REFERENCES `businesses` (`id`),
  CONSTRAINT `FKm0w2xffwe13pmkusoxnxuim7j` FOREIGN KEY (`policy_id`) REFERENCES `policies` (`id`),
  CONSTRAINT `FKo1og2qs5egfscchtbej8xq765` FOREIGN KEY (`assigned_officer_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `documents`
--

DROP TABLE IF EXISTS `documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `documents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `document_type` enum('ASSET_PROOF','BUSINESS_PROOF','CLAIM_PHOTO','GST_CERTIFICATE','OTHER','POLICE_REPORT','PREVIOUS_INSURANCE','REPAIR_BILL','RISK_PHOTO') NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(255) NOT NULL,
  `file_type` varchar(255) NOT NULL,
  `uploaded_at` datetime(6) NOT NULL,
  `application_id` bigint DEFAULT NULL,
  `claim_id` bigint DEFAULT NULL,
  `uploaded_by_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbehf0ouj6b4lg75ymmd6pofg8` (`application_id`),
  KEY `FKt96pcgmfm72vpvltr4aa836lh` (`claim_id`),
  KEY `FKa8xyugvg1b1gjr07r5057pn5` (`uploaded_by_id`),
  CONSTRAINT `FKa8xyugvg1b1gjr07r5057pn5` FOREIGN KEY (`uploaded_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKbehf0ouj6b4lg75ymmd6pofg8` FOREIGN KEY (`application_id`) REFERENCES `policy_applications` (`id`),
  CONSTRAINT `FKt96pcgmfm72vpvltr4aa836lh` FOREIGN KEY (`claim_id`) REFERENCES `claims` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `insurance_products`
--

DROP TABLE IF EXISTS `insurance_products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `insurance_products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `base_premium_rate` decimal(5,4) NOT NULL,
  `category` enum('CYBER','HEALTH','LIABILITY','MARINE','PROPERTY','VEHICLE') NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `description` text,
  `is_active` bit(1) NOT NULL,
  `max_coverage_amount` decimal(15,2) DEFAULT NULL,
  `min_coverage_amount` decimal(15,2) DEFAULT NULL,
  `product_code` varchar(255) NOT NULL,
  `product_name` varchar(255) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `created_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKcu5j8u1c1s5cp5ou9an1kbpdi` (`product_code`),
  KEY `FKf04n3ic83xjbme1euycdpo0gc` (`created_by`),
  CONSTRAINT `FKf04n3ic83xjbme1euycdpo0gc` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `is_read` bit(1) NOT NULL,
  `message` text NOT NULL,
  `read_at` datetime(6) DEFAULT NULL,
  `reference_id` bigint DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `type` enum('APPLICATION_APPROVED','APPLICATION_ASSIGNED_TO_UNDERWRITER','APPLICATION_REJECTED','APPLICATION_SUBMITTED','APPLICATION_UNDER_REVIEW','CLAIM_APPEALED','CLAIM_APPROVED','CLAIM_ASSIGNED_TO_OFFICER','CLAIM_REJECTED','CLAIM_SETTLED','CLAIM_SUBMITTED','CLAIM_UNDER_INVESTIGATION','CUSTOMER_ACCEPTED_DECISION','CUSTOMER_REJECTED_DECISION','GENERAL','PAYMENT_PENDING','PAYMENT_RECEIVED','POLICY_CANCELLED','POLICY_EXPIRING_SOON','POLICY_ISSUED','POLICY_REACTIVATED','POLICY_SUSPENDED') NOT NULL,
  `recipient_id` bigint NOT NULL,
  `triggered_by_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqqnsjxlwleyjbxlmm213jaj3f` (`recipient_id`),
  KEY `FK1icr20ycufke3xxtulnl4iawx` (`triggered_by_id`),
  CONSTRAINT `FK1icr20ycufke3xxtulnl4iawx` FOREIGN KEY (`triggered_by_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqqnsjxlwleyjbxlmm213jaj3f` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `policies`
--

DROP TABLE IF EXISTS `policies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `policies` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `annual_premium` decimal(10,2) NOT NULL,
  `cancellation_reason` text,
  `cancelled_at` datetime(6) DEFAULT NULL,
  `coverage_amount` decimal(15,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `deductible` decimal(10,2) DEFAULT NULL,
  `end_date` date NOT NULL,
  `issued_at` datetime(6) NOT NULL,
  `policy_document_url` varchar(255) DEFAULT NULL,
  `policy_number` varchar(255) NOT NULL,
  `start_date` date NOT NULL,
  `status` enum('ACTIVE','CANCELLED','EXPIRED','SUSPENDED') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `application_id` bigint DEFAULT NULL,
  `business_id` bigint NOT NULL,
  `product_id` bigint DEFAULT NULL,
  `underwriter_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKoa74bk3bbln2o1hgik4b93rp9` (`policy_number`),
  UNIQUE KEY `UK1am58mf4krpoqv9vfqui2ifx` (`application_id`),
  KEY `FKqb1sc4icfry5cxojdqo0xcaln` (`business_id`),
  KEY `FKfndw3jbmmcj0gkwxdasrit85o` (`product_id`),
  KEY `FKk5vthoy1tyknyp2jy50mk3e39` (`underwriter_id`),
  CONSTRAINT `FKfndw3jbmmcj0gkwxdasrit85o` FOREIGN KEY (`product_id`) REFERENCES `insurance_products` (`id`),
  CONSTRAINT `FKk5vthoy1tyknyp2jy50mk3e39` FOREIGN KEY (`underwriter_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKko1k4mhd5whrft02fdksujoy8` FOREIGN KEY (`application_id`) REFERENCES `policy_applications` (`id`),
  CONSTRAINT `FKqb1sc4icfry5cxojdqo0xcaln` FOREIGN KEY (`business_id`) REFERENCES `businesses` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `policy_applications`
--

DROP TABLE IF EXISTS `policy_applications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `policy_applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `annual_premium` decimal(15,2) DEFAULT NULL,
  `coverage_amount` decimal(15,2) NOT NULL,
  `coverage_end_date` date NOT NULL,
  `coverage_start_date` date NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `premium_adjustment_pct` decimal(5,2) DEFAULT NULL,
  `reviewed_at` datetime(6) DEFAULT NULL,
  `risk_notes` text,
  `status` enum('APPROVED','CUSTOMER_ACCEPTED','CUSTOMER_REJECTED','DRAFT','POLICY_ISSUED','REJECTED','SUBMITTED','UNDER_REVIEW') NOT NULL,
  `submitted_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `assigned_underwriter_id` bigint DEFAULT NULL,
  `business_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKa46m68aenm21jielp0capqrg4` (`assigned_underwriter_id`),
  KEY `FKim018jlfbsl9o01xtlvbhv89q` (`business_id`),
  KEY `FK6fndvlne6fbj6e63qmfht9w2y` (`product_id`),
  CONSTRAINT `FK6fndvlne6fbj6e63qmfht9w2y` FOREIGN KEY (`product_id`) REFERENCES `insurance_products` (`id`),
  CONSTRAINT `FKa46m68aenm21jielp0capqrg4` FOREIGN KEY (`assigned_underwriter_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKim018jlfbsl9o01xtlvbhv89q` FOREIGN KEY (`business_id`) REFERENCES `businesses` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `premium_payments`
--

DROP TABLE IF EXISTS `premium_payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `premium_payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(15,2) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `paid_at` datetime(6) DEFAULT NULL,
  `payment_reference` varchar(255) NOT NULL,
  `remarks` text,
  `status` enum('FAILED','PAID','PENDING','REFUNDED') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `application_id` bigint NOT NULL,
  `business_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK1guv9vcggdddamhx1mv6naj7d` (`payment_reference`),
  KEY `FKlvt76ldimcxg3elf2lync14he` (`application_id`),
  KEY `FKhbj21kt19xhx73vk11huvpvmt` (`business_id`),
  CONSTRAINT `FKhbj21kt19xhx73vk11huvpvmt` FOREIGN KEY (`business_id`) REFERENCES `businesses` (`id`),
  CONSTRAINT `FKlvt76ldimcxg3elf2lync14he` FOREIGN KEY (`application_id`) REFERENCES `policy_applications` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `underwriter_decisions`
--

DROP TABLE IF EXISTS `underwriter_decisions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `underwriter_decisions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comments` text,
  `decided_at` datetime(6) NOT NULL,
  `decision` enum('APPROVED','REFER_TO_SENIOR','REJECTED') NOT NULL,
  `premium_adjustment_pct` decimal(5,2) DEFAULT NULL,
  `risk_score` int DEFAULT NULL,
  `application_id` bigint NOT NULL,
  `underwriter_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1uenhr32x1wn7a5s2h91v9m0p` (`application_id`),
  KEY `FK9av2jif2gtxealffmutll3ol5` (`underwriter_id`),
  CONSTRAINT `FK1uenhr32x1wn7a5s2h91v9m0p` FOREIGN KEY (`application_id`) REFERENCES `policy_applications` (`id`),
  CONSTRAINT `FK9av2jif2gtxealffmutll3ol5` FOREIGN KEY (`underwriter_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(255) NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `is_active` bit(1) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `role` enum('ADMIN','CLAIMS_OFFICER','CUSTOMER','UNDERWRITER') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-16 21:45:00
