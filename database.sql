

DROP DATABASE IF EXISTS campus_board;
CREATE DATABASE campus_board;
USE campus_board;

CREATE TABLE student (
  student_id  INT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  email       VARCHAR(120) NOT NULL UNIQUE,
  password    VARCHAR(50)  NOT NULL,
  program     VARCHAR(80),
  year_level  INT
);

CREATE TABLE session (
  session_id    INT PRIMARY KEY,
  creator_id    INT NOT NULL,
  session_type  VARCHAR(20)  NOT NULL,   -- STUDY_SPOT or REVIEW_GROUP
  subject       VARCHAR(60)  NOT NULL,
  description   VARCHAR(300) NOT NULL,
  capacity      INT NOT NULL,
  status        VARCHAR(10)  NOT NULL,   -- OPEN or CLOSED
  created_at    DATETIME NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES student(student_id)
);

CREATE TABLE study_spot_session (
  session_id       INT PRIMARY KEY,
  location         VARCHAR(120) NOT NULL,
  available_seats  INT NOT NULL,
  FOREIGN KEY (session_id) REFERENCES session(session_id)
);

CREATE TABLE review_group_session (
  session_id      INT PRIMARY KEY,
  scheduled_date  DATE NOT NULL,
  scheduled_time  TIME NOT NULL,
  max_members     INT  NOT NULL,
  FOREIGN KEY (session_id) REFERENCES session(session_id)
);

CREATE TABLE join_request (
  request_id    INT PRIMARY KEY,
  session_id    INT NOT NULL,
  requester_id  INT NOT NULL,
  status        VARCHAR(10) NOT NULL,    -- PENDING, ACCEPTED or DECLINED
  requested_at  DATETIME NOT NULL,
  responded_at  DATETIME NULL,
  FOREIGN KEY (session_id)   REFERENCES session(session_id),
  FOREIGN KEY (requester_id) REFERENCES student(student_id)
);

CREATE TABLE session_member (
  session_id  INT NOT NULL,
  student_id  INT NOT NULL,
  joined_at   DATETIME NOT NULL,
  PRIMARY KEY (session_id, student_id),
  FOREIGN KEY (session_id) REFERENCES session(session_id),
  FOREIGN KEY (student_id) REFERENCES student(student_id)
);

SHOW tables;

SELECT * FROM student;
SELECT * FROM session;
SELECT * FROM study_spot_session;
SELECT * FROM review_group_session;
SELECT * FROM join_request;
SELECT * FROM session_member;