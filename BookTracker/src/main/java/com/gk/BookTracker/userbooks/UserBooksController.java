package com.gk.BookTracker.userbooks;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import com.gk.BookTracker.book.Book;
import com.gk.BookTracker.book.BookRepository;
import com.gk.BookTracker.user.BooksByUser;
import com.gk.BookTracker.user.BooksByUserRepository;

@Controller
public class UserBooksController {
	
	@Autowired
	UserBooksRepository userBooksRepository;
	
	@Autowired
	BooksByUserRepository booksByUserRepository;
	
	@Autowired
	BookRepository bookRepository;

	@PostMapping("/addUserBook")
	public ModelAndView addBookForUser(@RequestBody MultiValueMap<String, String> formData,
			@AuthenticationPrincipal OAuth2User principal)
	{
		if(principal == null || principal.getAttribute("login") == null) {
			return null;
		}
		
		String bookId = formData.getFirst("bookId");
		Optional<Book> optionalBook =  bookRepository.findById(bookId);
		if (!optionalBook.isPresent())
		{
			return new ModelAndView("redirect:/");
		}
		Book book = optionalBook.get();
		
		UserBooks userBooks = new UserBooks();
		UserBooksPrimaryKey key = new UserBooksPrimaryKey();
		String userId = principal.getAttribute("login");
		key.setUserId(userId);
		key.setBookId(bookId);
		userBooks.setKey(key);
		userBooks.setStartedDate(LocalDate.parse(formData.getFirst("startDate")));
		userBooks.setCompletedDate(LocalDate.parse(formData.getFirst("completedDate")));
		userBooks.setReadingStatus(formData.getFirst("readingStatus"));
		int rating = Integer.parseInt(formData.getFirst("rating"));
		userBooks.setRating(rating);
		userBooksRepository.save(userBooks);
		
		//Persisting data in BooksByUser repository
		BooksByUser booksByUser = new BooksByUser();
		booksByUser.setId(userId);
		booksByUser.setBookName(book.getName());
		booksByUser.setBookId(book.getId());
		booksByUser.setCoverIds(book.getCoverIds());
		booksByUser.setAuthorNames(book.getAuthorNames());
        booksByUser.setReadingStatus(formData.getFirst("readingStatus"));
        booksByUser.setRating(rating);
        booksByUserRepository.save(booksByUser);
		
		return new ModelAndView("redirect:/books/"+bookId);
	}
	
}
