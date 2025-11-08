-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 08, 2025 at 08:02 PM
-- Server version: 10.11.14-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `odh_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `activity_logs`
--

CREATE TABLE `activity_logs` (
  `log_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `action_type` varchar(50) NOT NULL,
  `action_details` varchar(500) DEFAULT NULL,
  `timestamp` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Dumping data for table `activity_logs`
--

INSERT INTO `activity_logs` (`log_id`, `user_id`, `action_type`, `action_details`, `timestamp`) VALUES
(8, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 15:37:27'),
(9, 2, 'UPLOAD', 'Uploaded document: markinggg', '2025-11-05 15:38:29'),
(10, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 15:40:07'),
(11, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 16:03:21'),
(12, NULL, 'LOGIN_FAILED', 'Failed login attempt for: hh', '2025-11-05 17:57:18'),
(13, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 17:57:25'),
(14, NULL, 'LOGIN_FAILED', 'Failed login attempt for: hygtf', '2025-11-05 19:01:52'),
(15, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 19:02:10'),
(16, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 19:08:52'),
(17, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 19:22:47'),
(18, 2, 'USER_CREATE_ERROR', 'User creation error: Cannot construct instance of `com.example.offlinedocumenthubserver.User` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)\n at [Source: (String)\"{\"id\":0,\"username\":\"ioi\",\"fullName\":\"ioi poo\",\"password\":\"123\",\"role\":\"student\"}\"; line: 1, column: 2]', '2025-11-05 19:24:02'),
(19, 2, 'USER_UPDATE_ERROR', 'User update error: Cannot construct instance of `com.example.offlinedocumenthubserver.User` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)\n at [Source: (String)\"{\"id\":1,\"username\":\"adminsss\",\"fullName\":\"System Administrator\",\"password\":null,\"role\":\"admin\"}\"; line: 1, column: 2]', '2025-11-05 19:24:52'),
(20, 2, 'UPLOAD', 'Uploaded document: TestMySpeech', '2025-11-05 19:25:27'),
(21, 2, 'USER_DELETE', 'Deleted user ID: 1', '2025-11-05 19:28:00'),
(22, 2, 'USER_CREATE_ERROR', 'User creation error: Cannot construct instance of `com.example.offlinedocumenthubserver.User` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)\n at [Source: (String)\"{\"id\":0,\"username\":\"uygt\",\"fullName\":\"ygr\",\"password\":\"gfc\",\"role\":\"student\"}\"; line: 1, column: 2]', '2025-11-05 19:28:09'),
(23, 2, 'USER_UPDATE_ERROR', 'User update error: Cannot construct instance of `com.example.offlinedocumenthubserver.User` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)\n at [Source: (String)\"{\"id\":3,\"username\":\"niyoi0oo\",\"fullName\":\"niyo\",\"password\":null,\"role\":\"admin\"}\"; line: 1, column: 2]', '2025-11-05 19:28:42'),
(24, 2, 'DELETE', 'Deleted document ID: 1', '2025-11-05 19:30:25'),
(25, 2, 'USER_UPDATE_ERROR', 'User update error: Cannot construct instance of `com.example.offlinedocumenthubserver.User` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)\n at [Source: (String)\"{\"id\":3,\"username\":\"niyoi\",\"fullName\":\"niyo\",\"password\":null,\"role\":\"student\"}\"; line: 1, column: 2]', '2025-11-05 19:31:46'),
(26, NULL, 'LOGIN_FAILED', 'Failed login attempt for: jhg', '2025-11-05 19:32:35'),
(27, NULL, 'REGISTER', 'New user registered: uytr', '2025-11-05 19:32:43'),
(28, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 19:48:38'),
(29, 2, 'USER_CREATE_ERROR', 'User creation error: Cannot construct instance of `com.example.offlinedocumenthubserver.User` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)\n at [Source: (String)\"{\"id\":0,\"username\":\"iiww\",\"fullName\":\"www\",\"password\":\"wwwww\",\"role\":\"student\"}\"; line: 1, column: 2]', '2025-11-05 19:48:52'),
(30, 2, 'USER_UPDATE_ERROR', 'User update error: Cannot construct instance of `com.example.offlinedocumenthubserver.User` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)\n at [Source: (String)\"{\"id\":4,\"username\":\"uytrooo\",\"fullName\":\"uytr\",\"password\":null,\"role\":\"student\"}\"; line: 1, column: 2]', '2025-11-05 19:49:16'),
(31, 2, 'UPLOAD', 'Uploaded document: Appointment', '2025-11-05 19:49:33'),
(32, 2, 'EDIT_ERROR', 'Edit error: Cannot construct instance of `com.example.offlinedocumenthubserver.Document` (no Creators, like default constructor, exist): cannot deserialize from Object value (no delegate- or property-based Creator)\n at [Source: (String)\"{\"docId\":3,\"title\":\"Appointmentooo\",\"filePath\":\"shared_documents\\\\Appointment_20251105_214933.pdf\",\"uploadedBy\":\"niyo\",\"userId\":2,\"uploadDate\":\"2025-11-05\",\"fileSize\":\"92.5 KB\"}\"; line: 1, column: 2]', '2025-11-05 19:54:30'),
(33, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 20:24:01'),
(34, 2, 'USER_CREATE', 'Created user: tt', '2025-11-05 20:24:16'),
(35, 2, 'USER_UPDATE', 'Updated user ID: 5', '2025-11-05 20:24:30'),
(36, 2, 'USER_DELETE', 'Deleted user ID: 5', '2025-11-05 20:24:36'),
(37, 2, 'UPLOAD', 'Uploaded document: Adelphine_MUSABYIMANAA[1]', '2025-11-05 20:24:57'),
(38, 2, 'EDIT_ERROR', 'Edit error: Cannot deserialize value of type `long` from String \"1.3 MB\": not a valid `long` value\n at [Source: (String)\"{\"docId\":4,\"title\":\"musaa\",\"filePath\":\"shared_documents\\\\Adelphine_MUSABYIMANAA_1__20251105_222457.doc\",\"uploadedBy\":\"niyo\",\"userId\":2,\"uploadDate\":\"2025-11-05\",\"fileSize\":\"1.3 MB\"}\"; line: 1, column: 173] (through reference chain: com.example.offlinedocumenthubserver.Document[\"fileSize\"])', '2025-11-05 20:25:11'),
(39, 2, 'DOWNLOAD', 'Downloaded document: Adelphine_MUSABYIMANAA[1]', '2025-11-05 20:25:29'),
(40, 2, 'DELETE', 'Deleted document ID: 4', '2025-11-05 20:26:12'),
(41, 2, 'EDIT_ERROR', 'Edit error: Cannot deserialize value of type `long` from String \"92.5 KB\": not a valid `long` value\n at [Source: (String)\"{\"docId\":3,\"title\":\"Appointmentop\",\"filePath\":\"shared_documents\\\\Appointment_20251105_214933.pdf\",\"uploadedBy\":\"niyo\",\"userId\":2,\"uploadDate\":\"2025-11-05\",\"fileSize\":\"92.5 KB\"}\"; line: 1, column: 167] (through reference chain: com.example.offlinedocumenthubserver.Document[\"fileSize\"])', '2025-11-05 20:26:23'),
(42, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 20:28:05'),
(43, 2, 'USER_UPDATE', 'Updated user ID: 2', '2025-11-05 20:28:20'),
(44, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 20:28:46'),
(45, NULL, 'REGISTER', 'New user registered: kapo', '2025-11-05 20:29:54'),
(46, NULL, 'LOGIN_FAILED', 'Failed login attempt for: kapo', '2025-11-05 20:30:07'),
(47, 6, 'LOGIN', 'User logged in successfully', '2025-11-05 20:30:09'),
(48, 6, 'UPLOAD', 'Uploaded document: Readme', '2025-11-05 20:31:15'),
(49, 6, 'LOGIN', 'User logged in successfully', '2025-11-05 20:36:37'),
(50, 6, 'LOGOUT', 'User logged out', '2025-11-05 20:37:10'),
(51, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 20:37:36'),
(52, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 20:45:59'),
(53, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 20:48:28'),
(54, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 20:51:06'),
(55, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 20:56:00'),
(56, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 21:00:15'),
(57, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 21:05:00'),
(58, 2, 'LOGOUT', 'User logged out', '2025-11-05 21:05:21'),
(59, 6, 'LOGIN', 'User logged in successfully', '2025-11-05 21:05:30'),
(60, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 21:14:44'),
(61, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 21:18:08'),
(62, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 21:19:17'),
(63, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 22:04:59'),
(64, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 22:16:32'),
(65, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 22:34:11'),
(66, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 22:37:21'),
(67, 2, 'LOGIN', 'User logged in successfully', '2025-11-05 22:44:19'),
(68, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 15:22:58'),
(69, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 15:41:56'),
(70, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 15:48:01'),
(71, 2, 'LOGOUT', 'User logged out', '2025-11-06 15:53:05'),
(72, NULL, 'LOGIN_FAILED', 'Failed login attempt for: uhygft', '2025-11-06 15:59:32'),
(73, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 15:59:40'),
(74, 2, 'UPLOAD', 'Uploaded document: project[1] (AutoRecovered)', '2025-11-06 16:00:38'),
(75, 6, 'LOGIN', 'User logged in successfully', '2025-11-06 16:01:26'),
(76, 6, 'DOWNLOAD', 'Downloaded document: project[1] (AutoRecovered)', '2025-11-06 16:01:40'),
(77, 6, 'LOGOUT', 'User logged out', '2025-11-06 16:02:47'),
(78, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 16:02:59'),
(79, 2, 'LOGOUT', 'User logged out', '2025-11-06 17:53:04'),
(80, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 20:02:35'),
(81, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 20:15:46'),
(82, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:15:55'),
(83, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:15:55'),
(84, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:15:57'),
(85, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:15:59'),
(86, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:01'),
(87, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:03'),
(88, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:05'),
(89, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:07'),
(90, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:09'),
(91, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:11'),
(92, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:13'),
(93, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:15'),
(94, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:17'),
(95, 2, 'CONVERSATIONS_LOAD_ERROR', 'Failed to load conversations: Unknown column \'m2.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:19'),
(96, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:20'),
(97, 2, 'MARK_READ_ERROR', 'Mark read error: Unknown column \'receiver_id\' in \'WHERE\'', '2025-11-06 20:16:20'),
(98, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:21'),
(99, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:23'),
(100, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:25'),
(101, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:27'),
(102, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:29'),
(103, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:31'),
(104, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:33'),
(105, 2, 'SEND_MESSAGE_ERROR', 'Message send error: Unknown column \'receiver_id\' in \'INSERT INTO\'', '2025-11-06 20:16:34'),
(106, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:35'),
(107, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:37'),
(108, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:39'),
(109, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:41'),
(110, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:43'),
(111, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:45'),
(112, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:47'),
(113, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:49'),
(114, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:51'),
(115, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:53'),
(116, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:55'),
(117, 2, 'MARK_READ_ERROR', 'Mark read error: Unknown column \'receiver_id\' in \'WHERE\'', '2025-11-06 20:16:55'),
(118, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:55'),
(119, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:57'),
(120, 2, 'SEND_MESSAGE_ERROR', 'Message send error: Unknown column \'receiver_id\' in \'INSERT INTO\'', '2025-11-06 20:16:58'),
(121, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:16:59'),
(122, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:01'),
(123, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:03'),
(124, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:05'),
(125, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:07'),
(126, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:09'),
(127, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:11'),
(128, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:13'),
(129, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:15'),
(130, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:17'),
(131, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:19'),
(132, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:21'),
(133, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:23'),
(134, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:25'),
(135, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:27'),
(136, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:29'),
(137, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:31'),
(138, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:33'),
(139, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:35'),
(140, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:37'),
(141, 2, 'MESSAGES_LOAD_ERROR', 'Failed to load messages: Unknown column \'m.receiver_id\' in \'WHERE\'', '2025-11-06 20:17:39'),
(142, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 20:19:54'),
(143, 2, 'SEND_MESSAGE', 'Sent message to user ID: 6', '2025-11-06 20:20:21'),
(144, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 20:27:23'),
(145, 2, 'SEND_MESSAGE', 'Sent message to user ID: 6', '2025-11-06 20:27:48'),
(146, 2, 'SEND_MESSAGE', 'Sent message to user ID: 6', '2025-11-06 20:28:12'),
(147, 2, 'SEND_MESSAGE', 'Sent message to user ID: 4', '2025-11-06 20:28:38'),
(148, 2, 'LOGOUT', 'User logged out', '2025-11-06 20:28:55'),
(149, 6, 'LOGIN', 'User logged in successfully', '2025-11-06 20:29:07'),
(150, 6, 'SEND_MESSAGE', 'Sent message to user ID: 2', '2025-11-06 20:29:27'),
(151, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 20:32:55'),
(152, 2, 'SEND_MESSAGE', 'Sent message to user ID: 6', '2025-11-06 20:34:03'),
(153, 2, 'SEND_MESSAGE', 'Sent message to user ID: 3', '2025-11-06 20:34:39'),
(154, 2, 'SEND_MESSAGE', 'Sent message to user ID: 6', '2025-11-06 20:34:54'),
(155, 2, 'SEND_MESSAGE', 'Sent message to user ID: 6', '2025-11-06 20:34:56'),
(156, 2, 'LOGIN', 'User logged in successfully', '2025-11-06 20:55:38'),
(157, 2, 'LOGIN', 'User logged in successfully', '2025-11-08 17:44:40'),
(158, 2, 'LOGIN', 'User logged in successfully', '2025-11-08 17:58:42'),
(159, 2, 'LOGIN', 'User logged in successfully', '2025-11-08 18:01:47'),
(160, 2, 'LOGIN', 'User logged in successfully', '2025-11-08 18:09:47'),
(161, 2, 'LOGIN', 'User logged in successfully', '2025-11-08 18:21:46'),
(162, 2, 'LOGIN', 'User logged in successfully', '2025-11-08 18:28:42'),
(163, 2, 'LOGIN', 'User logged in successfully', '2025-11-08 18:30:23'),
(164, 2, 'LOGIN', 'User logged in successfully', '2025-11-08 18:34:19'),
(165, 2, 'LOGIN', 'User logged in successfully', '2025-11-08 18:41:20'),
(166, 2, 'UPLOAD', 'Uploaded document: TrM_SWDDT501_DevOps Techniques Application', '2025-11-08 18:42:15'),
(167, 2, 'USER_UPDATE', 'Updated user ID: 4', '2025-11-08 18:43:03'),
(168, 2, 'USER_UPDATE', 'Updated user ID: 4', '2025-11-08 18:45:06'),
(169, 2, 'LOGOUT', 'User logged out', '2025-11-08 18:51:04'),
(170, 2, 'LOGIN', 'User logged in successfully', '2025-11-08 18:54:00');

-- --------------------------------------------------------

--
-- Table structure for table `documents`
--

CREATE TABLE `documents` (
  `doc_id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `uploaded_by` varchar(100) NOT NULL,
  `upload_date` date NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `file_size` bigint(20) DEFAULT 0,
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Dumping data for table `documents`
--

INSERT INTO `documents` (`doc_id`, `title`, `file_path`, `uploaded_by`, `upload_date`, `user_id`, `file_size`, `created_at`) VALUES
(2, 'TestMySpeech', 'shared_documents\\TestMySpeech_20251105_212527.docx', 'niyo', '2025-11-05', 2, 26232, '2025-11-05 19:25:27'),
(3, 'Appointment', 'shared_documents\\Appointment_20251105_214933.pdf', 'niyo', '2025-11-05', 2, 94702, '2025-11-05 19:49:33'),
(5, 'Readme', 'shared_documents\\Readme_20251105_223115.txt', 'kapo', '2025-11-05', 6, 586, '2025-11-05 20:31:15'),
(6, 'project[1] (AutoRecovered)', 'shared_documents\\project_1___AutoRecovered__20251106_180038.docx', 'niyo', '2025-11-06', 2, 17274, '2025-11-06 16:00:38'),
(7, 'TrM_SWDDT501_DevOps Techniques Application', 'shared_documents\\TrM_SWDDT501_DevOps_Techniques_Application_20251108_204215.pdf', 'HARINDINTWARI Etienne', '2025-11-08', 2, 905257, '2025-11-08 18:42:15');

-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

CREATE TABLE `messages` (
  `message_id` int(11) NOT NULL,
  `sender_id` int(11) NOT NULL,
  `receiver_id` int(11) NOT NULL,
  `message_text` text NOT NULL,
  `sent_date` timestamp NULL DEFAULT current_timestamp(),
  `is_read` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Dumping data for table `messages`
--

INSERT INTO `messages` (`message_id`, `sender_id`, `receiver_id`, `message_text`, `sent_date`, `is_read`) VALUES
(1, 2, 6, 'hello', '2025-11-06 20:20:21', 1),
(2, 2, 6, 'hello wee', '2025-11-06 20:27:47', 1),
(3, 2, 6, 'murahoooo', '2025-11-06 20:28:12', 1),
(4, 2, 4, 'bite wee', '2025-11-06 20:28:38', 0),
(5, 6, 2, 'kk', '2025-11-06 20:29:27', 1),
(6, 2, 6, 'umeze gute? byakunze?', '2025-11-06 20:34:03', 0),
(7, 2, 3, 'salama wowe', '2025-11-06 20:34:39', 0),
(8, 2, 6, 'ijuhygf', '2025-11-06 20:34:54', 0),
(9, 2, 6, 'hgfgcd', '2025-11-06 20:34:56', 0);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `full_name` varchar(100) DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(20) DEFAULT 'student',
  `created_at` timestamp NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `full_name`, `password_hash`, `role`, `created_at`) VALUES
(2, 'niyo', 'HARINDINTWARI Etienne', '3381777', 'admin', '2025-11-05 15:36:49'),
(3, 'jado', 'Iradukunda', '3381777', 'admin', '2025-11-05 15:36:49'),
(4, 'betty', 'Akimpaye Beatrice', '119991', 'student', '2025-11-05 19:32:43'),
(6, 'fre', 'Ntawe Frederic', '3284437', 'student', '2025-11-05 20:29:54');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activity_logs`
--
ALTER TABLE `activity_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `idx_activity_logs_timestamp` (`timestamp`),
  ADD KEY `idx_activity_logs_user_id` (`user_id`),
  ADD KEY `idx_activity_logs_action_type` (`action_type`);

--
-- Indexes for table `documents`
--
ALTER TABLE `documents`
  ADD PRIMARY KEY (`doc_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`message_id`),
  ADD KEY `receiver_id` (`receiver_id`),
  ADD KEY `idx_messages_sender_receiver` (`sender_id`,`receiver_id`),
  ADD KEY `idx_messages_sent_date` (`sent_date`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `activity_logs`
--
ALTER TABLE `activity_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=171;

--
-- AUTO_INCREMENT for table `documents`
--
ALTER TABLE `documents`
  MODIFY `doc_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `messages`
--
ALTER TABLE `messages`
  MODIFY `message_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activity_logs`
--
ALTER TABLE `activity_logs`
  ADD CONSTRAINT `activity_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `documents`
--
ALTER TABLE `documents`
  ADD CONSTRAINT `documents_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`),
  ADD CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`user_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
