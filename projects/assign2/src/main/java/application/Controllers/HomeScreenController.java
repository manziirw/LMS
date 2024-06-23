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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeScreenController {

    @FXML
    private TableView<Book> bookTableView;

    @FXML
    private TableColumn<Book, String> titleColumn;

    @FXML
    private TableColumn<Book, String> authorColumn;

    @FXML
    private Button borrowButton;

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
        if (selectedBook != null) {
            // Implement borrowing logic here
            showInfoAlert("Book borrowed: " + selectedBook.getTitle());
        } else {
            showErrorAlert("Please select a book to borrow!");
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
