package com.example;

import java.sql.*;
import java.util.*;
import java.net.http.*;
import java.net.URI;
import org.json.JSONObject;

public class DBHelper {

    private static final String API_KEY = "QLe52EYIyUQ3Y95YScGyh7cMMWLUx2LOLK4iKsww";
    private static final String API_URL = "https://api.cohere.com/v2/generate";
    private Connection connection;

    public DBHelper() {
        try {
            // Load the SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    
            // Connection URL for SQL Server with named instance
            String url = "jdbc:sqlserver://localhost:1433;databaseName=chatbot_db;encrypt=true;trustServerCertificate=true";
            
            // SQL Server credentials (escaped backslash in username)
            String user = "sa"; // Domain or machine name + SQL login
            String password = "hassan428826";     // Replace with your actual password
    
            // Establish connection
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connected to SQL Server successfully.");
        } catch (Exception e) {
            System.err.println("❌ Database connection failed:");
            e.printStackTrace();
        }
    }
    

    public boolean authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerUser(String username, String password) {
        String checkQuery = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(checkQuery)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return false;

            String insertQuery = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllTopics() {
        List<String> topics = new ArrayList<>();
        String query = "SELECT name FROM topics";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                topics.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topics;
    }

    public List<String> getQuestionsForTopic(String topic) {
        List<String> questions = new ArrayList<>();
        String query = "SELECT question FROM questions WHERE topic_id = (SELECT topic_id FROM topics WHERE name = ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, topic);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    questions.add(rs.getString("question"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public void addQuestionToTopic(String topic, String question, String answer, int userId) {
        String query = "INSERT INTO questions (topic_id, question, answer, created_by) VALUES ((SELECT topic_id FROM topics WHERE name = ?), ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, topic);
            stmt.setString(2, question);
            stmt.setString(3, answer);
            stmt.setInt(4, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String callLLMAPI(String question) {
        try {
            String requestBody = "{\"prompt\": \"" + question + "\", \"max_tokens\": 10000}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());
            return jsonResponse.getJSONArray("generations").getJSONObject(0).getString("text").trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't connect to the API at this time.";
        }
    }

    public void saveFeedback(int questionId, int rating, String feedbackText) {
        String query = "INSERT INTO feedback (question_id, rating, feedback_text) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, questionId);
            stmt.setInt(2, rating);
            stmt.setString(3, feedbackText);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getFeedbackForQuestion(int questionId) {
        List<String> feedbackList = new ArrayList<>();
        String query = "SELECT feedback_text FROM feedback WHERE question_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                feedbackList.add(rs.getString("feedback_text"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feedbackList;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getAnswer(String question) {
        String sql = "SELECT answer FROM questions WHERE LOWER(LTRIM(RTRIM(question))) = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, question.toLowerCase().trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("answer");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getSimilarAnswer(String question) {
        String sql = "SELECT TOP 1 answer FROM questions WHERE question LIKE ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + question + "%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("answer");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getUserIdByUsername(String username) {
        String query = "SELECT user_id FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("user_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<String> getUserHistory(int userId) {
    List<String> history = new ArrayList<>();
    String query = """
        SELECT TOP 20 question, answer
        FROM questions
        WHERE created_by = ?
        ORDER BY created_at DESC
    """;

    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String question = rs.getString("question");
            String answer = rs.getString("answer");

            String previewQ = question.length() > 10 ? question.substring(0, 10) + "..." : question;
            String previewA = answer.length() > 10 ? answer.substring(0, 10) + "..." : answer;

            history.add("Q: " + previewQ + " | A: " + previewA);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return history;
}

public List<String[]> getUserHistoryEntries(int userId) {
    List<String[]> history = new ArrayList<>();
    String query = """
        SELECT TOP 20 question, answer
        FROM questions
        WHERE created_by = ?
        ORDER BY created_at DESC
    """;
    try (PreparedStatement stmt = connection.prepareStatement(query)) {
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            history.add(new String[]{
                rs.getString("question"),
                rs.getString("answer")
            });
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return history;
}
}