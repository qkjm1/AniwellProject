DROP DATABASE IF EXISTS `aniwell`;
CREATE DATABASE `aniwell`;
USE `aniwell`;

-- ✅ 게시판
CREATE TABLE `board` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `regDate` DATETIME NOT NULL,
  `updateDate` DATETIME NOT NULL,
  `code` CHAR(50) NOT NULL UNIQUE COMMENT 'notice, free, qna...',
  `name` CHAR(20) NOT NULL UNIQUE COMMENT '게시판 이름',
  `delStatus` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  `delDate` DATETIME
);

-- ✅ 회원
CREATE TABLE `member` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `regDate` DATETIME NOT NULL,
  `updateDate` DATETIME NOT NULL,
  `loginId` CHAR(30) NOT NULL,
  `loginPw` CHAR(100) NOT NULL,
  `address` TEXT NOT NULL,
  `authLevel` SMALLINT(2) NOT NULL DEFAULT 1 COMMENT '관리자=7, 수의사=3, 일반=1',
  `name` CHAR(20) NOT NULL,
  `nickname` CHAR(20) NOT NULL,
  `cellphone` CHAR(20) NOT NULL,
  `email` CHAR(20) NOT NULL,
  `delStatus` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0,
  `authName` CHAR(30) NOT NULL,
  `delDate` DATETIME
);

-- ✅ 반려동물
CREATE TABLE `pet` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `memberId` INT(10) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `species` ENUM('강아지', '고양이') NOT NULL,
  `breed` VARCHAR(100),
  `gender` ENUM('수컷', '암컷') NOT NULL,
  `birthDate` DATE NOT NULL,
  `weight` DECIMAL(5, 2) NOT NULL,
  `photo` VARCHAR(255)
);

-- ✅ 게시글
CREATE TABLE `article` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `regDate` DATETIME NOT NULL DEFAULT NOW(),
  `updateDate` DATETIME NOT NULL DEFAULT NOW(),
  `title` CHAR(100) NOT NULL,
  `body` TEXT NOT NULL,
  `memberId` INT(10) UNSIGNED NOT NULL,
  `boardId` INT(10) NOT NULL,
  `hitCount` INT(10) UNSIGNED NOT NULL DEFAULT 0,
  `goodReactionPoint` INT(10) UNSIGNED NOT NULL DEFAULT 0,
  `badReactionPoint` INT(10) UNSIGNED NOT NULL DEFAULT 0,
  `repliesCount` INT(10) UNSIGNED NOT NULL DEFAULT 0
);

-- ✅ 댓글
CREATE TABLE `reply` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `regDate` DATETIME NOT NULL,
  `updateDate` DATETIME NOT NULL,
  `memberId` INT(10) UNSIGNED NOT NULL,
  `relTypeCode` CHAR(50) NOT NULL,
  `relId` INT(10) NOT NULL,
  `body` TEXT NOT NULL,
  `goodReactionPoint` INT(10) UNSIGNED NOT NULL DEFAULT 0,
  `badReactionPoint` INT(10) UNSIGNED NOT NULL DEFAULT 0
);

-- ✅ 반려동물 행동 분석
CREATE TABLE `pet_behavior_analysis` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `memberId` INT(10) NOT NULL,
  `petId` INT(10) NOT NULL,
  `analyzedAt` DATETIME NOT NULL,
  `mood` ENUM('행복', '불안', '분노', '슬픔', '놀람') NOT NULL,
  `behaviorLabel` VARCHAR(100) NOT NULL,
  `confidence` DECIMAL(5, 2) NOT NULL,
  `imageUrl` TEXT
);

-- ✅ 반려동물 감정 분석
CREATE TABLE `pet_analysis` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `petId` INT UNSIGNED NOT NULL,
  `imagePath` VARCHAR(255) NOT NULL,
  `emotionResult` VARCHAR(50) NOT NULL,
  `confidence` FLOAT NOT NULL,
  `analyzedAt` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`petId`) REFERENCES `pet`(`id`) ON DELETE CASCADE
);

-- ✅ 반려동물 건강 로그
CREATE TABLE `pet__health_log` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `petId` INT(10) NOT NULL,
  `logDate` DATETIME NOT NULL,
  `foodWeight` DECIMAL(6, 2) NOT NULL,
  `waterWeight` DECIMAL(6, 2) NOT NULL,
  `litterCount` INT(10) NOT NULL,
  `notes` TEXT
);

-- ✅ 산책 모임
CREATE TABLE `walk_crew` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `description` TEXT NOT NULL,
  `district_id` INT NOT NULL,
  `leaderId` INT(10) NOT NULL,
  `createdAt` DATETIME NOT NULL DEFAULT NOW()
);

-- ✅ 산책 모임 멤버
CREATE TABLE `walk_crew_member` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `crewId` INT(10) NOT NULL,
  `memberId` INT(10) NOT NULL,
  `joinedAt` DATETIME NOT NULL
);

-- ✅ 지역 정보
CREATE TABLE `district` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `city` VARCHAR(50) NOT NULL,
  `district` VARCHAR(50) NOT NULL,
  `dong` VARCHAR(50) NOT NULL
);

-- ✅ 백신 접종
CREATE TABLE `pet_vaccination` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `petId` INT(10) NOT NULL,
  `vaccineName` VARCHAR(100),
  `injectionDate` DATE NOT NULL,
  `nextDueDate` DATE,
  `vetName` VARCHAR(100),
  `notes` TEXT
);

-- ✅ 백신 종류 및 주기
CREATE TABLE `vaccine_schedule` (
  `vaccineName` VARCHAR(100) PRIMARY KEY,
  `intervalMonths` INT NOT NULL,
  `type` ENUM('Initial', 'Annual') NOT NULL,
  `description` TEXT
);

-- ✅ 캘린더 이벤트
CREATE TABLE `calendar_event` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `memberId` INT NOT NULL,
  `petId` INT,
  `title` VARCHAR(100) NOT NULL,
  `eventDate` DATE NOT NULL,
  `content` TEXT NOT NULL,
  `createdAt` DATETIME DEFAULT NOW()
);

-- ✅ QnA
CREATE TABLE `qna` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `memberId` INT(10) UNSIGNED NOT NULL,
  `title` VARCHAR(100) NOT NULL,
  `body` TEXT NOT NULL,
  `isSecret` BOOLEAN DEFAULT FALSE,
  `isFromUser` BOOLEAN DEFAULT FALSE,
  `isAnswered` BOOLEAN DEFAULT FALSE,
  `orderNo` INT(10) NOT NULL DEFAULT 0,
  `regDate` DATETIME NOT NULL,
  `updateDate` DATETIME NOT NULL,
  `isActive` BOOLEAN DEFAULT TRUE,
  `isFaq` TINYINT(1) NOT NULL DEFAULT 0
);

-- ✅ 수의사 답변
CREATE TABLE `vet_answer` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `memberId` INT(10) NOT NULL,
  `answer` TEXT,
  `answerAt` DATETIME NOT NULL DEFAULT NOW(),
  `vetName` VARCHAR(100) NOT NULL,
  `qna_id` INT(10) UNSIGNED NOT NULL
);

-- ✅ 추천 장소
CREATE TABLE `pet_recommendation` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `memberId` INT(10) NOT NULL,
  `type` ENUM('병원', '용품') NOT NULL,
  `name` VARCHAR(100) NOT NULL,
  `address` TEXT NOT NULL,
  `phone` VARCHAR(50) NOT NULL,
  `mapUrl` TEXT NOT NULL,
  `createdAt` DATETIME NOT NULL DEFAULT NOW()
);

-- ✅ BLE 활동
CREATE TABLE `pet_ble_activity` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `petId` INT(10) NOT NULL,
  `zoneName` VARCHAR(100) NOT NULL,
  `enteredAt` DATETIME NOT NULL,
  `exitedAt` DATETIME NOT NULL,
  `durationSec` INT NOT NULL,
  `rssi` INT(10) NOT NULL
);

-- ✅ 좋아요/싫어요
CREATE TABLE `reactionPoint` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `regDate` DATETIME NOT NULL,
  `updateDate` DATETIME NOT NULL,
  `memberId` INT(10) UNSIGNED NOT NULL,
  `relTypeCode` CHAR(50) NOT NULL,
  `relId` INT(10) NOT NULL,
  `point` INT(10) NOT NULL
);


############# 🔑 외래 키 제약조건 ###################

-- 🔗 외래키 제약 (선택적으로 사용 가능)
ALTER TABLE `pet` ADD CONSTRAINT `fk_pet_member` FOREIGN KEY (`memberId`) REFERENCES `member` (`id`) ON DELETE CASCADE;

ALTER TABLE `article` ADD CONSTRAINT `fk_article_member` FOREIGN KEY (`memberId`) REFERENCES `member` (`id`) ON DELETE CASCADE;
ALTER TABLE `article` ADD CONSTRAINT `fk_article_board` FOREIGN KEY (`boardId`) REFERENCES `board` (`id`);

ALTER TABLE `reply` ADD CONSTRAINT `fk_reply_member` FOREIGN KEY (`memberId`) REFERENCES `member` (`id`) ON DELETE CASCADE;

ALTER TABLE `pet_behavior_analysis` ADD CONSTRAINT `fk_behavior_pet` FOREIGN KEY (`petId`) REFERENCES `pet` (`id`) ON DELETE CASCADE;

ALTER TABLE `walk_crew` ADD CONSTRAINT `fk_walkcrew_district` FOREIGN KEY (`district_id`) REFERENCES `district` (`id`);

ALTER TABLE `walk_crew_member`
  ADD CONSTRAINT `fk_walkcrew_member_crew` FOREIGN KEY (`crewId`) REFERENCES `walk_crew` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_walkcrew_member_member` FOREIGN KEY (`memberId`) REFERENCES `member` (`id`) ON DELETE CASCADE;


############# 🔑 외래 키 제약조건 ###################

############# 📜 테스트용 코드 ###################

-- ✅ 게시판 샘플
INSERT INTO `board` SET `regDate` = NOW(), `updateDate` = NOW(), `code` = 'notice', `name` = '공지사항';
INSERT INTO `board` SET `regDate` = NOW(), `updateDate` = NOW(), `code` = 'crew', `name` = '크루모집';
INSERT INTO `board` SET `regDate` = NOW(), `updateDate` = NOW(), `code` = 'qna', `name` = '질의응답';

-- ✅ 회원 샘플
INSERT INTO `member`
SET `regDate` = NOW(), `updateDate` = NOW(),
    `loginId` = 'admin', `loginPw` = SHA2('1234', 256), `address` = '서울시 중구',
    `authLevel` = 7, `name` = '관리자', `nickname` = 'admin',
    `cellphone` = '010-1234-5678', `email` = 'admin@example.com',
    `authName` = '관리자';

INSERT INTO `member`
SET `regDate` = NOW(), `updateDate` = NOW(),
    `loginId` = 'vet1', `loginPw` = SHA2('abcd', 256), `address` = '대전시 서구',
    `authLevel` = 3, `name` = '홍수의', `nickname` = '수의사홍',
    `cellphone` = '010-2222-3333', `email` = 'vet@example.com',
    `authName` = '수의사';

INSERT INTO `member`
SET `regDate` = NOW(), `updateDate` = NOW(),
    `loginId` = 'user1', `loginPw` = SHA2('userpw', 256), `address` = '청주시 상당구',
    `authLevel` = 1, `name` = '홍길동', `nickname` = '길동이',
    `cellphone` = '010-9999-8888', `email` = 'user@example.com',
    `authName` = '일반';

-- ✅ 지역 정보
INSERT INTO `district` (`city`, `district`, `dong`) VALUES
('서울특별시', '강남구', '역삼동'),
('대전광역시', '서구', '둔산동'),
('부산광역시', '해운대구', '우동');

-- ✅ 반려동물
INSERT INTO `pet` (`memberId`, `name`, `species`, `breed`, `gender`, `birthDate`, `weight`) VALUES
(1, '콩이', '강아지', '말티즈', '암컷', '2021-05-10', 3.5),
(2, '루비', '고양이', '러시안블루', '암컷', '2020-03-15', 4.2),
(3, '밤비', '강아지', '푸들', '수컷', '2019-11-01', 5.1),
(1, '나비', '고양이', '코리안숏헤어', '수컷', '2022-08-30', 3.8),
(1, '초코', '강아지', '시츄', '암컷', '2018-07-12', 4.0);

-- ✅ 게시글
INSERT INTO `article` (`regDate`, `updateDate`, `memberId`, `boardId`, `title`, `body`) VALUES
(NOW(), NOW(), 1, 1, '강아지 예방접종 중요성', '강아지도 사람처럼 예방접종이 필요합니다.'),
(NOW(), NOW(), 1, 1, '고양이 발정기 대처법', '고양이의 발정기 행동과 대처 방법을 알려드립니다.'),
(NOW(), NOW(), 1, 1, '반려동물과 산책하기 좋은 장소', '서울에서 강아지와 산책하기 좋은 공원 소개.');

-- ✅ QnA
INSERT INTO `qna` (`memberId`, `title`, `body`, `isSecret`, `isFromUser`, `isAnswered`, `orderNo`, `regDate`, `updateDate`, `isActive`)
VALUES
(1, '강아지는 언제부터 예방접종을 시작해야 하나요?', '보통 생후 6~8주부터 시작하며, 이후 매년 추가 접종이 필요합니다.', FALSE, FALSE, TRUE, 1, NOW(), NOW(), TRUE),
(1, '고양이 중성화 수술은 언제 하는 게 좋나요?', '암컷은 생후 6개월 전후, 수컷은 생후 5~6개월에 하는 것이 일반적입니다.', FALSE, FALSE, TRUE, 2, NOW(), NOW(), TRUE),
(1, '강아지가 설사를 자주 하는데 병원에 데려가야 하나요?', '3일 이상 지속되거나 피가 섞이면 병원에 방문해야 합니다.', FALSE, FALSE, TRUE, 3, NOW(), NOW(), TRUE);

-- ✅ 백신 종류 및 주기
INSERT INTO `vaccine_schedule` (`vaccineName`, `intervalMonths`, `type`, `description`) VALUES
('Rabies', 12, 'Initial', '인간에게 감염될 수 있는 치명적인 바이러스 예방'),
('Parvovirus', 12, 'Initial', '파보 바이러스에 의한 위장관 질환 예방'),
('Distemper', 12, 'Initial', '강아지의 심각한 바이러스성 질병 예방'),
('Leptospirosis', 12, 'Annual', '물과 흙을 통해 퍼지는 세균 감염 예방');


############# 📜 테스트용 코드 ###################


############# 💣 트리거 ###################

-- ✅ 백신 자동 계산 트리거
DELIMITER $$

CREATE TRIGGER `auto_set_next_due_date`
BEFORE INSERT ON `pet_vaccination`
FOR EACH ROW
BEGIN
  DECLARE v_interval INT;

  SELECT `intervalMonths` INTO v_interval
  FROM `vaccine_schedule`
  WHERE `vaccineName` = NEW.`vaccineName`
  LIMIT 1;

  IF v_interval IS NOT NULL THEN
    SET NEW.`nextDueDate` = DATE_ADD(NEW.`injectionDate`, INTERVAL v_interval MONTH);
  ELSE
    SET NEW.`nextDueDate` = NULL;
  END IF;
END$$

DELIMITER ;

-- ✅ 댓글 수 자동 증가/감소 트리거
ALTER TABLE `article` ADD COLUMN `repliesCount` INT(10) UNSIGNED NOT NULL DEFAULT 0;

DELIMITER $$

CREATE TRIGGER `trg_reply_count_update`
AFTER INSERT ON `reply`
FOR EACH ROW
BEGIN
  IF NEW.`relTypeCode` = 'article' THEN
    UPDATE `article`
    SET `repliesCount` = `repliesCount` + 1
    WHERE `id` = NEW.`relId`;
  END IF;
END$$

CREATE TRIGGER `trg_reply_count_delete`
AFTER DELETE ON `reply`
FOR EACH ROW
BEGIN
  IF OLD.`relTypeCode` = 'article' THEN
    UPDATE `article`
    SET `repliesCount` = `repliesCount` - 1
    WHERE `id` = OLD.`relId`;
  END IF;
END$$

DELIMITER ;



############# 💣 트리거 ###################