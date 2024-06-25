package application.Controllers;

import application.Models.Book;
import application.Utils.DatabaseUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class HomeScreenController {

    @FXML
    private TableView<Book> bookTableView;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TableColumn<Book, String> authorColumn;

    @FXML
    private Button borrowButton;

    @FXML
    private Button returnButton;

    @FXML
    private TextField userIdField;

    @FXML
    private TextField lendDateField;

    private ObservableList<Book> bookList = FXCollections.observableArrayList();

    private Connection connection;

    @FXML
    public void initialize() {
        // Initialize TableView columns
        titleColumn.setCellValueFactory(cellData -> cellData.getValue().titleProperty());
        authorColumn.setCellValueFactory(cellData -> cellData.getValue().authorProperty());

        try {
            connection = DatabaseUtil.getConnection();
            if (connection != null) {
                loadBooks();
            } else {
                showErrorAlert("Database connection failed!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database connection error: " + e.getMessage());
        }
    }

    private void loadBooks() {
        String query = "SELECT * FROM book";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");

                Book book = new Book(id, title, author);
                bookList.add(book);
            }

            // Ensure bookTableView is not null before setting items
            if (bookTableView != null) {
                bookTableView.setItems(bookList);
            } else {
                showErrorAlert("TableView not initialized!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error loading books: " + e.getMessage());
        }
    }

    @FXML
    void borrowBook() {
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showErrorAlert("Please select a book to borrow!");
            return;
        }

        String userId = userIdField.getText().trim();
        String lendDate = lendDateField.getText().trim();

        if (userId.isEmpty() || lendDate.isEmpty()) {
            showErrorAlert("User ID and lend date fields cannot be empty!");
            return;
        }

        // Insert lending information into the lend table
        String lendQuery = "INSERT INTO lend (book_id, user_id, lend_date) VALUES (?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(lendQuery);
            statement.setInt(1, selectedBook.getId());
            statement.setInt(2, Integer.parseInt(userId));
            statement.setDate(3, java.sql.Date.valueOf(lendDate));
            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                showInfoAlert("Book borrowed successfully!");
            } else {
                showErrorAlert("Failed to borrow book!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error borrowing book: " + e.getMessage());
        }
    }

    @FXML
    void returnBook() {
        // Get the selected book
        Book selectedBook = bookTableView.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showErrorAlert("Please select a book to return!");
            return;
        }

        // Get user ID
        String userId = userIdField.getText().trim();
        if (userId.isEmpty()) {
            showErrorAlert("User ID cannot be empty!");
            return;
        }

        // Update lending information in the lend table
        String returnQuery = "UPDATE lend SET return_date = ? WHERE book_id = ? AND user_id = ? AND return_date IS NULL";
        try {
            PreparedStatement statement = connection.prepareStatement(returnQuery);
            statement.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            statement.setInt(2, selectedBook.getId());
            statement.setInt(3, Integer.parseInt(userId));
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                showInfoAlert("Book returned successfully!");
            } else {
                showErrorAlert("Failed to return book! Make sure the book was borrowed by this user and hasn't already been returned.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error returning book: " + e.getMessage());
        }
    }

    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
