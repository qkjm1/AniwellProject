
DROP DATABASE IF EXISTS `aniwell`;
CREATE DATABASE `aniwell`;
USE `aniwell`;

-- 게시판 테이블
CREATE TABLE `board`
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate    DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    `code`     CHAR(50) NOT NULL UNIQUE COMMENT 'notice(공지사항), free(자유), QnA(질의응답)...',
    `name`     CHAR(20) NOT NULL UNIQUE COMMENT '게시판 이름',
    delStatus  TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '삭제 여부 (0=삭제 전, 1=삭제 후)',
    delDate    DATETIME COMMENT '삭제 날짜'
);

-- 회원 테이블
CREATE TABLE `member`
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate    DATETIME  NOT NULL,
    updateDate DATETIME  NOT NULL,
    loginId    CHAR(30)  NOT NULL,
    loginPw    CHAR(100) NOT NULL,
    address    TEXT      NOT NULL,
    authLevel  SMALLINT(2) NOT NULL DEFAULT 1 COMMENT '관리자 = 7, 회원 = 1, 수의사 = 3',
   `name`     CHAR(20)  NOT NULL,
    nickname   CHAR(20)  NOT NULL,
    cellphone  CHAR(20)  NOT NULL,
    email      CHAR(20)  NOT NULL,
    photo  VARCHAR(255),
    delStatus  TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '탈퇴 여부 (0=탈퇴 전, 1=탈퇴 후)',
    authName   CHAR(30)  NOT NULL COMMENT '일반 또는 수의사',
    delDate    DATETIME COMMENT '탈퇴 날짜'
);

-- 반려동물 행동 분석 테이블
CREATE TABLE pet_behavior_analysis
(
    id            INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId      INT(10) NOT NULL COMMENT 'FK',
    petId         INT(10) NOT NULL COMMENT 'FK',
    analyzedAt    DATETIME      NOT NULL,
    mood          ENUM('행복', '불안', '분노', '슬픔', '놀람') NOT NULL,
    behaviorLabel VARCHAR(100)  NOT NULL COMMENT 'FK',
    confidence    DECIMAL(5, 2) NOT NULL,
    imageUrl      TEXT NULL
);

-- 반려동물 건강 로그 테이블
CREATE TABLE pet__health_log
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    petId       INT(10) NOT NULL COMMENT 'FK',
    logDate     DATETIME      NOT NULL COMMENT '로그 기록',
    foodWeight  DECIMAL(6, 2) NOT NULL,
    waterWeight DECIMAL(6, 2) NOT NULL,
    litterCount INT(10) NOT NULL,
    notes       TEXT NULL
);

-- 산책 모임 멤버 테이블
CREATE TABLE walk_crew_member
(
    id       INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    crewId   INT(10) NOT NULL COMMENT 'PK, FK',
    memberId INT(10) NOT NULL COMMENT 'PK, FK',
    joinedAt DATETIME NOT NULL
);


-- 북마크 테이블
CREATE TABLE bookmark
(
    id        INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId  INT(10) NOT NULL COMMENT 'FK, UNIQUE(with articleId)',
    articleId INT(10) NOT NULL COMMENT 'FK, UNIQUE(with memberId)',
    regDate   DATETIME NOT NULL DEFAULT NOW(),
    UNIQUE (memberId, articleId)
);

-- 수의사 답변 테이블
CREATE TABLE vet_answer
(
    id       INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId INT(10) NOT NULL COMMENT 'FK',
    answer   TEXT NULL,
    answerAt DATETIME     NOT NULL DEFAULT NOW(),
    vetName  VARCHAR(100) NOT NULL
);

ALTER TABLE vet_answer ADD COLUMN qna_id INT(10) UNSIGNED NOT NULL COMMENT '질문 ID (Qna 테이블 FK)';
    
-- 반려동물 추천 장소 테이블
CREATE TABLE pet_recommendation
(
    id        INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId  INT(10) NOT NULL COMMENT 'FK',
    `type`      ENUM('병원', '용품') NOT NULL COMMENT '병원, 용품 등',
    `name`      VARCHAR(100) NOT NULL,
    address   TEXT         NOT NULL,
    phone     VARCHAR(50)  NOT NULL,
    mapUrl    TEXT         NOT NULL COMMENT '카카오 맵 크롤링',
    createdAt DATETIME     NOT NULL DEFAULT NOW()
);

-- 반려동물 예방 접종 테이블
CREATE TABLE pet_vaccination
(
    id            INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    petId         INT(10) NOT NULL COMMENT 'FK',
    vaccineName   VARCHAR(100) NULL,
    injectionDate DATE         NOT NULL,
    nextDueDate   DATE         NULL COMMENT '다음 접종 자동계산 기입(같은 접종이름, 업데이트 되도록)',
    vetName       VARCHAR(100)  NULL,
    notes         TEXT NULL
);

-- 반려동물 테이블
CREATE TABLE pet
(
    id        INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId  INT(10) NOT NULL COMMENT 'FK',
    `name`      VARCHAR(50)   NOT NULL,
    species   ENUM('강아지', '고양이') NOT NULL,
    breed     VARCHAR(100) NULL COMMENT '자식 낳았는지 안 낳았는지',
    gender    ENUM('수컷', '암컷') NOT NULL,
    birthDate DATE          NOT NULL,
    weight    DECIMAL(5, 2) NOT NULL
);

ALTER TABLE pet ADD COLUMN photo VARCHAR(255);


CREATE TABLE pet_analysis (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '분석 결과 ID',
  petId INT UNSIGNED NOT NULL COMMENT '반려동물 ID (FK)',
  imagePath VARCHAR(255) NOT NULL COMMENT '분석에 사용된 이미지 경로 또는 URL',
  emotionResult VARCHAR(50) NOT NULL COMMENT '감정 분석 결과 (예: happy, angry 등)',
  confidence FLOAT NOT NULL COMMENT '분석 결과의 신뢰도 (0.0 ~ 1.0)',
  analyzedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '분석 수행 일시',
  
  CONSTRAINT fk_pet_analysis_petId FOREIGN KEY (petId) REFERENCES pet(id) ON DELETE CASCADE
);

-- 산책 모임 테이블
CREATE TABLE walk_crew
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(100) NOT NULL,
    `description` TEXT         NOT NULL,
    descriptoin TEXT         NOT NULL,
    district_id INT          NOT NULL COMMENT 'FK → district(id)',
    leaderId    INT(10) NOT NULL,
    createdAt   DATETIME     NOT NULL DEFAULT NOW()
);


ALTER TABLE `walk_crew_member`
ADD COLUMN petId INT(10) AFTER memberId;

-- BLE 기반 반려동물 활동 테이블

CREATE TABLE `pet_ble_activity` (
  `id` INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `petId` INT(10) NOT NULL,
  `zoneName` VARCHAR(100) NOT NULL,
  `enteredAt` DATETIME NOT NULL,
  `exitedAt` DATETIME NOT NULL,
  `durationSec` INT NOT NULL,
  `rssi` INT(10) NOT NULL
);



-- 게시글 테이블
CREATE TABLE `article` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '게시글 ID',
  `regDate` DATETIME NOT NULL COMMENT '작성일',
  `updateDate` DATETIME NOT NULL COMMENT '수정일', 
  `crewId` INT(10) UNSIGNED DEFAULT NULL COMMENT '크루 ID (walk_crew 테이블 FK)', 
  `title` VARCHAR(100) NOT NULL COMMENT '제목',
  `body` TEXT NOT NULL COMMENT '내용', 
  `delStatus` TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '삭제 여부',
  `delDate` DATETIME DEFAULT NULL COMMENT '삭제일',
 
  CONSTRAINT `fk_article_crew` FOREIGN KEY (`crewId`) REFERENCES `walk_crew` (`id`) ON DELETE CASCADE
);



-- BLE 기반 반려동물 활동 테이블
CREATE TABLE pet_ble_activity
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    petId       INT(10) NOT NULL COMMENT 'FK',
    zoneName    VARCHAR(100) NOT NULL COMMENT '화장실 , 밥그릇, 물그릇, 침대',
    enteredAt   DATETIME     NOT NULL COMMENT '구역 진입 시간',
    exitedAt    DATETIME     NOT NULL COMMENT '구역 나간 시간',
    durationSec INT          NOT NULL COMMENT '구역 머문 시간',
    rssi        INT(10) NOT NULL
);


-- 게시글 테이블
CREATE TABLE article
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate    DATETIME  NOT NULL DEFAULT NOW(),
    updateDate DATETIME  NOT NULL DEFAULT NOW(),
    title      CHAR(100) NOT NULL,
    `body`     TEXT      NOT NULL
);


--  memberId 추가
ALTER TABLE article
    ADD COLUMN memberId INT(10) UNSIGNED NOT NULL AFTER updateDate;

--  boardId 추가
ALTER TABLE article
    ADD COLUMN boardId INT(10) NOT NULL AFTER `memberId`;

-- hit 추가
ALTER TABLE article
    ADD COLUMN hitCount INT(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `body`;

--  article 테이블에 reactionPoint(좋아요) 컬럼 추가
ALTER TABLE article
    ADD COLUMN goodReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE article
    ADD COLUMN badReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;


-- 지역 정보 테이블
-- 시(city) → 구(district) → 동(dong) 구조의 행정동 정보를 저장
CREATE TABLE district
(
    id       INT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK, 고유 ID',
    city     VARCHAR(50) NOT NULL COMMENT '시/도 이름 (예: 서울특별시, 대전광역시)',
    district VARCHAR(50) NOT NULL COMMENT '구/군 이름 (예: 서구, 중구)',
    dong     VARCHAR(50) NOT NULL COMMENT '동네 이름 (예: 갈마동, 둔산동)'
);


-- 댓글 테이블
CREATE TABLE reply
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate     DATETIME NOT NULL,
    updateDate  DATETIME NOT NULL,
    memberId    INT(10) UNSIGNED NOT NULL,
    relTypeCode CHAR(50) NOT NULL COMMENT '관련 데이터 타입 코드',
    relId       INT(10) NOT NULL COMMENT '관련 데이터 번호',
    `body`      TEXT     NOT NULL
);

-- QnA 테이블
CREATE TABLE Qna
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId   INT(10) UNSIGNED NOT NULL COMMENT '질문 작성자 (회원)',
    title      VARCHAR(100) NOT NULL, -- 질문 제목
    `body`     TEXT         NOT NULL, -- 질문 내용 or 답변 본문
    isSecret   BOOLEAN DEFAULT FALSE, -- 비공개 여부
    isFromUser BOOLEAN DEFAULT FALSE, -- 사용자 등록 질문 여부
    isAnswered BOOLEAN DEFAULT FALSE, -- 관리자 답변 여부
    orderNo    INT(10) NOT NULL DEFAULT 0 COMMENT '출력 순서',
    regDate    DATETIME     NOT NULL,
    updateDate DATETIME     NOT NULL,
    isActive   BOOLEAN DEFAULT TRUE   -- 노출 여부 (숨김 처리 가능)
);

-- 자주 묻는 질문 구분(1= 자주 묻는 질문, 0 = 일반 질문)
ALTER TABLE qna ADD COLUMN isFaq TINYINT(1) NOT NULL DEFAULT 0;
UPDATE Qna SET isSecret = 0 WHERE isSecret NOT IN (0, 1);
UPDATE Qna SET isSecret = 1 WHERE id IN (1, 2, 3); -- 비밀글
UPDATE Qna SET isSecret = 0 WHERE id IN (4, 5, 6, 11); -- 공개글

--  reply 테이블에 좋아요 관련 컬럼 추가
ALTER TABLE reply ADD COLUMN goodReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE reply ADD COLUMN badReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;

-- reactionPoint 테이블 생성
CREATE TABLE reactionPoint
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate     DATETIME NOT NULL,
    updateDate  DATETIME NOT NULL,
    memberId    INT(10) UNSIGNED NOT NULL,
    relTypeCode CHAR(50) NOT NULL COMMENT '관련 데이터 타입 코드',
    relId       INT(10) NOT NULL COMMENT '관련 데이터 번호',
    `point`     INT(10) NOT NULL
);

-- 수의사 인증서 테이블
CREATE TABLE vet_certificate
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId   INT(10) UNSIGNED NOT NULL COMMENT '회원 ID (FK)',
    fileName   VARCHAR(255)     NOT NULL COMMENT '업로드된 원본 파일명',
    filePath   VARCHAR(500)     NOT NULL COMMENT '서버 저장 경로',
    uploadedAt DATETIME         NOT NULL DEFAULT NOW() COMMENT '업로드 일시',
    approved   TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '승인 여부 (0=대기, 1=승인, 2=거절)',

    -- FK 연결
    FOREIGN KEY (memberId) REFERENCES MEMBER(id) ON DELETE CASCADE
);

-- 백신 종류 및 주기 테이블
CREATE TABLE vaccine_schedule (
  vaccineName VARCHAR(100) PRIMARY KEY,
  intervalMonths INT NOT NULL COMMENT '백신 주기 (개월 단위)',
  `type` ENUM('Initial', 'Annual') NOT NULL COMMENT '초기 예방접종 또는 연간 접종 구분',
  DESCRIPTION TEXT NULL
  );
  
  CREATE TABLE calendar_event (
  id INT AUTO_INCREMENT PRIMARY KEY,
  memberId INT NOT NULL,                  
  petId INT NULL,                        
  eventDate DATE NOT NULL,               
  content TEXT NOT NULL,                 
  createdAt DATETIME DEFAULT NOW()       
);

ALTER TABLE calendar_event ADD COLUMN title VARCHAR(100) NOT NULL AFTER petId;



ALTER TABLE walk_crew_member
ADD COLUMN STATUS ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending' COMMENT '승인 상태';

-- ✅ 산책 크루 채팅방
CREATE TABLE `crew_chat_message` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `crewId` INT(10) UNSIGNED NOT NULL,  
  `senderId` INT(10) UNSIGNED NOT NULL,
  `nickname` VARCHAR(100) NOT NULL,
  `content` TEXT NOT NULL,
  `sentAt` DATETIME NOT NULL DEFAULT NOW(),
  FOREIGN KEY (`crewId`) REFERENCES `walk_crew`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`senderId`) REFERENCES `member`(`id`) ON DELETE CASCADE
);

-- ✅ 알림 기능 테이블
CREATE TABLE notification (
id INT(10) AUTO_INCREMENT PRIMARY KEY,
memberId INT(10) NOT NULL,           -- 알림 받는 회원 ID (외래키 가능)
title VARCHAR(255) NOT NULL,      -- 알림 제목
link VARCHAR(255) DEFAULT NULL,   -- 알림 클릭 시 이동할 링크
regDate DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 알림 생성일시
isRead BOOLEAN NOT NULL DEFAULT FALSE  -- 읽음 여부
);

ALTER TABLE notification ADD senderId INT(10) UNSIGNED DEFAULT NULL;


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


-- ✅ 크루
INSERT INTO `walk_crew` (`title`, `description`, `district_id`, `leaderId`, `createdAt`) VALUES
('댕모임', '댕댕이 모임', 1, 1, NOW()),
('강아지사랑', '댕댕이 모임', 2, 2, NOW());


-- ✅ 크루 멤버
INSERT INTO `walk_crew_member` (`memberId`, `crewId`, `joinedAt`) VALUES
(2, 2, NOW()),
(2, 1, NOW()),
(1, 1, NOW());


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


-- ✅ 백신 자동 계산 트리거(insert)

-- ✅ 백신 자동 계산 트리거


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


-- ✅ 백신 자동 계산 트리거(update)
DELIMITER $$

CREATE TRIGGER `auto_set_next_due_date_before_update`
BEFORE UPDATE ON `pet_vaccination`
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


DROP DATABASE IF EXISTS `aniwell`;
CREATE DATABASE `aniwell`;
USE `aniwell`;

-- 게시판 테이블
CREATE TABLE `board`
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate    DATETIME NOT NULL,
    updateDate DATETIME NOT NULL,
    `code`     CHAR(50) NOT NULL UNIQUE COMMENT 'notice(공지사항), free(자유), QnA(질의응답)...',
    `name`     CHAR(20) NOT NULL UNIQUE COMMENT '게시판 이름',
    delStatus  TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '삭제 여부 (0=삭제 전, 1=삭제 후)',
    delDate    DATETIME COMMENT '삭제 날짜'
);

-- 회원 테이블
CREATE TABLE `member`
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate    DATETIME  NOT NULL,
    updateDate DATETIME  NOT NULL,
    loginId    CHAR(30)  NOT NULL,
    loginPw    CHAR(100) NOT NULL,
    address    TEXT      NOT NULL,
    authLevel  SMALLINT(2) NOT NULL DEFAULT 1 COMMENT '관리자 = 7, 회원 = 1, 수의사 = 3',
   `name`     CHAR(20)  NOT NULL,
    nickname   CHAR(20)  NOT NULL,
    cellphone  CHAR(20)  NOT NULL,
    email      CHAR(20)  NOT NULL,
    delStatus  TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '탈퇴 여부 (0=탈퇴 전, 1=탈퇴 후)',
    authName   CHAR(30)  NOT NULL COMMENT '일반 또는 수의사',
    delDate    DATETIME COMMENT '탈퇴 날짜'
);

-- 반려동물 행동 분석 테이블
CREATE TABLE pet_behavior_analysis
(
    id            INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId      INT(10) NOT NULL COMMENT 'FK',
    petId         INT(10) NOT NULL COMMENT 'FK',
    analyzedAt    DATETIME      NOT NULL,
    mood          ENUM('행복', '불안', '분노', '슬픔', '놀람') NOT NULL,
    behaviorLabel VARCHAR(100)  NOT NULL COMMENT 'FK',
    confidence    DECIMAL(5, 2) NOT NULL,
    imageUrl      TEXT NULL
);

-- 반려동물 건강 로그 테이블
CREATE TABLE pet__health_log
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    petId       INT(10) NOT NULL COMMENT 'FK',
    logDate     DATETIME      NOT NULL COMMENT '로그 기록',
    foodWeight  DECIMAL(6, 2) NOT NULL,
    waterWeight DECIMAL(6, 2) NOT NULL,
    litterCount INT(10) NOT NULL,
    notes       TEXT NULL
);

-- 산책 모임 멤버 테이블
CREATE TABLE walk_crew_member
(
    id       INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    crewId   INT(10) NOT NULL COMMENT 'PK, FK',
    memberId INT(10) NOT NULL COMMENT 'PK, FK',
    joinedAt DATETIME NOT NULL
);

-- 북마크 테이블
CREATE TABLE bookmark
(
    id        INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId  INT(10) NOT NULL COMMENT 'FK, UNIQUE(with articleId)',
    articleId INT(10) NOT NULL COMMENT 'FK, UNIQUE(with memberId)',
    regDate   DATETIME NOT NULL DEFAULT NOW(),
    UNIQUE (memberId, articleId)
);

-- 수의사 답변 테이블
CREATE TABLE vet_answer
(
    id       INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId INT(10) NOT NULL COMMENT 'FK',
    answer   TEXT NULL,
    answerAt DATETIME     NOT NULL DEFAULT NOW(),
    vetName  VARCHAR(100) NOT NULL
);

ALTER TABLE vet_answer ADD COLUMN qna_id INT(10) UNSIGNED NOT NULL COMMENT '질문 ID (Qna 테이블 FK)';
    
-- 반려동물 추천 장소 테이블
CREATE TABLE pet_recommendation
(
    id        INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId  INT(10) NOT NULL COMMENT 'FK',
    `type`      ENUM('병원', '용품') NOT NULL COMMENT '병원, 용품 등',
    `name`      VARCHAR(100) NOT NULL,
    address   TEXT         NOT NULL,
    phone     VARCHAR(50)  NOT NULL,
    mapUrl    TEXT         NOT NULL COMMENT '카카오 맵 크롤링',
    createdAt DATETIME     NOT NULL DEFAULT NOW()
);

-- 반려동물 예방 접종 테이블
CREATE TABLE pet_vaccination
(
    id            INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    petId         INT(10) NOT NULL COMMENT 'FK',
    vaccineName   VARCHAR(100) NULL,
    injectionDate DATE         NOT NULL,
    nextDueDate   DATE         NULL COMMENT '다음 접종 자동계산 기입(같은 접종이름, 업데이트 되도록)',
    vetName       VARCHAR(100)  NULL,
    notes         TEXT NULL
);

-- 반려동물 테이블
CREATE TABLE pet
(
    id        INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId  INT(10) NOT NULL COMMENT 'FK',
    `name`      VARCHAR(50)   NOT NULL,
    species   ENUM('강아지', '고양이') NOT NULL,
    breed     VARCHAR(100) NULL COMMENT '자식 낳았는지 안 낳았는지',
    gender    ENUM('수컷', '암컷') NOT NULL,
    birthDate DATE          NOT NULL,
    weight    DECIMAL(5, 2) NOT NULL
);

ALTER TABLE pet ADD COLUMN photo VARCHAR(255);


CREATE TABLE pet_analysis (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '분석 결과 ID',
  petId INT UNSIGNED NOT NULL COMMENT '반려동물 ID (FK)',
  imagePath VARCHAR(255) NOT NULL COMMENT '분석에 사용된 이미지 경로 또는 URL',
  emotionResult VARCHAR(50) NOT NULL COMMENT '감정 분석 결과 (예: happy, angry 등)',
  confidence FLOAT NOT NULL COMMENT '분석 결과의 신뢰도 (0.0 ~ 1.0)',
  analyzedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '분석 수행 일시',
  
  CONSTRAINT fk_pet_analysis_petId FOREIGN KEY (petId) REFERENCES pet(id) ON DELETE CASCADE
);

-- 산책 모임 테이블
CREATE TABLE walk_crew
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    title       VARCHAR(100) NOT NULL,
    descriptoin TEXT         NOT NULL,
    district_id INT          NOT NULL COMMENT 'FK → district(id)',
    leaderId    INT(10) NOT NULL,
    createdAt   DATETIME     NOT NULL DEFAULT NOW()
);

-- BLE 기반 반려동물 활동 테이블
CREATE TABLE pet_ble_activity
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    petId       INT(10) NOT NULL COMMENT 'FK',
    zoneName    VARCHAR(100) NOT NULL COMMENT '화장실 , 밥그릇, 물그릇, 침대',
    enteredAt   DATETIME     NOT NULL COMMENT '구역 진입 시간',
    exitedAt    DATETIME     NOT NULL COMMENT '구역 나간 시간',
    durationSec INT          NOT NULL COMMENT '구역 머문 시간',
    rssi        INT(10) NOT NULL
);


-- 게시글 테이블
CREATE TABLE article
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate    DATETIME  NOT NULL DEFAULT NOW(),
    updateDate DATETIME  NOT NULL DEFAULT NOW(),
    title      CHAR(100) NOT NULL,
    `body`     TEXT      NOT NULL
);

--  memberId 추가
ALTER TABLE article
    ADD COLUMN memberId INT(10) UNSIGNED NOT NULL AFTER updateDate;

--  boardId 추가
ALTER TABLE article
    ADD COLUMN boardId INT(10) NOT NULL AFTER `memberId`;

-- hit 추가
ALTER TABLE article
    ADD COLUMN hitCount INT(10) UNSIGNED NOT NULL DEFAULT 0 AFTER `body`;

--  article 테이블에 reactionPoint(좋아요) 컬럼 추가
ALTER TABLE article
    ADD COLUMN goodReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE article
    ADD COLUMN badReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;


-- 지역 정보 테이블
-- 시(city) → 구(district) → 동(dong) 구조의 행정동 정보를 저장
CREATE TABLE district
(
    id       INT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK, 고유 ID',
    city     VARCHAR(50) NOT NULL COMMENT '시/도 이름 (예: 서울특별시, 대전광역시)',
    district VARCHAR(50) NOT NULL COMMENT '구/군 이름 (예: 서구, 중구)',
    dong     VARCHAR(50) NOT NULL COMMENT '동네 이름 (예: 갈마동, 둔산동)'
);


-- 댓글 테이블
CREATE TABLE reply
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate     DATETIME NOT NULL,
    updateDate  DATETIME NOT NULL,
    memberId    INT(10) UNSIGNED NOT NULL,
    relTypeCode CHAR(50) NOT NULL COMMENT '관련 데이터 타입 코드',
    relId       INT(10) NOT NULL COMMENT '관련 데이터 번호',
    `body`      TEXT     NOT NULL
);

-- QnA 테이블
CREATE TABLE Qna
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId   INT(10) UNSIGNED NOT NULL COMMENT '질문 작성자 (회원)',
    title      VARCHAR(100) NOT NULL, -- 질문 제목
    `body`     TEXT         NOT NULL, -- 질문 내용 or 답변 본문
    isSecret   BOOLEAN DEFAULT FALSE, -- 비공개 여부
    isFromUser BOOLEAN DEFAULT FALSE, -- 사용자 등록 질문 여부
    isAnswered BOOLEAN DEFAULT FALSE, -- 관리자 답변 여부
    orderNo    INT(10) NOT NULL DEFAULT 0 COMMENT '출력 순서',
    regDate    DATETIME     NOT NULL,
    updateDate DATETIME     NOT NULL,
    isActive   BOOLEAN DEFAULT TRUE   -- 노출 여부 (숨김 처리 가능)
);

-- 자주 묻는 질문 구분(1= 자주 묻는 질문, 0 = 일반 질문)
ALTER TABLE qna ADD COLUMN isFaq TINYINT(1) NOT NULL DEFAULT 0;
UPDATE Qna SET isSecret = 0 WHERE isSecret NOT IN (0, 1);
UPDATE Qna SET isSecret = 1 WHERE id IN (1, 2, 3); -- 비밀글
UPDATE Qna SET isSecret = 0 WHERE id IN (4, 5, 6, 11); -- 공개글

--  reply 테이블에 좋아요 관련 컬럼 추가
ALTER TABLE reply ADD COLUMN goodReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE reply ADD COLUMN badReactionPoint INT(10) UNSIGNED NOT NULL DEFAULT 0;

-- reactionPoint 테이블 생성
CREATE TABLE reactionPoint
(
    id          INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    regDate     DATETIME NOT NULL,
    updateDate  DATETIME NOT NULL,
    memberId    INT(10) UNSIGNED NOT NULL,
    relTypeCode CHAR(50) NOT NULL COMMENT '관련 데이터 타입 코드',
    relId       INT(10) NOT NULL COMMENT '관련 데이터 번호',
    `point`     INT(10) NOT NULL
);

-- 수의사 인증서 테이블
CREATE TABLE vet_certificate
(
    id         INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
    memberId   INT(10) UNSIGNED NOT NULL COMMENT '회원 ID (FK)',
    fileName   VARCHAR(255)     NOT NULL COMMENT '업로드된 원본 파일명',
    filePath   VARCHAR(500)     NOT NULL COMMENT '서버 저장 경로',
    uploadedAt DATETIME         NOT NULL DEFAULT NOW() COMMENT '업로드 일시',
    approved   TINYINT(1) UNSIGNED NOT NULL DEFAULT 0 COMMENT '승인 여부 (0=대기, 1=승인, 2=거절)',

    -- FK 연결
    FOREIGN KEY (memberId) REFERENCES MEMBER(id) ON DELETE CASCADE
);

-- 백신 종류 및 주기 테이블
CREATE TABLE vaccine_schedule (
  vaccineName VARCHAR(100) PRIMARY KEY,
  intervalMonths INT NOT NULL COMMENT '백신 주기 (개월 단위)',
  `type` ENUM('Initial', 'Annual') NOT NULL COMMENT '초기 예방접종 또는 연간 접종 구분',
  DESCRIPTION TEXT NULL
  );
  
  CREATE TABLE calendar_event (
  id INT AUTO_INCREMENT PRIMARY KEY,
  memberId INT NOT NULL,                  
  petId INT NULL,                        
  eventDate DATE NOT NULL,               
  content TEXT NOT NULL,                 
  createdAt DATETIME DEFAULT NOW()       
);

ALTER TABLE calendar_event ADD COLUMN title VARCHAR(100) NOT NULL AFTER petId;

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