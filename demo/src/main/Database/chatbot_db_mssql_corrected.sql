-- Drop and create the database
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'chatbot_db')
    DROP DATABASE chatbot_db;
GO

CREATE DATABASE chatbot_db;
GO

USE chatbot_db;
GO

-- Users Table
CREATE TABLE users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash CHAR(60) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    last_login DATETIME NULL
);
GO

-- Topics Table
CREATE TABLE topics (
    topic_id INT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- Questions Table
CREATE TABLE questions (
    question_id INT IDENTITY(1,1) PRIMARY KEY,
    topic_id INT,
    question NVARCHAR(MAX) NOT NULL,
    answer NVARCHAR(MAX) NOT NULL,
    created_at DATETIME DEFAULT GETDATE(),
    created_by INT,
    FOREIGN KEY (topic_id) REFERENCES topics(topic_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);
GO

-- Create unique index for full-text key
CREATE UNIQUE INDEX uq_question_id ON questions(question_id);
GO

-- Create full-text catalog and index
CREATE FULLTEXT CATALOG ftCatalog AS DEFAULT;
GO

CREATE FULLTEXT INDEX ON questions(question, answer)
    KEY INDEX uq_question_id
    ON ftCatalog;
GO

-- Conversation Context Table
CREATE TABLE conversation_context (
    context_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT,
    current_topic VARCHAR(100),
    previous_questions NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
GO

-- Feedback Table
CREATE TABLE feedback (
    feedback_id INT IDENTITY(1,1) PRIMARY KEY,
    question_id INT,
    rating TINYINT CHECK (rating BETWEEN 1 AND 5),
    feedback_text NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (question_id) REFERENCES questions(question_id)
);
GO

-- Indexes
CREATE INDEX idx_questions_topic ON questions(topic_id);
GO

CREATE INDEX idx_context_user ON conversation_context(user_id);
GO
