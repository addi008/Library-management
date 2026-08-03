package com.library.dao;

import com.library.exception.BookNotFoundException;
import com.library.model.Book;

import java.util.List;

public interface BookDAO {
    boolean addBook(Book book);
    boolean updateBook(Book book);
    boolean deleteBook(int bookId);
    Book getBookById(int bookId) throws BookNotFoundException;
    List<Book> getAllBooks();
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    Book searchByISBN(String isbn) throws BookNotFoundException;
}
